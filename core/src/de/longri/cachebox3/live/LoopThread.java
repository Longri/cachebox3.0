package de.longri.cachebox3.live;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LoopThread {
    private static final Logger log = LoggerFactory.getLogger(LoopThread.class);
    private final long sleepTime;
    private boolean loopShouldRun;
    private Thread loopThread;
    private Thread monitoringThread;

    public LoopThread(long LoopBreakTime) {
        super();
        sleepTime = LoopBreakTime;
        loopShouldRun = false;
    }

    protected abstract void loop();

    protected abstract boolean cancelLoop();

    public void start() {
        if (loopThread == null) {

            loopThread = new Thread(() -> {
                do {
                    loopShouldRun = true;
                    if (cancelLoop()) {
                        loopShouldRun = false;
                        loopThread = null;
                    }
                    else {
                        loop();
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException ignored) {
                        }
                    }
                } while (loopShouldRun);
                if (monitoringThread != null) monitoringThread.interrupt();
                monitoringThread = null;
                log.debug("Stop loopThread");
            });

            try {
                loopThread.start();
                // wait until loopThreads runnable is started
                do {
                    Thread.sleep(1000);
                } while (!loopShouldRun);

                if (monitoringThread == null) {

                    monitoringThread = new Thread(() -> {
                        do {
                            if (loopShouldRun) {
                                log.debug("MonitoringThread is checking!");
                                if (loopThread != null) {
                                    if (loopThread.isAlive()) {
                                        try {
                                            Thread.sleep(10000); // must not run that often
                                        } catch (InterruptedException ignored) {
                                            log.debug("Waking up monitoringThread");
                                        }
                                    } else {
                                        // both threads will finish
                                        loopShouldRun = false;
                                        loopThread = null;
                                        monitoringThread = null;
                                        start(); // restarts both (if loop() is hanging
                                    }
                                }
                            }
                        } while (loopShouldRun);
                        log.debug("Stop monitoringThread");
                    });

                    monitoringThread.setPriority(Thread.MIN_PRIORITY);
                    monitoringThread.start();
                }
            } catch (Exception ex) {
                log.error("monitoringThread: " + ex);
            }

        }
    }

}