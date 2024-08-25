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

#### Completed the core requirements and optimized as much as I can in terms of data structures.

# Notes
* Limit is an order list, sitting at a price level to be filled,and has a property called volume,which is the sum of all order's size
* Orders will be processed in fifo manner
* For limit orders,algorithm is : if the side is **BUY**,match the given order with orders at the **given order's price level or lower** in the asks.If side is **SELL**,match the **given order with orders at the given order's price level or higher** in the bids
* For market orders,look for the orders in the best bid/ask price bucket and fill orders.If one price level is fully filled and order is still not filled,look for the next best ask/bid
* If no match,it will stay on the order book until its matched
* Used this video to understand and model the book and limit/market order https://www.youtube.com/watch?v=Kl4-VJ2K8Ik
* You can use the command in the bomb.sh file and change the side to bomb the server with buy/sell orders and view the trades endpoint
* Open orders endpoint : http://localhost:8888/v1/orders/
* Place order endpoint http://localhost:8888/v1/orders/limit
* View trades endpoint http://localhost:8888/v1/trades/
* View price matching levels http://localhost:8888/v1/orders/prices?price=<price>&side=<side>&depth=15

# Todo
* generalize for other ticker/currencies other than BTCZAR
* validation and error handling
* maybe a market order too
* auth ( optional )
* possible spring context integration for DI?
