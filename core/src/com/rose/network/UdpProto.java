package com.rose.network;

import com.rose.ggpo.GameInput;
import com.rose.ggpo.IPollSink;
import com.rose.ggpo.Poll;
import com.rose.ggpo.RingBuffer;
import com.rose.ggpo.TimeSync;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Random;

public class UdpProto implements IPollSink {
    private static final int NUM_SYNC_PACKETS = 5;
    private static final int MAX_SEQ_DISTANCE = 32768;
    private static final long QUALITY_REPORT_INTERVAL = 1000000000L;
    private int next_send_seq;
    private RingBuffer<GameInput> pending_output;
    private RingBuffer<QueueEntry> send_queue;
    private RingBuffer<UdpProtocolEvent> event_queue;
    private SocketAddress server_addr;
    private Udp udp;
    private int disconnect_timeout;
    private int disconnect_notify_start;
    private State state;
    private ConnectState current_state;
    private int next_recv_seq;
    private boolean canStart;
    private int playerNumber;
    private UdpMsg.ConnectStatus remote_connect_status;
    private int local_frame_advantage;
    private long round_trip_time;
    private GameInput last_received_input;
    private TimeSync timeSync;
    private int remote_frame_advantage;

    public UdpProto(Udp udp, Poll poll, String serverIp, int portNum, int disconnect_timeout, int disconnect_notify_start) {
        this.udp = udp;
        this.disconnect_timeout = disconnect_timeout;
        this.disconnect_notify_start = disconnect_notify_start;
        pending_output = new RingBuffer<>(64);
        send_queue = new RingBuffer<>(64);
        event_queue = new RingBuffer<>(64);
        next_send_seq = 0;
        server_addr = new InetSocketAddress(serverIp, portNum);
        poll.registerLoop(this);

        state = new State();

        if(udp != null) {
            current_state = ConnectState.Syncing;
            state.sync.round_trips_remaining = NUM_SYNC_PACKETS;
            sendSyncRequest();
        }

        next_recv_seq = 0;
        canStart = false;

        remote_connect_status = new UdpMsg.ConnectStatus();
        timeSync = new TimeSync();
    }

    public UdpMsg.ConnectStatus getRemoteStatus() {
        return remote_connect_status;
    }

    private void sendSyncRequest() {
        Random rand = new Random();
        state.sync.random = rand.nextInt() & 0xFFFF;
        UdpMsg msg = new UdpMsg(UdpMsg.MsgType.ConnectReq);
        msg.payload.connReq.random_request = state.sync.random;
        sendMsg(msg);
    }

    public void sendMsg(UdpMsg msg) {
        // TODO: Update network stats here, such as packets sent.
        msg.hdr.sequenceNumber = next_send_seq++;
        send_queue.push(new QueueEntry(System.nanoTime(), server_addr, msg));
        pumpSendQueue();
    }

