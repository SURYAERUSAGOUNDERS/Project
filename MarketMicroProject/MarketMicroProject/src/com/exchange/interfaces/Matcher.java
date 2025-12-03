package com.exchange.interfaces;

import com.exchange.model.Order;

import java.math.BigDecimal;

public interface Matcher {
    void onOrder(Order order);
    boolean cancel(String orderId);
    boolean amend(String orderId, long newQty, BigDecimal newPrice);
}
