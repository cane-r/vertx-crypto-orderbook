# VALR Assessment

This application was generated using http://start.vertx.io

# Tech Stack
Vertx,Kotlin,JUnit,Mockito,RxJava

# Building

To launch your tests:
``
mvn clean test
``

To package your application:
``
mvn clean package
``

To run your application:
``
mvn clean compile exec:java
``

# Goal
Build a working, in-memory order book to place a limit order with order matching including the
ability to view all open orders.

# Notes
* Limit is an order list, sitting at a price level to be filled,and has a property called volume,which is the sum of all order's size
* Orders will be processed in fifo manner
* For limit orders,algorithm is : if the side is **BUY**,match the given order with orders at the **given order's price level or lower** in the asks.If side is **SELL**,match the **given order with orders at the given order's price level or higher** in the bids
* For market orders,look for the orders in the best bid/ask price bucket and fill orders.If one price level is fully filled and order is still not filled,look for the next best ask/bid
* If no match,it will stay on the order book until its matched
* Used this video to understand and model the book and limit/market order https://www.youtube.com/watch?v=Kl4-VJ2K8Ik
# Todo
* Write integration/unit tests for order book bids/asks
* limit order book page results to last x orders
* generalize for other ticker/currencies other than BTCZAR
* validation and error handling
* order filling and matching algorithms
* trades
* maybe mode a market order too
* user id for matching
* auth ( optional )
* possible spring context integration for DI?
