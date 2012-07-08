package com.javafx.experiments.scenicview.connector.helper;

public abstract class WorkerThread extends Thread {

    protected boolean running = true;
    protected final int sleepTime;

    public WorkerThread(final String name, final int sleepTime) {
        super(name);
        this.sleepTime = sleepTime;
    }

    public void finish() {
        this.running = false;
        interrupt();
    }

    @Override public void run() {
        long sleepTime = 0;
        while (running) {
            try {
                Thread.sleep(sleepTime);
                work();
            } catch (final Exception e) {
                e.printStackTrace();
            }
            sleepTime = this.sleepTime;
        }
    }

    protected abstract void work();
}
