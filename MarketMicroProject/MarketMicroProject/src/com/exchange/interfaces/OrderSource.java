package com.exchange.interfaces;

import com.exchange.model.Order;

public interface OrderSource {
    void publish(Order order);
}
