package com.rose.ggpo;

public interface GgpoCallbacks {
    boolean beginGame(String name);
    byte[] saveGameState();
    boolean loadFrame(byte[] buffer, int length);
    boolean logGameState(String filename, String buffer);
    Object freeBuffer(Object buffer); // <--- probably don't need cos java
    boolean advanceFrame(int flags);
    boolean onEvent(GgpoEvent event);
}
