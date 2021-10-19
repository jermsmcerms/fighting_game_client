package com.rose.network;

import com.rose.ggpo.RingBuffer;

public class Endpoint {
    public RingBuffer<Integer> input;
    public int[] input_queue;
//    public RingBuffer<input> input;
    public int firstIncorrectFrame = -1;
    public int prediction = -1;
    public int last_added_frame = -1;

    public Endpoint() {
        input = new RingBuffer<>(128);
        input_queue = new int[128];
    }
    public void addInput(int input, int frame_number) {

    }

}