    @Override
    public boolean onLoopPoll(Object o) {
        if(udp == null) { return false; }
        long now = System.nanoTime();
        pumpSendQueue();
        // TODO: handle different states, such as, syncing, running, or disconnecting
        switch(current_state) {
            case Syncing:
                break;
            case Running:
                if(state.running.last_quality_report_time <= 0 ||
                    state.running.last_quality_report_time +
                    QUALITY_REPORT_INTERVAL < now) {
                    UdpMsg msg = new UdpMsg(UdpMsg.MsgType.QualityReport);
                    msg.payload.qualrpt.ping = System.nanoTime();
                    msg.payload.qualrpt.frame_advantage = local_frame_advantage;
                    sendMsg(msg);
                    state.running.last_quality_report_time = now;
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

    public boolean handlesMsg(SocketAddress from) {
        if(udp == null) { return false; }
        return from.equals(server_addr);
    }

    public void onMsg(UdpMsg msg) {
        // TODO: handle the different kinds of messages that the server may send
        // TODO: if the client receives a connect reply, I need to
        //       start some kind of loop where we send "are we there yet"
        //       messages to the server. When the server responds YES,
        //       then we can start sending/receiving inputs
        boolean handled = false;
        switch(msg.hdr.type) {
            case 0:
                onInvalid(msg);
                break;
            case 1:
                handled = onConnectReq(msg);
                break;
            case 2:
                handled = onConnectRep(msg);
                break;
            case 3:
                handled = onStartReq(msg);
                break;
            case 4:
                handled = onStartRep(msg);
                break;
            case 5:
                handled = onInput(msg);
                break;
            case 6:
                handled = onQualityReport(msg);
                break;
            case 7:
                handled = onQualityReply(msg);
        }

        int seq = msg.hdr.sequenceNumber;
        if(	msg.hdr.type != UdpMsg.MsgType.ConnectReq.ordinal() &&
                msg.hdr.type != UdpMsg.MsgType.ConnectReply.ordinal()) {
            // TODO: reject messages from senders we don't expect
        }

        int skipped = seq - next_recv_seq;
        if(skipped > MAX_SEQ_DISTANCE) {
            return;
        }

        next_recv_seq = seq;
        if(msg.hdr.type < 0 || msg.hdr.type > 3) {
            onInvalid(msg);
        }

        if(handled) {
            // TODO: handle network resumed events
            //		 if a disconnect notification has been sent and
            //		 if the current state is running
        }
    }

    private void onInvalid(UdpMsg msg) {
    }

    private boolean onConnectReq(UdpMsg msg) {
        return true;
    }

    private boolean onConnectRep(UdpMsg msg) {
        if(msg.payload.connRep != null) {
            playerNumber = msg.payload.connRep.playerNumber;
        }
        UdpMsg reply = new UdpMsg(UdpMsg.MsgType.StartReq);
        reply.payload.startReq.canStart = 1;
        sendMsg(reply);
        return true;
    }

    private boolean onStartReq(UdpMsg msg) {

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
        UdpProtocolEvent event =
                new UdpProtocolEvent(UdpProtocolEvent.Event.Input);
        remote_connect_status.disconnected = msg.payload.input.connect_status.disconnected;
        remote_connect_status.last_frame = msg.payload.input.connect_status.last_frame;

        event.input.input = new GameInput(
                msg.payload.input.frame,
                msg.payload.input.input);

        event_queue.push(event);
        last_received_input = new  GameInput(event.input.input.getFrame(), event.input.input.getInput());
        return true;
    }

    private boolean onQualityReport(UdpMsg msg) {
        UdpMsg reply = new UdpMsg(UdpMsg.MsgType.QualityReply);
        reply.payload.qualrep.pong = msg.payload.qualrpt.ping;
        remote_frame_advantage = msg.payload.qualrpt.frame_advantage;
        sendMsg(reply);
        return true;
    }

    private boolean onQualityReply(UdpMsg msg) {
        round_trip_time = System.nanoTime() - msg.payload.qualrep.pong;
        return true;
    }

    public boolean getCanStart() {
        return canStart;
    }

    public void sendInput(GameInput gameInput, UdpMsg.ConnectStatus connectStatus) {
        if(udp != null) {
            if(current_state == ConnectState.Running) {
                // TODO: increment time sync frame here
                timeSync.advance_frame(gameInput, local_frame_advantage, remote_frame_advantage);
                pending_output.push(gameInput);
            }
            sendPendingOutput(connectStatus);
        }
    }

    private void sendPendingOutput(UdpMsg.ConnectStatus local_connect_status) {
        UdpMsg msg = new UdpMsg(UdpMsg.MsgType.Input);

        if(pending_output.front() != null) {
            GameInput current = pending_output.front();
            msg.payload.input.connect_status.disconnected = local_connect_status.disconnected;
            msg.payload.input.connect_status.last_frame = local_connect_status.last_frame;
            msg.payload.input.input = current.getInput();
            msg.payload.input.frame = current.getFrame();
            pending_output.pop();
        } else {
            msg.payload.input.frame = 0;
        }
        // TODO: perhaps handle acked frames
        // TODO: definitely update connect status

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

    public void setLocalFrameNumber(int local_frame) {
        if(last_received_input != null) {
            int remoteFrame =
                    (int)(last_received_input.getFrame() +
                            (round_trip_time * 60 / 1000));
            local_frame_advantage = remoteFrame - local_frame;
        }
    }

    public int recommendFrameDelay() {
        return timeSync.recommend_frame_wait_duration(false);
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
    public Running running;
    public State() {
        sync = new Sync();
        running = new Running();
    }

    public class Sync {
        public int round_trips_remaining;
        public int random;
    }

    public class Running {
        public long last_quality_report_time;
        public long last_network_stats_interval;
        public long last_input_packed_recv_time;
    }
}

enum ConnectState {
    Syncing, Synchronized, Running, Disconnected
}