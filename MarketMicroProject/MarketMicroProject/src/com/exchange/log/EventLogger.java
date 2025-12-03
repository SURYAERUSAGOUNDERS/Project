package com.exchange.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class EventLogger {

    private final LinkedBlockingQueue<LogEvent> queue;
    private final Thread writerThread;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final File outFile;
    private final long offerTimeoutMillis;
    private final AtomicLong dropped = new AtomicLong(0);

    public EventLogger(String filepath, int cap, long offerTimeoutMillis) {
        this.queue = new LinkedBlockingQueue<LogEvent>(cap);
        this.outFile = new File(filepath);
        this.offerTimeoutMillis = offerTimeoutMillis;

        this.writerThread = new Thread(new Runnable() {
            public void run() {
                runWriter();
            }
        }, "EventLogger-Writer");
        this.writerThread.setDaemon(true);
        this.writerThread.start();
    }

    public boolean publish(LogEvent ev) {
        if (!running.get()) return false;
        try {
            boolean ok = queue.offer(ev, offerTimeoutMillis, TimeUnit.MILLISECONDS);
            if (!ok) {
                dropped.incrementAndGet();
            }
            return ok;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void runWriter() {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(outFile, true))) {
            while (running.get() || !queue.isEmpty()) {
                LogEvent ev = queue.poll(500, TimeUnit.MILLISECONDS);
                if (ev == null) continue;
                try {
                    w.write(ev.toCsv());
                    w.newLine();
                } catch (IOException ignore) {}
                if (queue.isEmpty()) {
                    try {
                        w.flush();
                    } catch (IOException ignore) {}
                }
            }
            LogEvent ev;
            while ((ev = queue.poll()) != null) {
                try {
                    w.write(ev.toCsv());
                    w.newLine();
                } catch (IOException ignore) {}
            }
            try {
                w.flush();
            } catch (IOException ignore) {}
        } catch (Exception ignore) {}
    }

    public void shutdownAndAwait(long wait) {
        running.set(false);
        try {
            writerThread.join(wait);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public long getDropped() {
        return dropped.get();
    }
}
