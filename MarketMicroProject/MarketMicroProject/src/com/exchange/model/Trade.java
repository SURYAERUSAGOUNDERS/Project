package com.exchange.model;

import java.math.BigDecimal;
import java.time.Instant;

public class Trade {

    private final String buyOrderId;
    private final String sellOrderId;
    private final String symbol;
    private final long quantity;
    private final BigDecimal price;
    private final Instant timestamp;

    public Trade(String buyOrderId, String sellOrderId, String symbol,
                 long quantity, BigDecimal price, Instant timestamp) {
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = timestamp;
    }

    public String getBuyOrderId() {
        return buyOrderId;
    }

    public String getSellOrderId() {
        return sellOrderId;
    }

    public String getSymbol() {
        return symbol;
    }

    public long getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public BigDecimal getNotional() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public String toString() {
        return "TRADE " + quantity + "@" + price + " BUY:" + buyOrderId + " SELL:" + sellOrderId;
    }
}
