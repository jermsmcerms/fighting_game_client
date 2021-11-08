//package com.rose.tests;
//
//import com.badlogic.gdx.Gdx;
//import com.rose.network.Client;
//import com.rose.network.ConnectState;
//import com.rose.network.UdpProto;
//
//import java.io.IOException;
//
///*
//    This test should demonstrate an ability to synchronized
//    with another player.
//    The test will pass when two players send and receive all synchronization packets.
//    The test has no fail conditions. However, will run until it passes.
// */
//public class NetworkSyncTest {
//    private boolean connect_reply_received;
//    private TestGame callbacks;
//    private Client client;
//
//    public NetworkSyncTest(TestGame callbacks) {
//        this.callbacks = callbacks;
//    }
//
//    public void runSyncTest() {
//        try {
//            client = new Client();
//            long now, next;
//            next = System.nanoTime();
//
//            System.out.println("Begin network sync test now...");
//            client.connect();
//            while (true) {
//                now = System.nanoTime();
//                doPoll(Math.max(0, next - now - 1));
//                if (now >= next) {
//                    callbacks.runConnectRequestFrame();
//                    next = now + (1000000000L / 60);
//                    connect_reply_received = client.getServerConnectState() == ConnectState.Running;
//                }
//            }
//
////            for(UdpProto endpoint : client.getEndpoints()) {
////                if(endpoint != null) {
////                    System.out.println("remote magic number " + endpoint.getRemoteMagic());
////                }
////            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void doPoll(long timeout) throws IOException {
//        if(client != null) {
//            client.doPoll(timeout);
//        }
//    }
//}
