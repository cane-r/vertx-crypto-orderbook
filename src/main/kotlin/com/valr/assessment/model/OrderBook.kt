package com.valr.assessment.model

import com.valr.assessment.enums.Side
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.Json
import io.vertx.core.shareddata.Shareable
import io.vertx.rxjava3.core.Vertx
import java.math.BigDecimal
import java.util.*

/***
 * Order book class
 */
class OrderBook (val buyers : OrderLimitMap,
                 val sellers : OrderLimitMap,
                 // for event propagation to verticles/handlers
                  val vertx: Vertx,
                  ) : Shareable{

  fun removeOrderFromBook (order: Order) {
    if(order.side == Side.BUYER)
      buyers.getOrderLimit(order.price)?.removeOrder(order)
    else
      sellers.getOrderLimit(order.price)?.removeOrder(order)
  }

  fun updateOrderQuantity(order:Order) {
    if(order.side == Side.BUYER) {
      buyers.getOrderLimit(order.price)?.updateOrder(order);
    }
    else
      sellers.getOrderLimit(order.price)?.updateOrder(order)
  }

  fun placeLimitOrder(order: Order) {
    //lateinit ?
    var limit : OrderLimit?;
    val log = LoggerFactory.getLogger(this.javaClass)

    if(order.side == Side.BUYER) {
      limit = buyers.getOrderLimit(order.price);
    }
    else {
      limit = sellers.getOrderLimit(order.price)
    }
    if(Objects.isNull(limit)) {
      limit = OrderLimit(order.price)

      if(order.side == Side.BUYER) {
        buyers.addLimit(limit);
      }
      else {
        sellers.addLimit(limit);
      }
    }

    limit?.addOrderToLimit(order);

    // it has been put to the book,now process it.
    vertx.eventBus().publish("limitOrderPlaced", Json.encode(order))
  }
  // experimental
  fun destroyEverything() {
    this.buyers.map.clear()
    this.sellers.map.clear()
  }

  fun cancelOrder () {}

  fun getOrderBooks () : Map<String,List<OrderResponseDto>?> {
    val res = mutableMapOf<String,List<OrderResponseDto>?>();

    val buyerOrders = this.buyers.orders()
    val sellerOrders = this.sellers.orders()

    res["buyers"] = buyerOrders;
    res["sellers"] = sellerOrders;

    return res;
  }
  override fun toString(): String {
    return "OrderBook(buyers=$buyers,seller=$sellers)"
  }

  /**
   If the side is BUY,match the given order with orders at the given order's price level
   or lower in the asks.
   *
   *
   If side is SELL,match the given order with orders at the given order's price level
   or higher in the bids
   */
  fun getPossibleLimitPrices(order: Order,maxDepth:Int) : List<BigDecimal> {
    return getPossibleLimitPrices(order.price,order.side,maxDepth);
  }

  // go up and down in the price levels according to the side.
  // floorKey is used for when there is no price level and it gets the nearest one
  // lowerKey is used for getting down and up in the price levels
  fun getPossibleLimitPrices(p: BigDecimal,side : Side,maxDepth:Int) : List<BigDecimal> {
    var price = p;

    val sellList = mutableListOf<BigDecimal>()
    val buyList = mutableListOf<BigDecimal>()

    if(side == Side.SELLER) {
      // if no record in the opposite book or price is bigger than best bid,no match
      if(buyers.map.isEmpty() || price > getBestBid()) {
        return mutableListOf()
      }
      // match if price is exactly matched
      if(buyers.map.containsKey(price)) {
        sellList.add(price)
        //  if(sellList.size == maxDepth)return sellList
      }
      //otherwise get the next price level
      if(buyers.map.isNotEmpty() && !buyers.map.containsKey(price)) {
        price = buyers.map.floorKey(price)
        sellList.add(price)
      }
      // get all possible price levels
      while (buyers.map.lowerKey(price) != null) {
        price = buyers.map.lowerKey(price);
        sellList.add(price)
        if (buyers.map.ceilingKey(price) == null) {
          break
        }
      }
    }
    else {
      // if no record in the opposite book or price is less than best ask,no match
      if(sellers.map.isEmpty() || price < getBestAsk()) {
        return mutableListOf()
      }
      // match if price is exactly matched
      if(sellers.map.containsKey(price)) {
        buyList.add(price)
      }
      //otherwise get the next price level
      if(sellers.map.isNotEmpty() && !sellers.map.containsKey(price)) {
        price = sellers.map.floorKey(price)
        buyList.add(price)
      }
      // get all possible price levels
      while (sellers.map.lowerKey(price) != null) {
        price = sellers.map.lowerKey(price);
        buyList.add(price)
        if (sellers.map.lowerKey(price) == null) {
          break
        }
      }
    }
    return if(side == Side.BUYER) buyList else sellList;
  }

  private fun getBestAsk() : BigDecimal? {
    return this.sellers.map.firstEntry()?.value?.price;
  }
  private fun getBestBid() : BigDecimal? {
    return this.buyers.map.firstEntry()?.value?.price;
  }

  // maybe introduce a spread ?
  // for example if there is only a 1 BUYER in the order book at the price 3330
  // and a SELL order comes at 2000 , it will match ..
  // for simplicity,I assume this will never happen
  private fun canMatchOrders(order: Order) : Boolean {
    if(order.side == Side.BUYER) {
      if(this.sellers.map.isEmpty()) {
        return false;
      }
      val bestAsk = getBestAsk();

      return order.price >= bestAsk
    }
    else {
      if(this.buyers.map.isEmpty()) {
        return false;
      }
      val bestBid = getBestBid();

      return order.price <= bestBid
    }
  }

  // it will be called by the order matching engine verticle.
  // after receiving order placed event
  fun matchOrders(order: Order) : List<OrderMatch> {
    val isBuyer = order.side == Side.BUYER;
    val matches = mutableListOf<OrderMatch>();

    if(canMatchOrders(order)) {

      // I thought of getting the all possible price levels because for example suppose
      // there is a two BUY order sitting at the price level 3000$/10 quantity and 3001$/10 quantity , which makes 20 quantity total
      // if an order comes with 2999$/20 quantity , it will eat away these BUY orders and order book will become empty
      val orders = getPossibleLimitPrices(order.price,order.side,0)

      // start from the highest if you are a seller,lowest if you are buyer
      // you can test these price levels at the endpoint
      // http://localhost:8888/v1/orders/prices?price=3000&side=BUYER&depth=15
      for(limitPrice in orders.reversed()) {
        if(order.isOrderFilled()) {
          break
        }
        var limit : OrderLimit?;
        if(isBuyer) {
          limit = sellers.getOrderLimit(limitPrice);
        }
        else {
          limit = buyers.getOrderLimit(limitPrice);
        }
        val orderMatches = limit?.fill(order)
        if(orderMatches != null) {
          matches.addAll(orderMatches);
        }
      }
    }
    return matches;
  }
  // it will/maybe useful in market orders
  // loop and eat all orders as aggressively as it can
  // unused right now
  fun orderMatchLoop() {
    val log = LoggerFactory.getLogger(this.javaClass)
    while (true) {
      if(this.sellers.map.isEmpty() || this.buyers.map.isEmpty()) {
        break;
      }

      val bestSellLimit = this.sellers.map.firstEntry();
      val bestBuyLimit = this.buyers.map.firstEntry();

      if(bestBuyLimit.key < bestSellLimit.key) {
        break
      }

      while(!this.sellers.map.isEmpty() && !this.sellers.map.isEmpty()) {
        val bidOrders : TreeSet<Order> = this.buyers.map.firstEntry().value.orders
        val sellOrders : TreeSet<Order> = this.sellers.map.firstEntry().value.orders

        while (!bidOrders.isEmpty() || !sellOrders.isEmpty() ) {
          val buy  = bidOrders.first()
          val sell = sellOrders.first()

          val min = buy.quantity.min(sell.quantity);
          buy.fill(min)
          sell.fill(min)

          if(buy.isOrderFilled()) {
            removeOrderFromBook(buy)
          }
          else {
            updateOrderQuantity(buy)
          }
          if(sell.isOrderFilled()) {
            removeOrderFromBook(sell)
          }
          else {
            updateOrderQuantity(sell)
          }

          val match = OrderMatch(buy,sell,min,buy.price,buy.currencyPair)
          log.info(match)
        }
      }
    }
  }
}
