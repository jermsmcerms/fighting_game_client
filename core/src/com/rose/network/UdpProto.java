package com.rose.network;

import com.rose.ggpo.GameInput;
import com.rose.ggpo.IPollSink;
import com.rose.ggpo.Poll;
import com.rose.ggpo.RingBuffer;
import com.rose.ggpo.TimeSync;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class UdpProto implements IPollSink {
    /* Intervals are measured in nano seconds. 1ms = 1,000,000 ns */
    private static final long SYNC_RETRY_INTERVAL       = 2000000000L;
    private static final long CONNECT_RETRY_INTERVAL    = 2000000000L; // 2000 ms = 2 billion ns
    private static final int SYNC_FIRST_RETRY_INTERVAL  = 500000000;
    private static final int NUM_SYNC_PACKETS = 5;
    private static final int MAX_SEQ_DISTANCE = 32768;
    private static final long RUNNING_RETRY_INTERVAL    = 200000000L;
    private static final long QUALITY_REPORT_INTERVAL   = 100000000L;
    private static final long NETWORK_STATS_INTERVAL    = 100000000L;
    private int next_send_seq;
    private final RingBuffer<GameInput> pending_output;
    private final RingBuffer<QueueEntry> send_queue;
    private final RingBuffer<UdpProtocolEvent> event_queue;
    private final SocketAddress server_addr;
    private final Udp udp;
    private final State state;
    private final TimeSync time_sync;
    private ConnectState current_state;
    private int next_recv_seq;
    private boolean canStart;
    private int playerNumber;
    private final UdpMsg.ConnectStatus remote_connect_status;
    private GameInput last_received_input;
    private int remote_frame_advantage;
    private long last_send_time;
    private int remote_magic_number;
    private int magic_number;
    private boolean connected;
    private boolean disconnect_notify_sent;
    private GameInput last_sent_input;
    private GameInput last_acked_input;
    private UdpMsg.ConnectStatus local_connect_status;
    private boolean disconnect_event_sent;
    private long round_trip_time;
    private int local_frame_advantage;

    public UdpProto(Udp udp, Poll poll, String serverIp, int portNum) {
        this.udp = udp;
        poll.registerLoop(this);
        event_queue = new RingBuffer<>(64);
        send_queue = new RingBuffer<>(64);
        pending_output = new RingBuffer<>(64);
        remote_connect_status = new UdpMsg.ConnectStatus();
        server_addr = new InetSocketAddress(serverIp, portNum);
        state = new State();
        time_sync = new TimeSync();
        canStart = false;
        next_send_seq = 0;
        next_recv_seq = 0;

        do {
            magic_number = ThreadLocalRandom.current().nextInt();
        } while(magic_number <= 0);

        if(udp != null) {
            current_state = ConnectState.Connecting;
            sendConnectRequest();
        }

        last_received_input = new GameInput(GameInput.NULL_FRAME, 0);
        last_sent_input = new GameInput(GameInput.NULL_FRAME, 0);
        last_acked_input = new GameInput(GameInput.NULL_FRAME, 0);

        local_connect_status = new UdpMsg.ConnectStatus();
    }

    public ConnectState getConnectState() {
        return current_state;
    }

    public UdpMsg.ConnectStatus getRemoteStatus() {
        return remote_connect_status;
    }

    void sendConnectRequest() {
        if(current_state != ConnectState.Connecting) {
            current_state = ConnectState.Connecting;
        }
        UdpMsg msg = new UdpMsg(UdpMsg.MsgType.ConnectReq);
        msg.payload.connReq.random_request = 1;
        sendMsg(msg);
    }

    private void sendSyncRequest() {
        Random rand = new Random();
        state.sync.random = rand.nextInt() & 0xFFFF;
        UdpMsg msg = new UdpMsg(UdpMsg.MsgType.SyncRequest);
        msg.payload.syncReq.random_request = state.sync.random;
        sendMsg(msg);
    }

    public void sendMsg(UdpMsg msg) {
        last_send_time = System.nanoTime();        
        msg.hdr.sequenceNumber = next_send_seq++;
        msg.hdr.magicNumber = magic_number;
        send_queue.push(new QueueEntry(System.nanoTime(), server_addr, msg));
        pumpSendQueue();
    }

    @Override
    public boolean onLoopPoll(Object o) {
        if(udp == null) { return false; }
        long now = System.nanoTime();
        long next_interval;
        pumpSendQueue();
        switch(current_state) {
            case Connecting:
                // if its been longer than 2000-ms and still not connected to server, resend sync request
                if(last_send_time > 0 && now - last_send_time > CONNECT_RETRY_INTERVAL) {
                    sendConnectRequest();
                }
                break;
            case Connected:
                // Now that we're connected. queue up a sync request?
                event_queue.push(new UdpProtocolEvent(UdpProtocolEvent.Event.Connected));
                break;
            case Syncing:
                // similar to the connecting state, if it's been longer than x-ms and haven't received a sync reply,
                // send another sync request.
                next_interval = (state.sync.round_trips_remaining == NUM_SYNC_PACKETS) ? SYNC_FIRST_RETRY_INTERVAL : SYNC_RETRY_INTERVAL;
                if (last_send_time > 0 && last_send_time + next_interval < now) {
                    sendSyncRequest();
                }
                break;
            case Running:
                if( state.running.last_input_packed_recv_time <= 0 ||
                    state.running.last_input_packed_recv_time + RUNNING_RETRY_INTERVAL < now) {
                    System.out.println("haven't exchanged packets in a while (last received: " +
                            last_received_input.getFrame() + ", last sent: " +
                            last_sent_input.getFrame() + "). Resending");
                    sendPendingOutput();
                    state.running.last_input_packed_recv_time = now;
                }

                if( state.running.last_quality_report_time <= 0 ||
                    state.running.last_quality_report_time +
                        QUALITY_REPORT_INTERVAL < now) {
                    UdpMsg msg = new UdpMsg(UdpMsg.MsgType.QualityReport);
                    msg.payload.qualrpt.ping = System.nanoTime();
                    msg.payload.qualrpt.frame_advantage = local_frame_advantage;
                    sendMsg(msg);
                    state.running.last_quality_report_time = now;
                }

                if( state.running.last_network_stats_interval <= 0 ||
                    state.running.last_network_stats_interval +
                        NETWORK_STATS_INTERVAL < now) {
                    state.running.last_network_stats_interval = now;
                }
                break;
            case Disconnected:
                break;
        }
        return true;
    }


    private void pumpSendQueue() {
        while(!send_queue.empty()) {
            QueueEntry entry = send_queue.front();
            // TODO: Handle Jitter here.
            // TODO: Then handle oop properties. If oop checks fail then:
            udp.sendTo(entry.msg, entry.dest_addr);
            // regardless of oop check result or send entry msg, pop the entry
            send_queue.pop();
        }
        // TODO: send the rouge packet it is needed.
    }

    public void onMsg(UdpMsg msg) {
        boolean handled = false;
        switch(msg.hdr.type) {
            case 0:
                onInvalid();
                break;
            case 1:
                handled = onConnectReq();
                break;
            case 2:
                handled = onConnectRep(msg);
                break;
            case 3:
                handled = onSyncReq(msg);
                break;
            case 4:
                handled = onSyncRep(msg);
                break;
            case 5:
                handled = onStartReq();
                break;
            case 6:
                handled = onStartRep(msg);
                break;
            case 7:
                handled = onInput(msg);
                break;
            case 8:
                handled = onQualityReport(msg);
                break;
            case 9:
                handled = onQualityReply(msg);
        }

        int seq = msg.hdr.sequenceNumber;
        if(	msg.hdr.type != UdpMsg.MsgType.ConnectReq.ordinal() &&
            msg.hdr.type != UdpMsg.MsgType.ConnectReply.ordinal() &&
            msg.hdr.type != UdpMsg.MsgType.SyncRequest.ordinal() &&
            msg.hdr.type != UdpMsg.MsgType.SyncReply.ordinal()) {
            if(msg.hdr.magicNumber != remote_magic_number) {
                return;
            }
        }

        int skipped = seq - next_recv_seq;
        if(skipped > MAX_SEQ_DISTANCE) {
            return;
        }

        next_recv_seq = seq;
        if(handled) {
            // TODO: set last_recevied_time here
            if( disconnect_notify_sent &&
                current_state == ConnectState.Running) {
                event_queue.push(new UdpProtocolEvent(
                    UdpProtocolEvent.Event.NetworkResumed));
                disconnect_notify_sent = false;
            }
        }
    }

    private void onInvalid() {
    }

    private boolean onConnectReq() {
        return true;
    }

    private boolean onConnectRep(UdpMsg msg) {
        if(current_state == ConnectState.Connecting) {
            current_state = ConnectState.Connected;
        }
        playerNumber = msg.payload.connRep.playerNumber;
        return true;
    }

    private boolean onSyncReq(UdpMsg msg) {
        if(remote_magic_number > 0 && msg.hdr.magicNumber != remote_magic_number) {
            return false;
        }

        UdpMsg reply = new UdpMsg(UdpMsg.MsgType.SyncReply);
        reply.payload.syncRep.random_reply = msg.payload.syncReq.random_request;
        sendMsg(reply);
        return true;
    }

    private boolean onSyncRep(UdpMsg msg) {
        if(current_state != ConnectState.Syncing) {
            return msg.hdr.magicNumber == remote_magic_number;
        }

        if(msg.payload.syncRep.random_reply != state.sync.random) {
            return false;
        }

        if(!connected) {
            event_queue.push(new UdpProtocolEvent(UdpProtocolEvent.Event.Connected));
            connected = true;
        }

        if(--state.sync.round_trips_remaining == 0) {
            event_queue.push(new UdpProtocolEvent(UdpProtocolEvent.Event.Synchronized));
            current_state = ConnectState.Running;
            last_received_input = new GameInput(-1, -1);
            remote_magic_number = msg.hdr.magicNumber;
        } else {
            UdpProtocolEvent event = new UdpProtocolEvent(UdpProtocolEvent.Event.Synchronizing);
            event.syncing.total = NUM_SYNC_PACKETS;
            event.syncing.count = NUM_SYNC_PACKETS - state.sync.round_trips_remaining;
            event_queue.push(event);
            sendSyncRequest();
        }
        return true;
    }

    private boolean onStartReq() {

        return true;
    }

    private boolean onStartRep(UdpMsg msg) {
        canStart = msg.payload.startRep.response != 0;
        if(!canStart) {
            UdpMsg reply = new UdpMsg(UdpMsg.MsgType.StartReq);
            reply.payload.startReq.canStart = 1;
            sendMsg(reply);
        } else {
            current_state = ConnectState.Running;
        }
        return true;
    }

    private boolean onInput(UdpMsg msg) {
        boolean disconnect_requested = msg.payload.input.disconnect_requested;
        if(disconnect_requested) {
            if(current_state != ConnectState.Disconnected && !disconnect_event_sent) {
                event_queue.push(new UdpProtocolEvent(UdpProtocolEvent.Event.Disconnected));
                disconnect_event_sent = true;
            }
        } else {
            UdpMsg.ConnectStatus remote_status = msg.payload.input.connect_status;
            // TODO: update peer connect status "array" here:
        }

        if(msg.payload.input.num_inputs > 0) {
            int num_inputs = msg.payload.input.num_inputs;
            int current_frame = msg.payload.input.start_frame;
            int[] inputs = new int[num_inputs];
            System.arraycopy(msg.payload.input.inputs, 0, inputs, 0, inputs.length);

            for(int input : inputs) {
                boolean useInputs = current_frame == last_received_input.getFrame() + 1;
                last_received_input.setInput(input);

                if(useInputs) {
                    last_received_input.setFrame(current_frame);
                    UdpProtocolEvent event = new UdpProtocolEvent(UdpProtocolEvent.Event.Input);
                    event.input.input = new GameInput(last_received_input.getFrame(), last_received_input.getInput());
                    event_queue.push(event);
                    state.running.last_input_packed_recv_time = System.nanoTime();
                }

                current_frame++;
            }
        }

        while(pending_output.size() > 0 && pending_output.front().getFrame() < msg.payload.input.ack_frame) {
            last_acked_input = new GameInput(pending_output.front().getFrame(), pending_output.front().getInput());
            pending_output.pop();
        }
        return true;
    }

    private boolean onQualityReport(UdpMsg msg) {
        UdpMsg reply = new UdpMsg(UdpMsg.MsgType.QualityReply);
        reply.payload.qualrep.pong = msg.payload.qualrpt.ping;
        sendMsg(reply);
        remote_frame_advantage = msg.payload.qualrpt.frame_advantage;
        return true;
    }

    private boolean onQualityReply(UdpMsg msg) {
        round_trip_time = System.nanoTime() - msg.payload.qualrep.pong;
        return true;
    }

    public boolean getCanStart() {
        return canStart;
    }

    public void sendInput(GameInput gameInput, UdpMsg.ConnectStatus local_connect_status) {
        if(udp != null) {
            if(current_state == ConnectState.Running) {
                // TODO: increment time sync frame here
                time_sync.advance_frame(gameInput, local_frame_advantage, remote_frame_advantage);
                pending_output.push(gameInput);
            }
            updateLocalConnectStatus(local_connect_status);
            sendPendingOutput();
        }
    }

    private void updateLocalConnectStatus(UdpMsg.ConnectStatus local_connect_status) {
        this.local_connect_status.disconnected = local_connect_status.disconnected;
        this.local_connect_status.last_frame = local_connect_status.last_frame;
    }

    private void sendPendingOutput() {
        UdpMsg msg = new UdpMsg(UdpMsg.MsgType.Input);
        int j;
        // TODO: something for inputs?

        if(pending_output.size() > 0) {
            msg.payload.input.start_frame = pending_output.front().getFrame();
            for(j = 0; j < pending_output.size; j++) {
                GameInput current = pending_output.item(j);
                msg.payload.input.inputs[j] = current.getInput();
                last_sent_input = current;
            }
        } else {
            msg.payload.input.start_frame = 0;
        }

        msg.payload.input.ack_frame = last_received_input.getFrame();
        msg.payload.input.num_inputs = pending_output.size();
        msg.payload.input.disconnect_requested = current_state == ConnectState.Disconnected;
        // Copy local connect status into msg.payload.input.connect_status
        if(local_connect_status == null) {
            local_connect_status = new UdpMsg.ConnectStatus();
            local_connect_status.disconnected = false;
            local_connect_status.last_frame = 0;
        } else {
            msg.payload.input.connect_status.disconnected = local_connect_status.disconnected;
            msg.payload.input.connect_status.last_frame = local_connect_status.last_frame;
        }

        sendMsg(msg);
    }

    public UdpProtocolEvent getEvent() {
        if(event_queue.size() == 0) { return null; }
        UdpProtocolEvent event = event_queue.front();
        event_queue.pop();
        return event;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public void synchronize() {
        if(udp != null) {
            current_state = ConnectState.Syncing;
            state.sync.round_trips_remaining = NUM_SYNC_PACKETS;
            sendSyncRequest();
        }
    }

    public boolean isSynchronized() {
        return current_state == ConnectState.Synchronized;
    }

    public boolean isInitialized() {
        return udp == null;
    }

    public void setLocalFrameNumber(int local_frame) {
        long remote_frame = last_received_input.getFrame() +
            (round_trip_time * 60 / 1000000000L);
        local_frame_advantage = (int)(remote_frame - local_frame);
    }

    public int recommendFrameDelay() {
        return time_sync.recommend_frame_wait_duration(false);
    }
}

class QueueEntry {
    public long queue_time;
    public SocketAddress dest_addr;
    UdpMsg msg;

    public QueueEntry(long queue_time, SocketAddress in_addr, UdpMsg msg) {
        this.queue_time = queue_time;
        this.dest_addr = in_addr;
        this.msg = msg;
    }
}

class State {
    public Sync sync;
    public Connect connecting;
    public Running running;
    public State() {
        sync = new Sync();
        connecting = new Connect();
        running = new Running();
    }

    public static class Connect {
        public long last_connect_reply_time;
    }

    public static class Sync {
        public int round_trips_remaining;
        public int random;
    }

    public static class Running {
        public long last_quality_report_time;
        public long last_network_stats_interval;
        public long last_input_packed_recv_time;
    }
}

