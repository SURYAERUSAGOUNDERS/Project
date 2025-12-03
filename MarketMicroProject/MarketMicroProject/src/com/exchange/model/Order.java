package com.exchange.model;

import com.exchange.book.PriceLevel;

import java.math.BigDecimal;
import java.time.Instant;

public class Order {

    public enum Side { BUY, SELL }

    private final String orderId;
    private final String accountId;
    private final String symbol;
    private final Side side;
    private final long originalQty;
    private long remaining;
    private BigDecimal price;
    private final OrderType type;
    private final TimeInForce tif;
    private final Instant timestamp;

    public PriceLevel.Node<Order> nodeHandle;

    public Order(String orderId,
                 String accountId,
                 String symbol,
                 Side side,
                 long quantity,
                 BigDecimal price,
                 Instant timestamp) {
        this.orderId = orderId;
        this.accountId = accountId == null ? "UNKNOWN" : accountId;
        this.symbol = symbol;
        this.side = side;
        this.originalQty = quantity;
        this.remaining = quantity;
        this.price = price;
        this.type = price == null ? OrderType.MARKET : OrderType.LIMIT;
        this.tif = TimeInForce.GTC;
        this.timestamp = timestamp == null ? Instant.now() : timestamp;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getSymbol() {
        return symbol;
    }

    public Side getSide() {
        return side;
    }

    public long getOriginalQty() {
        return originalQty;
    }

    public long getRemaining() {
        return remaining;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public OrderType getType() {
        return type;
    }

    public TimeInForce getTif() {
        return tif;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public boolean isFilled() {
        return remaining <= 0;
    }

    public synchronized long applyFill(long qtyToFill, BigDecimal tradePrice) {
        long fill = Math.min(qtyToFill, remaining);
        if (fill <= 0) return 0;
        remaining -= fill;
        return fill;
    }

    public synchronized void reduceTo(long newRemaining) {
        if (newRemaining < 0) newRemaining = 0;
        this.remaining = newRemaining;
    }

    public void setPrice(BigDecimal newPrice) {
        this.price = newPrice;
    }

    @Override
    public String toString() {
        String p = price == null ? "MKT" : price.toPlainString();
        return "Order[id=" + orderId + " acc=" + accountId + " " + side + " " +
                remaining + "/" + originalQty + "@" + p + "]";
    }
}
