package com.exchange.log;

import com.exchange.model.Order;
import com.exchange.model.Trade;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

public final class LogEvent {

    private final EventType type;
    private final String symbol;
    private final String orderId;
    private final String side;
    private final long quantity;
    private final BigDecimal price;
    private final String relatedOrderId;
    private final Instant timestamp;
    private final String note;

    private LogEvent(EventType type, String symbol, String orderId, String side,
                     long quantity, BigDecimal price, String relatedOrderId,
                     Instant timestamp, String note) {
        this.type = type;
        this.symbol = symbol;
        this.orderId = orderId;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.relatedOrderId = relatedOrderId;
        this.timestamp = timestamp == null ? Instant.now() : timestamp;
        this.note = note;
    }

    public static LogEvent order(Order o) {
        return new LogEvent(EventType.ORDER, o.getSymbol(), o.getOrderId(),
                o.getSide().name(), o.getRemaining(), o.getPrice(), null,
                o.getTimestamp(), "");
    }

    public static LogEvent trade(Trade t) {
        return new LogEvent(EventType.TRADE, t.getSymbol(), t.getBuyOrderId(),
                "BUY", t.getQuantity(), t.getPrice(), t.getSellOrderId(),
                t.getTimestamp(), "");
    }

    public static LogEvent amend(String symbol, String orderId, long newQty, BigDecimal newPx) {
        return new LogEvent(EventType.AMEND, symbol, orderId, "", newQty, newPx, null,
                Instant.now(), "");
    }

    public static LogEvent cancel(String symbol, String orderId) {
        return new LogEvent(EventType.CANCEL, symbol, orderId, "", 0L, BigDecimal.ZERO, null,
                Instant.now(), "");
    }

    public String toCsv() {
        StringJoiner s = new StringJoiner(",");
        s.add(type.name());
        s.add(symbol == null ? "" : symbol);
        s.add(orderId == null ? "" : orderId);
        s.add(side == null ? "" : side);
        s.add(Long.toString(quantity));
        s.add(price == null ? "" : price.toPlainString());
        s.add(relatedOrderId == null ? "" : relatedOrderId);
        s.add(DateTimeFormatter.ISO_INSTANT.format(timestamp));
        s.add(note == null ? "" : note);
        return s.toString();
    }

    @Override
    public String toString() {
        return toCsv();
    }
}
