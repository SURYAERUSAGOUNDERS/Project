package com.exchange.core;

import com.exchange.interfaces.OrderSource;
import com.exchange.model.Order;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Sequencer implements OrderSource {

    private final BlockingQueue<Order> queue = new LinkedBlockingQueue<Order>();

    public void publish(Order order) {
        try {
            queue.put(order);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Order take() throws InterruptedException {
        return queue.take();
    }
}
