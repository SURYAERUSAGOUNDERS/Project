package com.exchange.match;

import com.exchange.book.OrderBook;
import com.exchange.book.PriceLevel;
import com.exchange.model.Order;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NavigableMap;

public class FifoMatchStrategy implements MatchStrategy {

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

            BigDecimal bestAsk = entry.getKey();
            if (buy.getPrice().compareTo(bestAsk) < 0) break;

            PriceLevel<Order> level = entry.getValue();

            while (!buy.isFilled() && !level.isEmpty()) {

                Order sell = level.removeFirst();
                long qty = Math.min(buy.getRemaining(), sell.getRemaining());

                buy.applyFill(qty, bestAsk);
                sell.applyFill(qty, bestAsk);

                cb.onTrade(buy, sell, qty);

                if (!sell.isFilled()) {
                    PriceLevel.Node<Order> n = level.addLast(sell);
                    sell.nodeHandle = n;
                }
            }

            if (level.isEmpty()) sellLadder.remove(bestAsk);
        }
    }

    private void matchSell(Order sell, OrderBook book, TradeCallback cb) {

        NavigableMap<BigDecimal, PriceLevel<Order>> buyLadder = book.getBuyLadder();

        while (!sell.isFilled() && !buyLadder.isEmpty()) {

            Map.Entry<BigDecimal, PriceLevel<Order>> entry = buyLadder.firstEntry();
            if (entry == null) break;

            BigDecimal bestBid = entry.getKey();
            if (sell.getPrice().compareTo(bestBid) > 0) break;

            PriceLevel<Order> level = entry.getValue();

            while (!sell.isFilled() && !level.isEmpty()) {

                Order buy = level.removeFirst();
                long qty = Math.min(sell.getRemaining(), buy.getRemaining());

                sell.applyFill(qty, bestBid);
                buy.applyFill(qty, bestBid);

                cb.onTrade(buy, sell, qty);

                if (!buy.isFilled()) {
                    PriceLevel.Node<Order> n = level.addLast(buy);
                    buy.nodeHandle = n;
                }
            }

            if (level.isEmpty()) buyLadder.remove(bestBid);
        }
    }
}
