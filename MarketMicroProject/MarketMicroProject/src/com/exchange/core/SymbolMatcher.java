package com.exchange.core;

import com.exchange.book.OrderBook;
import com.exchange.book.PriceLevel;
import com.exchange.exceptions.OrderNotFoundException;
import com.exchange.interfaces.Matcher;
import com.exchange.log.EventLogger;
import com.exchange.log.LogEvent;
import com.exchange.match.MatchStrategy;
import com.exchange.model.Order;
import com.exchange.model.Trade;
import com.exchange.risk.RiskEngine;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SymbolMatcher implements Matcher {

    private final OrderBook book;
    private final Map<String, Order> orderIndex = new ConcurrentHashMap<String, Order>();
    private final EventLogger eventLogger;
    private final RiskEngine risk = RiskEngine.getInstance();
    private final MatchStrategy strategy;

    public SymbolMatcher(String symbol, EventLogger logger, MatchStrategy strategy) {
        this.book = new OrderBook(symbol);
        this.eventLogger = logger;
        this.strategy = strategy;
    }

    public OrderBook getOrderBook() {
        return book;
    }

    public void onOrder(final Order order) {

    orderIndex.put(order.getOrderId(), order);

    strategy.match(order, book, new MatchStrategy.TradeCallback() {
        public void onTrade(Order buy, Order sell, long qty) {

            BigDecimal px = sell.getPrice() != null ? sell.getPrice() : buy.getPrice();
            if (px == null) px = BigDecimal.ZERO;

            Trade t = new Trade(
                    buy.getOrderId(),
                    sell.getOrderId(),
                    book.getSymbol(),
                    qty,
                    px,
                    Instant.now()
            );

            book.addTrade(px, qty);
            risk.onTrade(t, buy, sell);

            if (eventLogger != null) eventLogger.publish(LogEvent.trade(t));

            System.out.println("----------------------------------------------------");
            System.out.println("TRADE EXECUTED");
            System.out.println("----------------------------------------------------");
            System.out.println("Symbol       : " + book.getSymbol());
            System.out.println("Buyer Order  : " + buy.getOrderId() + " (" + buy.getAccountId() + ")");
            System.out.println("Seller Order : " + sell.getOrderId() + " (" + sell.getAccountId() + ")");
            System.out.println("Quantity     : " + qty);
            System.out.println("Price        : " + px);
            System.out.println("Timestamp    : " + t.getTimestamp());
            System.out.println("----------------------------------------------------");
        }
    });

    // ★★★ INSERT REMAINING ORDER INTO BOOK ★★★
    if (!order.isFilled()) {
        if (order.getSide() == Order.Side.BUY) {
            PriceLevel<Order> level = book.getBuyLadder()
                    .computeIfAbsent(order.getPrice(), p -> new PriceLevel<>());
            order.nodeHandle = level.addLast(order);
        } else {
            PriceLevel<Order> level = book.getSellLadder()
                    .computeIfAbsent(order.getPrice(), p -> new PriceLevel<>());
            order.nodeHandle = level.addLast(order);
        }
    }
}


    public boolean cancel(String orderId) {

        Order o = orderIndex.remove(orderId);
        if (o == null) {
            throw new OrderNotFoundException("Order not found: " + orderId);
        }

        Map<BigDecimal, PriceLevel<Order>> ladder =
                (o.getSide() == Order.Side.BUY)
                        ? book.getBuyLadder()
                        : book.getSellLadder();

        PriceLevel<Order> level = ladder.get(o.getPrice());
        if (level != null) {
            level.remove(o.nodeHandle);
            if (level.isEmpty()) {
                ladder.remove(o.getPrice());
            }
        }

        System.out.println("[CANCELLED] " + orderId);
        if (eventLogger != null) {
            eventLogger.publish(LogEvent.cancel(book.getSymbol(), orderId));
        }
        return true;
    }

    public boolean amend(String orderId, long newQty, BigDecimal newPrice) {

        Order o = orderIndex.get(orderId);
        if (o == null) {
            throw new OrderNotFoundException("Order not found: " + orderId);
        }

        Map<BigDecimal, PriceLevel<Order>> ladder =
                (o.getSide() == Order.Side.BUY)
                        ? book.getBuyLadder()
                        : book.getSellLadder();

        PriceLevel<Order> oldLevel = ladder.get(o.getPrice());
        if (oldLevel != null) {
            oldLevel.remove(o.nodeHandle);
            if (oldLevel.isEmpty()) {
                ladder.remove(o.getPrice());
            }
        }

        o.setPrice(newPrice);
        o.reduceTo(newQty);

        PriceLevel<Order> newLevel =
                ladder.computeIfAbsent(newPrice, new java.util.function.Function<BigDecimal, PriceLevel<Order>>() {
                    public PriceLevel<Order> apply(BigDecimal p) {
                        return new PriceLevel<Order>();
                    }
                });

        PriceLevel.Node<Order> n = newLevel.addLast(o);
        o.nodeHandle = n;

        System.out.println("[AMENDED] " + orderId + " → qty=" + newQty + " @" + newPrice);
        if (eventLogger != null) {
            eventLogger.publish(LogEvent.amend(book.getSymbol(), orderId, newQty, newPrice));
        }
        return true;
    }
}
