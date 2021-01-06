package de.nkilders.astar;

public class Timer {
    private long last;

    public Timer() {
        reset();
    }

    public boolean hasReached(long l) {
        return currentMS() - last >= l;
    }

    public void reset() {
        last = currentMS();
    }

    private long currentMS() {
        return System.nanoTime() / 1000000L;
    }
}