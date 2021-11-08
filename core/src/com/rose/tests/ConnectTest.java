//package com.rose.tests;
//
//import com.badlogic.gdx.Gdx;
//import com.rose.network.Client;
//import com.rose.network.ConnectState;
//
//import java.io.IOException;
//
///*
//    This test is to demonstrate the ability for a client to connect to a server.
//    This test will run until stopped if no server exists.
//    This test will pass and quit once it receives a connection reply message from the server.
//    The test "fails" if it never stops.
// */
//public class ConnectTest {
//    private boolean connect_reply_received;
//    private TestGame callbacks;
//    private Client client;
//
//    public ConnectTest(TestGame callbacks) {
//        this.callbacks = callbacks;
//    }
//
//    public void runConnectTest() {
//        try {
//            client = new Client();
//            long now, next;
//            next = System.currentTimeMS();
//
//            System.out.println("Begin connect test now...");
//            client.connect();
//            while (!connect_reply_received) {
//                now = System.currentTimeMillis();
//                doPoll(Math.max(0, next - now - 1));
//                if (now >= next) {
//                    callbacks.runConnectRequestFrame();
//                    next = now + (1000 / 60);
//                    System.out.println("connect state " + client.getServerConnectState());
//                    connect_reply_received = client.getServerConnectState() == ConnectState.Connected;
//                }
//            }
//            System.out.println("Connection test passed. Exiting.");
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
