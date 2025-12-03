package com.exchange.core;

import com.exchange.log.EventLogger;
import com.exchange.model.Order;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MatcherService implements Runnable {

    private final Sequencer sequencer;
    private final EventLogger logger;
    private final Map<String, SymbolMatcher> matchers = new ConcurrentHashMap<String, SymbolMatcher>();
    private volatile boolean running = true;

    public MatcherService(Sequencer sequencer, EventLogger logger) {
        this.sequencer = sequencer;
        this.logger = logger;
    }

    public EventLogger getLogger() {
        return logger;
    }

    public void registerSymbol(String symbol, SymbolMatcher matcher) {
        matchers.put(symbol, matcher);
    }

    public SymbolMatcher getMatcher(String symbol) {
        return matchers.get(symbol);
    }

    public Map<String, SymbolMatcher> getAllMatchers() {
        return matchers;
    }

    public void stop() {
        running = false;
    }

    public void run() {
        try {
            while (running) {
                Order o = sequencer.take();
                SymbolMatcher m = matchers.get(o.getSymbol());
                if (m != null) {
                    m.onOrder(o);
                } else {
                    System.out.println("No matcher for symbol " + o.getSymbol());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
