package com.rose.tests;

import com.rose.ggpo.GGPOEventCode;
import com.rose.ggpo.GameInput;
import com.rose.ggpo.GgpoEvent;
import com.rose.ggpo.Poll;
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
    private final Poll poll;
    private final UdpProto server_endpoint;
    private boolean synchronizing;
    private final UdpMsg.ConnectStatus[] local_connect_status;

    public DummyClient() throws IOException {
        Udp udp = new Udp(this);
        poll = udp.getPoll();
        server_endpoint = new UdpProto(udp, poll, SERVER_IP, PORT_NUM);
        local_connect_status = new UdpMsg.ConnectStatus[2];
        for(int i = 0; i < local_connect_status.length; i++) {
            local_connect_status[i] = new UdpMsg.ConnectStatus();
            local_connect_status[i].last_frame = -1;
            local_connect_status[i].disconnected = false;
        }
    }

    public void incrementFrame() {
        doPoll();
    }

    public void doPoll() {
        poll.pump(0);
        processUdpProtocolEvents();
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

        GameInput gameInput = new GameInput(frame, input);
        if(gameInput.getFrame() != GameInput.NULL_FRAME) {
            local_connect_status[0].last_frame = gameInput.getFrame();
            server_endpoint.sendInput(gameInput); // <-- TODO: Remove when passing local connect status is necessary
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
