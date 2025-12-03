package com.exchange.match;

import com.exchange.book.OrderBook;
import com.exchange.model.Order;

public interface MatchStrategy {

    void match(Order incoming, OrderBook book, TradeCallback callback);

    interface TradeCallback {
        void onTrade(Order buy, Order sell, long quantityFilled);
    }
}
