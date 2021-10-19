package com.rose.data;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.util.HashMap;

public class BoxSizes {
    private HashMap<String, int[][]> ryu_box_data;

    public BoxSizes() {
        ryu_box_data = new HashMap<>();
        int[][] idle_box_data = new int[][]{
            {47, 34, -25, -22, 0}, // foot
            {44, 46, -22, -22, 34}, // chest
            {20, 20, -4, -15, 71}, // head
        };
        ryu_box_data.put("idle_walk", idle_box_data);
    }

    public HashMap<String, int[][]> getBoxData() {
        return ryu_box_data;
    }
}
