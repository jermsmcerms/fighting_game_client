package com.rose.ggpo;

public class GGPONetworkStatus {
    public Network network;
    public TimeSync timesync;

    public GGPONetworkStatus() {
        network = new Network();
        timesync = new TimeSync();
    }
    public static class Network {
        public int send_queue_length;
        public int recv_queue_length;
        public long ping;
        public int kbps_sent;
    }

    public static class TimeSync {
        public int local_frames_behind;
        public int remote_frames_behind;
    }
}
