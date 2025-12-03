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
import java.util.Scanner;

public class ManualOrderEntry {

    private static final Scanner SCAN = new Scanner(System.in);

    public static void run(Sequencer seq, EventLogger logger, Map<String, SymbolMatcher> mm) {

        List<String> syms = new ArrayList<String>(mm.keySet());

        if (syms.isEmpty()) {
            System.out.println("No symbols registered.");
            return;
        }

        System.out.println("=== MANUAL ORDER ENTRY ===");
        for (int i = 0; i < syms.size(); i++) {
            System.out.println((i + 1) + ") " + syms.get(i));
        }
        System.out.println("0) Back");
        System.out.print("Select symbol: ");

        int idx = Integer.parseInt(SCAN.nextLine());
        if (idx == 0) return;
        if (idx < 1 || idx > syms.size()) return;

        String sym = syms.get(idx - 1);

        System.out.println("1) BUY");
        System.out.println("2) SELL");
        System.out.print("Side: ");

        String s = SCAN.nextLine();
        Order.Side side = "1".equals(s) ? Order.Side.BUY : Order.Side.SELL;

        System.out.print("Quantity: ");
        long qty = Long.parseLong(SCAN.nextLine());

        System.out.print("Price: ");
        BigDecimal px = new BigDecimal(SCAN.nextLine());

        String id = "MAN" + System.currentTimeMillis();

        Order o = new Order(id, "USER", sym, side, qty, px, Instant.now());

        String line = "[MANUAL ORDER] " + sym + " | " + side + " | QTY=" + qty + " | PX=" + px + " | ID=" + id;
        if (side == Order.Side.BUY) {
            System.out.println("\u001B[32m" + line + "\u001B[0m");
        } else {
            System.out.println("\u001B[31m" + line + "\u001B[0m");
        }

        seq.publish(o);
        if (logger != null) logger.publish(LogEvent.order(o));
    }
}
