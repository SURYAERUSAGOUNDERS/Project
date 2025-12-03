package book;

import com.exchange.book.OrderBook;
import com.exchange.book.PriceLevel;
import com.exchange.model.Order;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Instant;

public class OrderBookTest {

    @Test
    public void testBestBidAndAsk() {
        OrderBook book = new OrderBook("AAPL");

        Order b1 = new Order("B1", "ACC1", "AAPL", Order.Side.BUY, 100,
                new BigDecimal("100.00"), Instant.now());
        Order b2 = new Order("B2", "ACC2", "AAPL", Order.Side.BUY, 100,
                new BigDecimal("101.00"), Instant.now());
        Order s1 = new Order("S1", "ACC3", "AAPL", Order.Side.SELL, 100,
                new BigDecimal("102.00"), Instant.now());

        book.getBuyLadder()
                .computeIfAbsent(b1.getPrice(), p -> new PriceLevel<>())
                .addLast(b1);

        book.getBuyLadder()
                .computeIfAbsent(b2.getPrice(), p -> new PriceLevel<>())
                .addLast(b2);

        book.getSellLadder()
                .computeIfAbsent(s1.getPrice(), p -> new PriceLevel<>())
                .addLast(s1);

        Assert.assertEquals(new BigDecimal("101.00"), book.getBestBid());
        Assert.assertEquals(new BigDecimal("102.00"), book.getBestAsk());
    }
}