package com.rose.network;

import com.rose.ggpo.GGPOErrorCode;
import com.rose.ggpo.GGPOEventCode;
import com.rose.ggpo.GGPONetworkStatus;
import com.rose.ggpo.GameInput;
import com.rose.ggpo.GgpoEvent;
import com.rose.ggpo.Poll;
import com.rose.ggpo.Sync;
import com.rose.screens.ApplicationScreen;
import com.rose.tests.SendInputTest;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class Client extends Udp.Callbacks {
    private static final String SERVER_IP = "192.168.80.194";
    private static final int PORT_NUM = 1234;
    private static final int RECOMMENDATION_INTERVAL = 240000000;
    private Poll poll;
    private UdpProto server_endpoint;
    private Sync sync;
    private boolean synchronizing;
    private UdpMsg.ConnectStatus[] local_connect_status;
    private boolean connecting;
    private int next_recommended_sleep;
    private ApplicationScreen callbacks;

    public Client() {
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Client(ApplicationScreen callbacks) throws IOException {
        this.callbacks = callbacks;
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() throws IOException {
        Udp udp = new Udp(this);
        poll = udp.getPoll();
        server_endpoint = new UdpProto(udp, poll, SERVER_IP, PORT_NUM);
        sync = new Sync();
        sync.setCallbacks(this.callbacks);
        sync.setFrameDelay(0, 2);
        local_connect_status = new UdpMsg.ConnectStatus[2];
        for(int i = 0; i < local_connect_status.length; i++) {
            local_connect_status[i] = new UdpMsg.ConnectStatus();
            local_connect_status[i].last_frame = -1;
            local_connect_status[i].disconnected = false;
        }
        connecting = true;
    }

    public void incrementFrame() {
        doPoll(0);
        sync.incrementFrame();
    }

    public void doPoll(long timeout) {
        if(!sync.isInRollback()) {
            poll.pump(0);
            processUdpProtocolEvents();
            if (!connecting && !synchronizing) {
                sync.checkSimulation(0);
                int current_frame = sync.frame_count;
                server_endpoint.setLocalFrameNumber(current_frame);

                int total_min_confirmed = poll2players(current_frame);
                if (total_min_confirmed >= 0) {
                    assert (total_min_confirmed != Integer.MAX_VALUE);
                    sync.setLastConfirmedFrame(total_min_confirmed);
                }

                if (current_frame > next_recommended_sleep) {
                    int interval =
                        Math.max(0, server_endpoint.recommendFrameDelay());

                    if (interval > 0) {
                        GgpoEvent event = new GgpoEvent(GGPOEventCode.GGPO_EVENTCODE_TIMESYNC);
                        event.timeSync.frames_ahead = interval;
                        callbacks.onEvent(event);
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

    public GGPOErrorCode addLocalInput(int input) {
        if(sync.isInRollback()) {
            System.out.println("in rollback");
            return GGPOErrorCode.GGPO_ERRORCODE_IN_ROLLBACK;
        }
        if(synchronizing) {
            System.out.println("synchronizing");

            return GGPOErrorCode.GGPO_ERRORCODE_NOT_SYNCHRONIZED;
        }

        if(server_endpoint.getPlayerNumber() < 0 || server_endpoint.getPlayerNumber() > 2) {
            System.out.println("invalid player");

            return GGPOErrorCode.GGPO_ERRORCODE_INVALID_PLAYER_HANDLE;
        }


        if(!sync.addLocalInput(0, input)) {
            System.out.println("Prediction threshold reached");
            return GGPOErrorCode.GGPO_ERRORCODE_PREDICTION_THRESHOLD;
        }

        GameInput gameInput = new GameInput(sync.frame_count, input);
        if(gameInput.getFrame() != GameInput.NULL_FRAME) {
            local_connect_status[0].last_frame = gameInput.getFrame();
            server_endpoint.sendInput(gameInput, local_connect_status[0]); // <-- TODO: Remove when passing local connect status is necessary
        }

        return GGPOErrorCode.GGPO_OK;
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
            if(server_endpoint.isInitialized() &&
                !server_endpoint.isSynchronized() &&
                !local_connect_status[1].disconnected) {
                return;
            }
        }

//        GgpoEvent event = new GgpoEvent(GGPOEventCode.GGPO_EVENTCODE_RUNNING);
        // TODO: handle first running event
        synchronizing = false;
    }

    public int[] syncInput() {
        return sync.syncInputs();
    }

    public GGPONetworkStatus getNetworkStats() {
        return server_endpoint.getNetworkStats();
    }

    public void setCallbacks(ApplicationScreen applicationScreen) {
        callbacks = applicationScreen;
        sync.setCallbacks(applicationScreen);
    }

    public int getPlayerNumber() {
        if(server_endpoint.getPlayerNumber() == 1) {
            return 2;
        }
        return 1;
    }
}
