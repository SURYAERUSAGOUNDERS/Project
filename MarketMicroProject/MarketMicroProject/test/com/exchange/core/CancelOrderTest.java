package core;

//import com.exchange.book.PriceLevel;
import com.exchange.core.SymbolMatcher;
//import com.exchange.log.EventLogger;
import com.exchange.match.FifoMatchStrategy;
import com.exchange.model.Order;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Instant;

public class CancelOrderTest {

    @Test
    public void testCancelOrder() {
        SymbolMatcher matcher = new SymbolMatcher(
                "AAPL",
                null,
                new FifoMatchStrategy()
        );

        Order o1 = new Order("O1", "ACC1", "AAPL",
                Order.Side.BUY, 50, new BigDecimal("150.00"), Instant.now());

        matcher.onOrder(o1);

        boolean cancelled = matcher.cancel("O1");

        Assert.assertTrue(cancelled);
        Assert.assertEquals(0, matcher.getOrderBook().getBuyLadder().size());
    }
}