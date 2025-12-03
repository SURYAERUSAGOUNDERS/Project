package com.exchange.risk;

import com.exchange.model.Order;
import com.exchange.model.Trade;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RiskEngine {

    public static class Position {
        public long qty = 0;
        public BigDecimal avgCost = BigDecimal.ZERO;
        public BigDecimal realizedPnl = BigDecimal.ZERO;
    }

    private final Map<String, Map<String, Position>> ledger = new ConcurrentHashMap<String, Map<String, Position>>();

    private static final RiskEngine INSTANCE = new RiskEngine();

    private RiskEngine() {}

    public static RiskEngine getInstance() {
        return INSTANCE;
    }

    private Map<String, Position> map(String account) {
        return ledger.computeIfAbsent(account, new java.util.function.Function<String, Map<String, Position>>() {
            public Map<String, Position> apply(String x) {
                return new ConcurrentHashMap<String, Position>();
            }
        });
    }

    private Position pos(String account, String symbol) {
        return map(account).computeIfAbsent(symbol, new java.util.function.Function<String, Position>() {
            public Position apply(String x) {
                return new Position();
            }
        });
    }

    public void onTrade(Trade t, Order buy, Order sell) {
        update(buy.getAccountId(), t.getSymbol(), t.getQuantity(), t.getPrice(), true);
        update(sell.getAccountId(), t.getSymbol(), t.getQuantity(), t.getPrice(), false);
    }

    private synchronized void update(String acc, String sym, long qty, BigDecimal price, boolean isBuy) {
        Position p = pos(acc, sym);
        long signed = isBuy ? qty : -qty;

        if (p.qty != 0 && Long.signum(p.qty) != Long.signum(signed)) {
            long closing = Math.min(Math.abs(p.qty), Math.abs(signed));
            if (p.qty > 0 && signed < 0) {
                p.realizedPnl = p.realizedPnl.add(price.subtract(p.avgCost).multiply(BigDecimal.valueOf(closing)));
            } else if (p.qty < 0 && signed > 0) {
                p.realizedPnl = p.realizedPnl.add(p.avgCost.subtract(price).multiply(BigDecimal.valueOf(closing)));
            }
            p.qty += signed;
            if (p.qty == 0) {
                p.avgCost = BigDecimal.ZERO;
            }
            long leftover = Math.abs(signed) - closing;
            if (leftover > 0) {
                p.avgCost = price;
                p.qty += isBuy ? leftover : -leftover;
            }
            return;
        }

        if (p.qty == 0) {
            p.qty = signed;
            p.avgCost = price;
        } else {
            long oldq = Math.abs(p.qty);
            long addq = Math.abs(signed);
            BigDecimal oldNotional = p.avgCost.multiply(BigDecimal.valueOf(oldq));
            BigDecimal addNotional = price.multiply(BigDecimal.valueOf(addq));
            long newQty = oldq + addq;
            p.avgCost = oldNotional.add(addNotional).divide(BigDecimal.valueOf(newQty), 8, BigDecimal.ROUND_HALF_UP);
            p.qty += signed;
        }
    }
}
