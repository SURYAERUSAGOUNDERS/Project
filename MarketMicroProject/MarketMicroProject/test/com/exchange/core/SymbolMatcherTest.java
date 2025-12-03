package core;

import com.exchange.core.SymbolMatcher;
import com.exchange.match.FifoMatchStrategy;
import com.exchange.model.Order;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Instant;

public class SymbolMatcherTest {

    @Test
    public void testSimpleMatch() {

        SymbolMatcher matcher = new SymbolMatcher(
                "AAPL",
                null,
                new FifoMatchStrategy()
        );

        Order buy = new Order(
                "B1", "ACC1", "AAPL", Order.Side.BUY,
                50, new BigDecimal("100.00"), Instant.now()
        );

        Order sell = new Order(
                "S1", "ACC2", "AAPL", Order.Side.SELL,
                50, new BigDecimal("99.00"), Instant.now()
        );

        matcher.onOrder(buy);
        matcher.onOrder(sell);

        Assert.assertEquals(0, buy.getRemaining());
        Assert.assertEquals(0, sell.getRemaining());
    }
}
