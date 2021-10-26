package com.rose.network;

import com.rose.ggpo.IPollSink;
import com.rose.ggpo.Poll;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class  Udp implements IPollSink {
    private final Poll poll;
    private final DatagramChannel dgc;
    private final Udp.Callbacks cb;
    private final SocketAddress server_addr;
    private final ByteBuffer recv_buffer;

    public Udp(Udp.Callbacks cb) throws IOException {
        this.cb = cb;
        poll = new Poll();
        poll.registerLoop(this);

        dgc = DatagramChannel.open();
        dgc.configureBlocking(false);
        dgc.socket().bind(null);

        server_addr = new InetSocketAddress("192.168.80.194", 1234);
        recv_buffer = ByteBuffer.allocate(UdpMsg.MAX_COMPRESSED_BITS);
    }

    public Poll getPoll() { return poll; }

    public void sendTo(UdpMsg msg, SocketAddress dst) {
        try {
            dgc.send(msg.getBuffer(), dst); }
        catch(IOException e) { e.printStackTrace(); }
    }

    public UdpMsg receiveMsg() throws IOException {
        recv_buffer.clear();
        SocketAddress server_addr = dgc.receive(recv_buffer);
        if (server_addr == null) {
            return null;
        }
        return new UdpMsg(recv_buffer);
    }

    public static abstract class Callbacks {
        public abstract void onMsg(SocketAddress from, UdpMsg msg);
    }

    //#region IPollSink Implementation
    @Override public boolean onLoopPoll(Object o) {
        boolean wasMsgReceived = true;
        while(wasMsgReceived) {
            UdpMsg msg = null;

            try { msg = receiveMsg(); }
            catch(IOException e) { e.printStackTrace(); }

            if(msg == null) { wasMsgReceived = false; }
            else {
                cb.onMsg(server_addr, msg);
            }
        }
        return true;
    }
    //#endregion
}
