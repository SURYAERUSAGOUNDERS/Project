package com.exchange.match;

import com.exchange.book.OrderBook;
import com.exchange.book.PriceLevel;
import com.exchange.model.Order;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NavigableMap;

public class ProRataMatchStrategy implements MatchStrategy {

    public void match(final Order incoming, final OrderBook book, final TradeCallback cb) {
        if (incoming.getSide() == Order.Side.BUY) {
            matchBuy(incoming, book, cb);
        } else {
            matchSell(incoming, book, cb);
        }
    }

    private void matchBuy(Order buy, OrderBook book, TradeCallback cb) {

        NavigableMap<BigDecimal, PriceLevel<Order>> sellLadder = book.getSellLadder();

        while (!buy.isFilled() && !sellLadder.isEmpty()) {

            Map.Entry<BigDecimal, PriceLevel<Order>> entry = sellLadder.firstEntry();
            if (entry == null) break;

            BigDecimal ask = entry.getKey();
            if (buy.getPrice().compareTo(ask) < 0) break;

            PriceLevel<Order> level = entry.getValue();
            long totalQty = level.totalQuantity();
            if (totalQty <= 0) {
                sellLadder.remove(ask);
                continue;
            }

            long toFill = buy.getRemaining();

            PriceLevel.Node<Order> n = level.firstNode();
            while (n != null && toFill > 0) {
                Order sell = n.value;

                long alloc = (sell.getRemaining() * toFill) / totalQty;
                if (alloc <= 0) alloc = Math.min(1, sell.getRemaining());
                if (alloc > toFill) alloc = toFill;

                buy.applyFill(alloc, ask);
                sell.applyFill(alloc, ask);

                cb.onTrade(buy, sell, alloc);

                toFill = buy.getRemaining();
                totalQty -= alloc;

                if (sell.isFilled()) {
                    PriceLevel.Node<Order> remove = n;
                    n = n.next;
                    level.remove(remove);
                } else {
                    n = n.next;
                }
            }

            if (level.isEmpty()) sellLadder.remove(ask);
        }
    }

    private void matchSell(Order sell, OrderBook book, TradeCallback cb) {

        NavigableMap<BigDecimal, PriceLevel<Order>> buyLadder = book.getBuyLadder();

        while (!sell.isFilled() && !buyLadder.isEmpty()) {

            Map.Entry<BigDecimal, PriceLevel<Order>> entry = buyLadder.firstEntry();
            if (entry == null) break;

            BigDecimal bid = entry.getKey();
            if (sell.getPrice().compareTo(bid) > 0) break;

            PriceLevel<Order> level = entry.getValue();
            long totalQty = level.totalQuantity();
            if (totalQty <= 0) {
                buyLadder.remove(bid);
                continue;
            }

            long toFill = sell.getRemaining();

            PriceLevel.Node<Order> n = level.firstNode();
            while (n != null && toFill > 0) {
                Order buy = n.value;

                long alloc = (buy.getRemaining() * toFill) / totalQty;
                if (alloc <= 0) alloc = Math.min(1, buy.getRemaining());
                if (alloc > toFill) alloc = toFill;

                sell.applyFill(alloc, bid);
                buy.applyFill(alloc, bid);

                cb.onTrade(buy, sell, alloc);

                toFill = sell.getRemaining();
                totalQty -= alloc;

                if (buy.isFilled()) {
                    PriceLevel.Node<Order> remove = n;
                    n = n.next;
                    level.remove(remove);
                } else {
                    n = n.next;
                }
            }

            if (level.isEmpty()) buyLadder.remove(bid);
        }
    }
}
