package de.nkilders.astar;

public class Timer {
    private long startTime;

    public Timer() {
        reset();
    }

    public boolean hasReached(long time) {
        return currentTime() - startTime >= time;
    }

    public void reset() {
        startTime = currentTime();
    }

    private long currentTime() {
        return System.nanoTime() / 1000000L;
    }
}