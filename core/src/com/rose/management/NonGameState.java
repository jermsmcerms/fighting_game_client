package com.rose.management;

public class NonGameState {
    public ChecksumInfo now;
    public ChecksumInfo periodic;
    public PlayerConnectionInfo pci;
    public PlayerConnectState state;

    public NonGameState() {
        now = new ChecksumInfo();
        periodic = new ChecksumInfo();
        pci = new PlayerConnectionInfo();
    }

    public void setConnectState(int playerHandle, PlayerConnectState synchronizing) {
        // set the player number to the provided state...
    }

    public enum PlayerConnectState {
        CONNECTING, SYNCHRONIZING, RUNNING, DISCONNECTED, DISCONNECTING
    }

    public static class PlayerConnectionInfo {
        int connect_progress;
        int disconnect_timeout;
        int disconnect_start;
    }

    public static class ChecksumInfo {
        public int frameNumber;
        public String checksum;
    }
}
