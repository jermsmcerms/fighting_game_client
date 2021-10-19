package com.rose.ggpo;

import com.rose.network.UdpMsg;

public class GameInput {
    public static final int NULL_FRAME = -1;
    public static final int INPUT_MAX_BYTES = 9;
    public static final int INPUT_MAX_PLAYERS = 2;

    private int frame;
    private int input;
    public UdpMsg.ConnectStatus connectStatus;

    public GameInput(int frame, int input) {
        this.frame = frame;
        this.input = input;
    }

    public int getInput() {
        return input;
    }

    public void setFrame(int frame) {
        this.frame = frame;
    }

    public int getFrame() { return frame; }

    public void setInput(int input) {
        this.input = input;
    }
}
