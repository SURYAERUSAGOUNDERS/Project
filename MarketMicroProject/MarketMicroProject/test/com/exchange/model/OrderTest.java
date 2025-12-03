package model;

import org.junit.Assert;
import org.junit.Test;
import com.exchange.model.Order;
import java.math.BigDecimal;
import java.time.Instant;

public class OrderTest {

    @Test
    public void testOrderFill() {
        Order o = new Order(
                "O1", "ACC1", "AAPL", Order.Side.BUY,
                100, new BigDecimal("150.50"), Instant.now()
        );

        long filled = o.applyFill(40, new BigDecimal("150.50"));
        Assert.assertEquals(40, filled);
        Assert.assertEquals(60, o.getRemaining());
    }
}