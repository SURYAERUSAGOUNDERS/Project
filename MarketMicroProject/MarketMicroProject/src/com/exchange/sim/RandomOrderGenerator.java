package com.exchange.sim;

import com.exchange.core.Sequencer;
import com.exchange.core.SymbolMatcher;
import com.exchange.log.EventLogger;
import com.exchange.log.LogEvent;
import com.exchange.model.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class RandomOrderGenerator {

    private static final Scanner SCAN = new Scanner(System.in);
    private static final Random R = new Random();

    public static void run(Sequencer seq, EventLogger logger, Map<String, SymbolMatcher> mm) {

        System.out.println("=== RANDOM MARKET MODE ===");
        System.out.println("Random orders will be generated every 2–3 seconds.");
        System.out.println("Press ENTER to stop...");

        Thread stopper = new Thread(new Runnable() {
            public void run() {
                SCAN.nextLine();
            }
        }, "RandomMode-Stopper");
        stopper.start();

        while (stopper.isAlive()) {
            try {
                List<String> symbols = new ArrayList<String>(mm.keySet());

                if (symbols.isEmpty()) {
                    System.out.println("No symbols registered. Stopping Random Mode.");
                    break;
                }

                String s = symbols.get(R.nextInt(symbols.size()));
                Order.Side side = R.nextBoolean() ? Order.Side.BUY : Order.Side.SELL;
                long qty = (R.nextInt(10) + 1) * 10L;
                BigDecimal px = new BigDecimal(50 + R.nextInt(200)).setScale(2, BigDecimal.ROUND_HALF_UP);

                String id = "RND" + System.currentTimeMillis();

                Order o = new Order(id, "AUTO", s, side, qty, px, Instant.now());

                String line = String.format(
                        "[AUTO ORDER]   SYM:%-5s  SIDE:%-4s  QTY:%-5d  PX:%-8s  ID:%s  --> submitted",
                        s, side, qty, px.toPlainString(), id
                );

                if (side == Order.Side.BUY) {
                    System.out.println("\u001B[32m" + line + "\u001B[0m");
                } else {
                    System.out.println("\u001B[31m" + line + "\u001B[0m");
                }

                seq.publish(o);
                if (logger != null) {
                    logger.publish(LogEvent.order(o));
                }

                Thread.sleep(2000 + R.nextInt(1000));

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.println("[Random Mode] Stopped.");
    }
}
