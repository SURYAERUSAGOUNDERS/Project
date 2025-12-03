package com.exchange.book;

import com.exchange.model.Order;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

public class OrderBook {

    private final String symbol;
    private final NavigableMap<BigDecimal, PriceLevel<Order>> buyLadder;
    private final NavigableMap<BigDecimal, PriceLevel<Order>> sellLadder;

    private final AtomicLong totalTradedQty = new AtomicLong(0);
    private BigDecimal totalTradedNotional = BigDecimal.ZERO;

    public OrderBook(String symbol) {
        this.symbol = symbol;
        this.buyLadder = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
        this.sellLadder = new ConcurrentSkipListMap<>();
    }

    public String getSymbol() {
        return symbol;
    }

    public NavigableMap<BigDecimal, PriceLevel<Order>> getBuyLadder() {
        return buyLadder;
    }

    public NavigableMap<BigDecimal, PriceLevel<Order>> getSellLadder() {
        return sellLadder;
    }

    public synchronized void addTrade(BigDecimal price, long qty) {
        if (qty <= 0) return;
        totalTradedQty.addAndGet(qty);
        totalTradedNotional = totalTradedNotional.add(price.multiply(BigDecimal.valueOf(qty)));
    }

    public long getTotalTradedQty() {
        return totalTradedQty.get();
    }

    public synchronized BigDecimal getVWAP() {
        long q = totalTradedQty.get();
        if (q == 0) return BigDecimal.ZERO;
        return totalTradedNotional.divide(BigDecimal.valueOf(q), 4, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal getBestBid() {
        return buyLadder.isEmpty() ? null : buyLadder.firstKey();
    }

    public BigDecimal getBestAsk() {
        return sellLadder.isEmpty() ? null : sellLadder.firstKey();
    }

    public BigDecimal getMidPrice() {
        BigDecimal b = getBestBid();
        BigDecimal a = getBestAsk();
        if (b == null && a == null) return null;
        if (b == null) return a;
        if (a == null) return b;
        return b.add(a).divide(BigDecimal.valueOf(2), 4, BigDecimal.ROUND_HALF_UP);
    }

    /** FIXED VERSION — supports display after each trade */
    public void printBook() {
        System.out.println();
        System.out.println("===== ORDER BOOK: " + symbol + " =====");

        System.out.println("--- SELL (Ask) ---");
        if (sellLadder.isEmpty()) {
            System.out.println("(no asks)");
        } else {
            for (BigDecimal p : sellLadder.keySet()) {
                System.out.println(p + " : " + sellLadder.get(p).totalQuantity());
            }
        }

        System.out.println("--- BUY (Bid) ---");
        if (buyLadder.isEmpty()) {
            System.out.println("(no bids)");
        } else {
            for (BigDecimal p : buyLadder.keySet()) {
                System.out.println(p + " : " + buyLadder.get(p).totalQuantity());
            }
        }

        System.out.println("Traded Volume: " + getTotalTradedQty());
        System.out.println("VWAP: " + getVWAP());
        System.out.println("Mid Price: " + getMidPrice());
        System.out.println("===============================");
    }
}
