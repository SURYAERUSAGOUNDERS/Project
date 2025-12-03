package com.exchange.report;

import com.exchange.core.MatcherService;
import com.exchange.core.SymbolMatcher;
import com.exchange.book.OrderBook;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MarketReport {

    public static class SummaryRow {
        public String symbol;
        public BigDecimal bestBid;
        public BigDecimal bestAsk;
        public BigDecimal midPrice;
        public long volume;
        public BigDecimal vwap;
    }

    public static SummaryRow getSymbolSummary(SymbolMatcher matcher) {
        OrderBook book = matcher.getOrderBook();

        SummaryRow r = new SummaryRow();
        r.symbol = book.getSymbol();
        r.bestBid = book.getBestBid();
        r.bestAsk = book.getBestAsk();
        r.midPrice = book.getMidPrice();
        r.volume = book.getTotalTradedQty();
        r.vwap = book.getVWAP();
        return r;
    }

    public static List<SummaryRow> getMarketSummary(List<String> symbols, MatcherService ms) {
        List<SummaryRow> rows = new ArrayList<SummaryRow>();
        for (String s : symbols) {
            SymbolMatcher m = ms.getMatcher(s);
            if (m != null) {
                rows.add(getSymbolSummary(m));
            }
        }
        return rows;
    }

    public static String findTopVolumeSymbol(List<SummaryRow> rows) {
        long max = -1;
        String sym = null;
        for (SummaryRow r : rows) {
            if (r.volume > max) {
                max = r.volume;
                sym = r.symbol;
            }
        }
        return sym;
    }

    public static String findLowestSpreadSymbol(List<SummaryRow> rows) {
        BigDecimal min = null;
        String sym = null;
        for (SummaryRow r : rows) {
            if (r.bestBid != null && r.bestAsk != null) {
                BigDecimal spread = r.bestAsk.subtract(r.bestBid);
                if (min == null || spread.compareTo(min) < 0) {
                    min = spread;
                    sym = r.symbol;
                }
            }
        }
        return sym;
    }
}
