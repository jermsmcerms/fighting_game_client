package com.rose.tests;

import com.rose.ggpo.GGPOEventCode;
import com.rose.ggpo.GameInput;
import com.rose.ggpo.GgpoEvent;
import com.rose.ggpo.Poll;
import com.rose.ggpo.Sync;
import com.rose.network.ConnectState;
import com.rose.network.Udp;
import com.rose.network.UdpMsg;
import com.rose.network.UdpProto;
import com.rose.network.UdpProtocolEvent;

import java.io.IOException;
import java.net.SocketAddress;

public class DummyClient extends Udp.Callbacks {
    private static final String SERVER_IP = "192.168.80.194";
    private static final int PORT_NUM = 1234;
    private static final int RECOMMENDATION_INTERVAL = 240000000;
    private final Poll poll;
    private final UdpProto server_endpoint;
    private final Sync sync;
    private boolean synchronizing;
    private final UdpMsg.ConnectStatus[] local_connect_status;
    private boolean connecting;
    private int next_recommended_sleep;

    public DummyClient() throws IOException {
        Udp udp = new Udp(this);
        poll = udp.getPoll();
        server_endpoint = new UdpProto(udp, poll, SERVER_IP, PORT_NUM);
        sync = new Sync();
        local_connect_status = new UdpMsg.ConnectStatus[2];
        for(int i = 0; i < local_connect_status.length; i++) {
            local_connect_status[i] = new UdpMsg.ConnectStatus();
            local_connect_status[i].last_frame = -1;
            local_connect_status[i].disconnected = false;
        }
        connecting = true;
    }

    public void incrementFrame() {
        doPoll();
        sync.incrementFrame();
    }

    public void doPoll() {
        poll.pump(0);
        processUdpProtocolEvents();
        if(!sync.isInRollback()) {
            sync.checkSimulation(0);
            if (!connecting && !synchronizing) {
                int current_frame = sync.frame_count;
                server_endpoint.setLocalFrameNumber(current_frame);

                int total_min_confirmed = poll2players(current_frame);
                if (total_min_confirmed >= 0) {
                    assert (total_min_confirmed != Integer.MAX_VALUE);
                    System.out.println("setting confirmed frame in sync to: " +
                            total_min_confirmed);
                    sync.setLastConfirmedFrame(total_min_confirmed);
                }

                if (current_frame > next_recommended_sleep) {
                    int interval =
                            Math.max(0, server_endpoint.recommendFrameDelay());

                    if (interval > 0) {
                        System.out.println("try to sleep for: " +
                                (1000000000 * interval / 60) + " frames");
                        next_recommended_sleep =
                                current_frame + RECOMMENDATION_INTERVAL;
                    }
                }
            }
        }
    }

    private int poll2players(int current_frame) {
        int total_min_confirmed = Integer.MAX_VALUE;
        for(int i = 0; i < local_connect_status.length; i++) {
            // TODO: Get peer connect status and disconnect if needed.
            if(!local_connect_status[i].disconnected) {
                total_min_confirmed =
                    Math.min(local_connect_status[i].last_frame,
                    total_min_confirmed);
            }
        }
        return total_min_confirmed;
    }

    public void addLocalInput(int frame, int input) {
        if(synchronizing) {
            System.out.println("synchronizing");

            return;
        }

        if(server_endpoint.getPlayerNumber() < 0 || server_endpoint.getPlayerNumber() > 2) {
            System.out.println("invalid player");

            return;
        }


        if(!sync.addLocalInput(0, input)) {
            System.out.println("Failed to add local input");
            return;
        }

        GameInput gameInput = new GameInput(sync.frame_count, input);
        if(gameInput.getFrame() != GameInput.NULL_FRAME) {
            local_connect_status[0].last_frame = gameInput.getFrame();
            server_endpoint.sendInput(gameInput, local_connect_status[0]); // <-- TODO: Remove when passing local connect status is necessary
        }
    }

    public ConnectState getCurrentStatus() {
        return server_endpoint.getConnectState();
    }
    @Override
    public void onMsg(SocketAddress from, UdpMsg msg) {
        server_endpoint.onMsg(msg);
    }

    public void processUdpProtocolEvents() {
        UdpProtocolEvent event = server_endpoint.getEvent();
        GgpoEvent ggpoEvent;
        while(event != null) {
            switch(event.getEventType()) {
                case Connected:
                    if( server_endpoint.getConnectState() == ConnectState.Connected &&
                        server_endpoint.getConnectState() != ConnectState.Syncing) {
                        server_endpoint.synchronize();
                        synchronizing = true;
                        connecting = false;
                    }
                    break;
                case Synchronized:
                    ggpoEvent = new GgpoEvent(GGPOEventCode.GGPO_EVENTCODE_SYNCHRONIZED_WITH_SERVER);
                    ggpoEvent.synced.player_handle = server_endpoint.getPlayerNumber();
                    checkInitialSync();
                    break;
                case Input:
                    // TODO: Don't send ggpo event. Add input to the sync's
                    //       input queue if now is a good time.
                    if(!local_connect_status[1].disconnected) {
                        int current_frame = local_connect_status[1].last_frame;
                        int new_remote = event.input.input.getFrame();
                        System.out.println(
                            "current frame = " + current_frame +
                            " new remote frame: " + new_remote
                        );
                        sync.addRemoteInput(1, event.input.input);
                        local_connect_status[1].last_frame =
                            event.input.input.getFrame();
                    }
                    break;
            }

            // TODO: process the event now.
            event = server_endpoint.getEvent();
        }
    }

    private void checkInitialSync() {
        if(synchronizing) {
            if(server_endpoint.isInitialized() && !server_endpoint.isSynchronized() && !local_connect_status[1].disconnected) {
                return;
            }
        }

//        GgpoEvent event = new GgpoEvent(GGPOEventCode.GGPO_EVENTCODE_RUNNING);
        // TODO: handle first running event
        synchronizing = false;
    }
}
