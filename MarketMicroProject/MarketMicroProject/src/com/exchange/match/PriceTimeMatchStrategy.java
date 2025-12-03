package com.exchange.match;

import com.exchange.book.OrderBook;
import com.exchange.model.Order;

public class PriceTimeMatchStrategy implements MatchStrategy {

    private final FifoMatchStrategy delegate = new FifoMatchStrategy();

    public void match(Order incoming, OrderBook book, TradeCallback cb) {
        delegate.match(incoming, book, cb);
    }
}
