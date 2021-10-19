package com.rose.network;

import com.rose.ggpo.GGPOEventCode;
import com.rose.ggpo.GameInput;
import com.rose.ggpo.GgpoEvent;
import com.rose.ggpo.Poll;
import com.rose.ggpo.Sync;
import com.rose.screens.MainScreen;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;

public class Client extends Udp.Callbacks {
    private static final String SERVER_IP = "192.168.80.194";
    private static final int RECOMMENDATION_INTERVAL = 240;
    private static final int PORT_NUM = 1234;
    private final DatagramChannel dgc;
    private final ByteBuffer buffer;
    private final int disconnect_timeout = 3000;
    private final int disconnect_notify_start = 100;
    public UdpProto local_udp_endpoint;
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
        dgc = DatagramChannel.open();
        dgc.configureBlocking(false);
        dgc.socket().bind(null);

        buffer = ByteBuffer.allocate(32);

        sync = new Sync();
        synchronizing = true;
        allConnected = false;
        udp = new Udp(PORT_NUM, this);
        poll = udp.getPoll();

        local_udp_endpoint = new UdpProto(udp, poll, SERVER_IP,
                PORT_NUM, disconnect_timeout, disconnect_notify_start);

        local_connect_status = new UdpMsg.ConnectStatus();
        remote_connect_status = new UdpMsg.ConnectStatus();
    }

    public int getPlayerNumber() {
        if(local_udp_endpoint != null) {
            return local_udp_endpoint.getPlayerNumber();
        }
        return 0;
    }

    public void doPoll(long timeout) throws IOException {
        if(!sync.isInRollback()) {
            if(poll != null) { poll.pump(0); }

            pollUdpProtocolEvents();

            if(local_udp_endpoint.getCanStart()) {
                int current_frame = sync.frame_count;
                local_udp_endpoint.setLocalFrameNumber(current_frame);
                sync.checkSimulation(timeout);
                int total_min_confirmed = poll2Players();
                if(total_min_confirmed >= 0) {
                    sync.setLastConfirmedFrame(total_min_confirmed);
                }

                // TODO: send time sync notifications if its proper to do so.
                if(current_frame > next_recommended_sleep) {
                    int recommend_frame_delay = local_udp_endpoint.recommendFrameDelay();
                    int interval = Math.max(0, recommend_frame_delay);
                    if(interval > 0) {
                        GgpoEvent event = new GgpoEvent(GGPOEventCode.GGPO_EVENTCODE_TIMESYNC);
                        event.timeSync.frames_ahead = interval;
                        callbacks.onEvent(event);
                        next_recommended_sleep = current_frame + RECOMMENDATION_INTERVAL;
                    }
                }
            }
        }
    }

    private void pollUdpProtocolEvents() {
        UdpProtocolEvent event = local_udp_endpoint.getEvent();
        while(event != null) {
            processPeerEvent(event);
            event = local_udp_endpoint.getEvent();
        }
    }

    private void processPeerEvent(UdpProtocolEvent event) {
        switch(event.getEventType()) {
            case Input:
                if(!local_connect_status.disconnected) {
                    sync.addRemoteInput(1, event.input.input);
                    if(remote_connect_status == null) {
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

        if(!local_udp_endpoint.getCanStart()) {
            return false;
        }

        gameInput = new GameInput(-1, input);
        if(!sync.addLocalInput(0, gameInput)) {
            return false;
        }
        gameInput = sync.input_queues.get(0).getCurrentInput();
        if(gameInput.getFrame() != GameInput.NULL_FRAME) {
            local_connect_status.last_frame = gameInput.getFrame();
            local_connect_status.disconnected = false;
            local_udp_endpoint.sendInput(gameInput, local_connect_status);
        }
        return true;
    }

    public boolean getAllConnected() {
        return local_udp_endpoint.getCanStart();
    }

    private int poll2Players() {
        int total_min_confirmed = Integer.MAX_VALUE;
//        if(!local_connect_status.disconnected) {
//            total_min_confirmed = local_connect_status.last_frame;
//        }
        if(local_udp_endpoint.getRemoteStatus() != null) {
            remote_connect_status =
                new UdpMsg.ConnectStatus(
                    local_udp_endpoint.getRemoteStatus().disconnected,
                    local_udp_endpoint.getRemoteStatus().last_frame);

            total_min_confirmed = Math.min(
                    remote_connect_status.last_frame,
                    total_min_confirmed);
            return total_min_confirmed;
        }
        return -1;
    }

    public void onMsg(SocketAddress from, UdpMsg msg) {
        local_udp_endpoint.onMsg(msg);
    }

    public int[] syncInput() {
        // TODO: return null if synchronizing
        return sync.syncInputs();
    }

    public void incrementFrame() throws IOException {
        sync.incrementFrame();
        doPoll(0);
    }

    public void setCallbacks(MainScreen screen) {
        this.callbacks = screen;
        sync.setCallbacks(callbacks);
    }

    public void setPlayerNumber() {
        if(local_udp_endpoint != null) {
            playerNumber = local_udp_endpoint.getPlayerNumber();
        }
    }
}
