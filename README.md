Micro Stock Exchange – Matching Engine
A complete, modular, and extensible stock-exchange matching engine written in Java.
1. Introduction

This project implements a functional simulation of a stock-exchange matching engine.
It supports:

Automated & manual order entry

Order book maintenance

Matching and trade execution

Risk & PnL accounting

Market analytics

Configurable matching strategies (FIFO, Price-Time, Pro-Rata)

The system demonstrates:

Matching engine fundamentals

Price–time priority

Real exchange-style concurrency

Custom data structures (PriceLevel, OrderBook)

Clean and extensible architecture

2. Features
2.1 Matching Engine

Supports LIMIT and MARKET orders

Supports BUY / SELL

Matching Strategies Available:

FIFO

Price-Time

Pro-Rata

Partial fills

Multi-level matching

O(1) cancel and amend

Each symbol has its own independent matcher

2.2 Order Book

Buy side sorted descending

Sell side sorted ascending

Built using:

NavigableMap<BigDecimal, PriceLevel<Order>>

Custom linked-list-based FIFO queue

Tracks:

Best Bid / Best Ask

Mid Price

Total Traded Volume

VWAP

Spread

2.3 Trading Modes
1) Random Market Mode

Generates random orders every 2–3 seconds

High-frequency simulated trading

Color-coded:

Green = BUY

Red = SELL

Trades execute immediately if matchable

Order completion summary printed

2) Manual Order Entry

User selects:

Symbol

Side

Quantity

Price

Outputs formatted, color-coded order confirmation.

2.4 Reports
Symbol-Level Report

Best Bid / Ask

Mid Price

VWAP

Volume

Market-Wide Report

All registered symbols

Top volume symbol

Lowest spread

Liquidity view

2.5 Risk & PnL Tracking

The RiskEngine maintains:

Positions for each account + symbol

Average cost basis

Realized PnL

Unrealized PnL (via mid-price)

VWAP impact

2.6 Concurrency Architecture

Multiple producers → order sources

Sequencer serializes orders into a single queue

MatcherService routes to symbol-specific matchers

One dedicated thread per symbol matcher

Ensures complete thread safety

2.7 Logging

CSV event logger supports:

ORDER

TRADE

CANCEL

AMEND

Uses a bounded queue with back-pressure and background flushing.

2.8 Error Handling

InvalidOrderException

OrderNotFoundException

Defensive validation

Graceful shutdown with logger flush

2.9 Design Principles

Single Responsibility

Open–Closed Principle

Interface Segregation

Dependency Injection

Clean package separation

3. Package Structure
com.exchange
│
├── core
├── model
├── book
├── risk
├── log
├── interfaces
└── exceptions

4. UML Diagram – High-Level System Architecture
+-------------------------------------------------------------+
|                        MarketMicroExchange                  |
+-------------------------------------------------------------+
|                                                             |
|  ┌─────────────┐     ┌──────────────┐     ┌──────────────┐  |
|  | Gateways    | --> |  Sequencer   | --> |MatcherService|  |
|  └─────────────┘     └──────────────┘     └───────┬──────┘  |
|                                                    │        |
|                                           ┌────────▼──────┐ |
|                                           | SymbolMatcher | |
|                                           └───────┬───────┘ |
|        SymbolMatcher contains:                    │         |
|         - OrderBook                               │         |
|         - MatchStrategy (FIFO/PT/PR)              │         |
|         - RiskEngine                              │         |
|         - EventLogger                             │         |
+-------------------------------------------------------------+

5. Build Instructions
5.1 Compile
javac -d out $(find . -name "*.java")

5.2 Run
cd out
java com.exchange.core.Main

6. Usage Guide
6.1 Main Menu
========== MICRO EXCHANGE ==========
1) Select Matching Strategy
2) Register Symbol
3) Random Market Mode
4) Manual Order Entry
5) Exit

7. Sample I/O
7.1 Startup
========== MICRO EXCHANGE ==========
Default Symbols Loaded: AAPL, MSFT, GOOG, TSLA

7.2 Random Market Mode
=== RANDOM MARKET MODE ===
Press ENTER to stop...


Sample orders:

[AUTO ORDER] SYM:AAPL SIDE:BUY  QTY:40 PX:187.00 ID:RND1234
[AUTO ORDER] SYM:GOOG SIDE:SELL QTY:20 PX:274.00 ID:RND5678

7.3 Trade Execution
------------------------------------------------------------
TRADE EXECUTED
Symbol       : AAPL
Buyer Order  : RND1234
Seller Order : RND5678
Quantity     : 40
Price        : 187.00
------------------------------------------------------------

7.4 Manual Order Entry
[MANUAL ORDER] AAPL | BUY | QTY=50 | PX=120.50 | ID=MAN1234

7.5 Single-Symbol Summary
Symbol     : AAPL
Best Bid   : 120.50
Best Ask   : 121.00
Mid Price  : 120.75
Volume     : 450
VWAP       : 119.82

7.6 Market-Wide Summary
================= MARKET-WIDE SUMMARY =================

Symbol  BestBid   BestAsk   MidPrice   Volume   VWAP
-------------------------------------------------------
AAPL    120.50    121.00    120.75     450      119.82
MSFT    311.50    312.20    311.85     380      310.10
GOOG    274.00    274.50    274.25     510      273.90

Top Volume Symbol: TSLA
Lowest Spread    : GOOG (0.50)

7.7 Cancel & Amend
[CANCELLED] B101
[AMENDED] B102 → qty=75 @121.40

8. Testing
8.1 Coverage

Matching logic

FIFO / Price-Time / Pro-Rata strategies

OrderBook correctness

RiskEngine PnL

Cancel / Amend

Concurrency ordering

Market reports

8.2 Test Directory
test/
 └── com/exchange/
      ├── book/OrderBookTest.java
      ├── core/SymbolMatcherTest.java
      ├── model/OrderTest.java
      └── risk/RiskEngineTest.java

8.3 Required Libraries

junit-4.13.2.jar

hamcrest-core-1.3.jar

Add via:

Project → Properties → Java Build Path → Add External JARs

9. Design Considerations

Modular & extendable

Plug-and-play strategies

Multi-threaded & scalable

Optional logging

Replaceable risk engine

Easy-to-use UI

10. Conclusion

This project demonstrates:

Order processing

Matching engine mechanics

Price–time priority

Trade generation & reporting

Concurrency

Risk & PnL management

Scalable and extensible architecture

It closely models real-world matching engines and provides a solid foundation for further exchange or trading-system development.
