package com.exchange.core;

import com.exchange.log.EventLogger;
import com.exchange.match.FifoMatchStrategy;
import com.exchange.match.MatchStrategy;
import com.exchange.match.PriceTimeMatchStrategy;
import com.exchange.match.ProRataMatchStrategy;
//import com.exchange.risk.RiskEngine;
import com.exchange.sim.ManualOrderEntry;
import com.exchange.sim.RandomOrderGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private static final Scanner SCAN = new Scanner(System.in);

    private static MatchStrategy activeStrategy = new FifoMatchStrategy();

    private static final Map<String, SymbolMatcher> matcherMap = new HashMap<String, SymbolMatcher>();

    private static final String[] DEFAULT_SYMBOLS = {
            "AAPL", "MSFT", "GOOG", "AMZN", "TSLA", "META", "NFLX"
    };

    public static void main(String[] args) throws Exception {

        Sequencer sequencer = new Sequencer();
        EventLogger logger = new EventLogger("events.csv", 10000, 200);
        MatcherService service = new MatcherService(sequencer, logger);
        //RiskEngine risk = RiskEngine.getInstance(); // kept for future PnL usage

        Thread matcherThread = new Thread(service, "MatcherService");
        matcherThread.start();

        for (int i = 0; i < DEFAULT_SYMBOLS.length; i++) {
            String sym = DEFAULT_SYMBOLS[i];
            SymbolMatcher matcher = new SymbolMatcher(sym, service.getLogger(), activeStrategy);
            service.registerSymbol(sym, matcher);
            matcherMap.put(sym, matcher);
        }

        System.out.println("======================================");
        System.out.println("     MICRO EXCHANGE – MATCH ENGINE");
        System.out.println("======================================");

        boolean running = true;
        while (running) {
            printMainMenu();
            String opt = SCAN.nextLine().trim();

            if ("1".equals(opt)) {
                chooseStrategy(service);
            } else if ("2".equals(opt)) {
                registerSymbol(service);
            } else if ("3".equals(opt)) {
                RandomOrderGenerator.run(sequencer, logger, matcherMap);
            } else if ("4".equals(opt)) {
                ManualOrderEntry.run(sequencer, logger, matcherMap);
            } else if ("5".equals(opt)) {
                running = false;
            } else {
                System.out.println("Invalid selection.");
            }
        }

        service.stop();
        matcherThread.interrupt();
        try {
            matcherThread.join(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        logger.shutdownAndAwait(2000);
        System.out.println("Goodbye.");
    }

    private static void printMainMenu() {
        System.out.println();
        System.out.println("1) Select Matching Strategy");
        System.out.println("2) Register Symbol");
        System.out.println("3) Random Market Mode");
        System.out.println("4) Manual Order Entry");
        System.out.println("5) Exit");
        System.out.print("Choose: ");
    }

    private static void chooseStrategy(MatcherService service) {

        System.out.println();
        System.out.println("Select Matching Strategy:");
        System.out.println("1) FIFO");
        System.out.println("2) PRICE-TIME");
        System.out.println("3) PRO-RATA");
        System.out.print("Choose: ");

        String s = SCAN.nextLine().trim();

        MatchStrategy chosen = null;
        if ("1".equals(s)) {
            chosen = new FifoMatchStrategy();
        } else if ("2".equals(s)) {
            chosen = new PriceTimeMatchStrategy();
        } else if ("3".equals(s)) {
            chosen = new ProRataMatchStrategy();
        } else {
            System.out.println("Invalid strategy.");
            return;
        }

        activeStrategy = chosen;

        for (Map.Entry<String, SymbolMatcher> e : matcherMap.entrySet()) {
            String sym = e.getKey();
            SymbolMatcher matcher = new SymbolMatcher(sym, service.getLogger(), activeStrategy);
            service.registerSymbol(sym, matcher);
            matcherMap.put(sym, matcher);
        }

        System.out.println("Active Strategy Updated.");
    }

    private static void registerSymbol(MatcherService service) {

        System.out.print("Enter symbol: ");
        String sym = SCAN.nextLine().trim().toUpperCase();
        if (sym.length() == 0) {
            System.out.println("Symbol cannot be empty.");
            return;
        }

        if (matcherMap.containsKey(sym)) {
            System.out.println("Symbol already registered: " + sym);
            return;
        }

        SymbolMatcher matcher = new SymbolMatcher(sym, service.getLogger(), activeStrategy);
        service.registerSymbol(sym, matcher);
        matcherMap.put(sym, matcher);

        System.out.println("Registered symbol: " + sym);
    }
}
