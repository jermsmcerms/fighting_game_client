package com.rose.management;

public class SaveGameState {
    public byte[] obj_data;
    public String checksum;

    public SaveGameState(byte[] obj_data, String checksum) {
        this.obj_data = obj_data;
        this.checksum = checksum;
    }
}
