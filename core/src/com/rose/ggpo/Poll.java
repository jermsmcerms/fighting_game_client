package com.rose.ggpo;

import java.util.HashMap;
import java.util.Iterator;

public class Poll implements IPollSink {
    private static final int INFINITE = Integer.MAX_VALUE;
    private static final int MAX_POLLABLE_HANDLES = 64;
    private long startTime;
    private int handleCount;
    private Object[] handles;
    private final StaticBuffer<PollSinkCb> loopSinks;
    private final StaticBuffer<PollSinkCb> msgSinks;
    private final StaticBuffer<PollPeriodicSinkCb> periodicSinks;

    public Poll() {
        startTime = 0;
        handleCount = 0;
        handles = new Object[MAX_POLLABLE_HANDLES];
        // TODO: may need a dummy handle?
        loopSinks = new StaticBuffer<PollSinkCb>(16);
        msgSinks = new StaticBuffer<PollSinkCb>(16);
        periodicSinks = new StaticBuffer<PollPeriodicSinkCb>(16);
    }

    public void registerLoop(IPollSink sink) {
        registerLoop(sink, null);
    }

    public void registerLoop(IPollSink sink, Object cookie) {
        loopSinks.push_back(new PollSinkCb(sink, cookie));
    }

    public void unRegisterLoop(Object cookie) {
        loopSinks.removeIf(next -> next.cookie.equals(cookie));
        loopSinks.updateSize(-1);
    }

    // TODO: pump all of the sinks and returned when the pump has finished.
    public boolean pump(int timeout) {
        int i, res;
        boolean finished = false;

        if(startTime == 0) {
            startTime = System.currentTimeMillis();
        }

        long elapsed = System.currentTimeMillis() - startTime;
        int maxwait = computeWaitTime(elapsed);
        if(maxwait != Integer.MAX_VALUE) {
            timeout = Math.min(timeout, maxwait);
        }

        // TODO: do something about handle sinks idk what yet because GGPO uses WaitForMultipleObjects whiche is Win32 exclusive.

        for(i = 0; i < msgSinks.size(); i++) {
            PollSinkCb cb = msgSinks.get(i);
            finished = !cb.sink.onMsgPoll(cb.cookie) || finished;
        }

        for(i = 0; i < periodicSinks.size(); i++) {
            PollPeriodicSinkCb cb = periodicSinks.get(i);
            if(cb.interval + cb.lastFired <= elapsed) {
                cb.lastFired = (int) ((elapsed / cb.interval) * cb.interval);
                finished = !cb.sink.onPeriodicPoll(cb.cookie, cb.lastFired) || finished;
            }
        }

        for(i = 0; i < loopSinks.size(); i++) {
            PollSinkCb cb = loopSinks.get(i);
            if(cb.cookie == null) { cb.cookie = new Object(); }
            finished = !cb.sink.onLoopPoll(cb.cookie) || finished;
        }

        return finished;
    }

    private int computeWaitTime(long elapsed) {
        int waitTime = INFINITE;
        int count = periodicSinks.size();
        if(count > 0) {
            for(int i = 0; i < count; i++) {
                PollPeriodicSinkCb cb = periodicSinks.get(i);
                int timeout = (int) ((cb.interval + cb.lastFired) - elapsed);
                if(waitTime == INFINITE || (timeout < waitTime)) {
                    waitTime = Math.max(timeout, 0);
                }
            }
        }
        return waitTime;
    }
}

class PollSinkCb {
    IPollSink sink;
    Object cookie;

    public PollSinkCb() {
        sink = null;
        cookie = null;
    }

    public PollSinkCb(IPollSink s, Object c) {
        sink = s;
        cookie = c;
    }
}

class PollPeriodicSinkCb extends PollSinkCb {
    public int interval;
    public int lastFired;
    public PollPeriodicSinkCb() {
        super(null, null);
        interval = 0;
        lastFired = 0;
    }

    public PollPeriodicSinkCb(IPollSink s, Object c, int i) {
        super(s, c);
        interval = i;
        lastFired = 0;
    }
}
