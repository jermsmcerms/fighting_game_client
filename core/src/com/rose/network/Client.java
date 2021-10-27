package com.rose.network;

import com.rose.ggpo.GameInput;
import com.rose.ggpo.Poll;
import com.rose.ggpo.Sync;
import com.rose.screens.MainScreen;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class Client extends Udp.Callbacks {
    private static final String SERVER_IP = "192.168.80.194";
    private static final int RECOMMENDATION_INTERVAL = 240;
    private static final int PORT_NUM = 1234;
    private final ByteBuffer buffer;
    private final long disconnect_timeout = 30000000000L;
    private final long disconnect_notify_start = 10000000000L;
    public UdpProto server_end_point;
    public UdpProto[] endpoints;
    private MainScreen callbacks;
    private boolean synchronizing;
    private boolean allConnected;
    private int playerNumber;
    private final Sync sync;
    private int sequence = -1;
    private Udp udp;
    private Poll poll;
    private int next_recommended_sleep;
    private UdpMsg.ConnectStatus local_connect_status;
    private UdpMsg.ConnectStatus remote_connect_status;

    public Client() throws IOException {
        buffer = ByteBuffer.allocate(32);

        sync = new Sync();
        synchronizing = true;
        allConnected = false;
        udp = new Udp(this);
        poll = udp.getPoll();

        server_end_point = new UdpProto(udp, poll, SERVER_IP,
                PORT_NUM);

        local_connect_status = new UdpMsg.ConnectStatus();
        remote_connect_status = new UdpMsg.ConnectStatus();
        endpoints = new UdpProto[2];
    }

    public void connect() {
        if(server_end_point != null) {
            server_end_point.sendConnectRequest();
        }
    }

    public ConnectState getServerConnectState() {
        if(playerNumber <= 0 || endpoints[playerNumber - 1] == null) {
            return server_end_point.getConnectState();
        }

        if(endpoints[playerNumber - 1] != null) {
            return endpoints[playerNumber - 1].getConnectState();
        }

        return ConnectState.Invalid;
    }

    public UdpProto[] getEndpoints() {
        return endpoints;
    }

    public int getPlayerNumber() {
        if(server_end_point != null) {
            return server_end_point.getPlayerNumber();
        }
        return 0;
    }

    public void doPoll(long timeout) throws IOException {
        if(!sync.isInRollback()) {
            if(poll != null) { poll.pump(0); }

            pollUdpProtocolEvents();

//            if(server_end_point.getCanStart()) {
//                int current_frame = sync.frame_count;
//                server_end_point.setLocalFrameNumber(current_frame);
//                sync.checkSimulation(timeout);
//                int total_min_confirmed = poll2Players();
//                if(total_min_confirmed >= 0) {
//                    sync.setLastConfirmedFrame(total_min_confirmed);
//                }
//
//                // TODO: send time sync notifications if its proper to do so.
//                if(current_frame > next_recommended_sleep) {
//                    int recommend_frame_delay = server_end_point.recommendFrameDelay();
//                    int interval = Math.max(0, recommend_frame_delay);
//                    if(interval > 0) {
//                        GgpoEvent event = new GgpoEvent(GGPOEventCode.GGPO_EVENTCODE_TIMESYNC);
//                        event.timeSync.frames_ahead = interval;
//                        callbacks.onEvent(event);
//                        next_recommended_sleep = current_frame + RECOMMENDATION_INTERVAL;
//                    }
//                }
//            }
        }
    }

    private void pollUdpProtocolEvents() {
        for(int i = 0; i < endpoints.length; i++) {
            if(endpoints[i] != null) {
                UdpProtocolEvent event = endpoints[i].getEvent();
                while (event != null) {

                    processPeerEvent(event);
                    event = server_end_point.getEvent();
                }
            }
        }
    }

    private void processPeerEvent(UdpProtocolEvent event) {
        if (event.getEventType() == UdpProtocolEvent.Event.Input) {
            if (!local_connect_status.disconnected) {
                sync.addRemoteInput(1, event.input.input);
                if (remote_connect_status == null) {
                    remote_connect_status = new UdpMsg.ConnectStatus();
                }

                remote_connect_status.last_frame =
                        event.input.input.getFrame();
            }
        }
    }

    public boolean addLocalInput(int input) {
        GameInput gameInput;
        if(sync.isInRollback()) {
            return false;
        }

        if(!server_end_point.getCanStart()) {
            return false;
        }

        gameInput = new GameInput(-1, input);
//        if(!sync.addLocalInput(0, gameInput)) {
//            return false;
//        }
        gameInput = sync.input_queues.get(0).getCurrentInput();
        if(gameInput.getFrame() != GameInput.NULL_FRAME) {
            local_connect_status.last_frame = gameInput.getFrame();
            local_connect_status.disconnected = false;
//            server_end_point.sendInput(gameInput, local_connect_status[0]);
        }
        return true;
    }

    public boolean getAllConnected() {
        return server_end_point.getCanStart();
    }

    private int poll2Players() {
        int total_min_confirmed = Integer.MAX_VALUE;
//        if(!local_connect_status.disconnected) {
//            total_min_confirmed = local_connect_status.last_frame;
//        }
        if(server_end_point.getRemoteStatus() != null) {
            remote_connect_status =
                new UdpMsg.ConnectStatus(
                    server_end_point.getRemoteStatus().disconnected,
                    server_end_point.getRemoteStatus().last_frame);

            total_min_confirmed = Math.min(
                    remote_connect_status.last_frame,
                    total_min_confirmed);
            return total_min_confirmed;
        }
        return -1;
    }

    public void onMsg(SocketAddress from, UdpMsg msg) {
        if(playerNumber <= 0 && msg.hdr.type == UdpMsg.MsgType.ConnectReply.ordinal()) {
            server_end_point.onMsg(msg);
            playerNumber = server_end_point.getPlayerNumber();
        } else if(playerNumber > 0 && endpoints[playerNumber - 1] == null) {
            synchronizing = true;
            server_end_point.onMsg(msg);
            server_end_point.synchronize();
            endpoints[playerNumber - 1] = server_end_point;
        } else {
            for (UdpProto endpoint : endpoints) {
                if (endpoint != null) {
                    endpoint.onMsg(msg);
                    return;
                }
            }
        }
    }

    public int[] syncInput() {
        // TODO: return null if synchronizing
        return sync.syncInputs();
    }

    public void incrementFrame() throws IOException {
//        sync.incrementFrame();
        doPoll(0);
    }

    public void setCallbacks(MainScreen screen) {
        this.callbacks = screen;
        sync.setCallbacks(callbacks);
    }

    public void setPlayerNumber() {
        if(server_end_point != null) {
            playerNumber = server_end_point.getPlayerNumber();
        }
    }
}
