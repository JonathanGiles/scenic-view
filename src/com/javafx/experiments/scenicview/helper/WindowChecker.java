package com.javafx.experiments.scenicview.helper;

import java.util.*;

import javafx.stage.Window;

public abstract class WindowChecker extends Thread {

    private boolean running = true;
    private final long sleepTime = 1000; // 1 sec
    private long maxWaitTime = -1;
    private final WindowFilter filter;
    private boolean verbose = false;
    
    public WindowChecker(final WindowFilter filter) {
        this.filter = filter;
    }

    public void finish() {
        this.running = false;
    }
    
    public void verbose() {
        verbose = true;
    }
    
    public interface WindowFilter {
        public boolean accept(Window window);
    }

    @Override public void run() {
        // Keep iterating until we have a any windows.
        // If we past the maximum wait time, we'll exit
        long currentWait = -1;
        List<Window> windows = getValidWindows(filter);
        while (running) {
            onWindowsFound(windows);
            try {
                if(verbose) {
                    System.out.println("No JavaFX window found - sleeping for " + sleepTime / 1000 + " seconds");
                }
                Thread.sleep(sleepTime);
                if(maxWaitTime != -1) {
                    currentWait += sleepTime;
                }

                if (currentWait > maxWaitTime) {
                    finish();
                }
            } catch (final InterruptedException ex) {
                ex.printStackTrace();
            }

            windows = getValidWindows(filter);
        }

    }
    
    protected abstract void onWindowsFound(List<Window> windows);
    
    public static List<Window> getValidWindows(final WindowFilter filter) {
        @SuppressWarnings("deprecation")
        final Iterator<Window> windows = Window.impl_getWindows();
        if(! windows.hasNext()) return Collections.emptyList();
        final List<Window> list = new ArrayList<Window>();
        while (windows.hasNext()) {
            final Window window = windows.next();
            if(filter.accept(window)) {
                list.add(window);
            }
        }
        return list;
    }

    public void setMaxWaitTime(final long maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }
    
}
