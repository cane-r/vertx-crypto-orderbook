package com.valr.assessment.model

import com.valr.assessment.MainVerticle
import com.valr.assessment.enums.Side
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.logging.SLF4JLogDelegate
import io.vertx.core.shareddata.Shareable
import java.io.Serializable
import java.math.BigDecimal
import java.util.*
import kotlin.math.log


/***
 * Limit is an orders list, sitting at a price level
 */
class OrderLimit (
  var price: BigDecimal,
  // orders will be processed as fifo manner,sorted by their timestamp. First order gets to be filled first.
  // linkedlist / arraylist can be used too
  var orders: TreeSet<Order>,
  var volume: BigDecimal,
) : Serializable,Shareable {
  constructor(price: BigDecimal) : this(price, TreeSet<Order>{ o1, o2 -> o1.timestamp.compareTo(o2.timestamp)  },BigDecimal.ZERO)

  lateinit var map: OrderLimitMap

  override fun toString(): String {
    return "OrderLimit(price=$price,orders=$orders,volume=$volume)"
  }
  fun addOrderToLimit(order: Order) {
    val log = LoggerFactory.getLogger(this.javaClass)
    this.orders.add(order);
    val vol = (order.price)*(order.quantity);
    this.volume += vol;
    order.setLimit(this);
  }
  fun orders () : Set<Order> {
    return orders;
  }
  fun orderCount () : Int {
    return orders.size;
  }
  private fun fillOrder(order1:Order, order2: Order) : OrderMatch {
    var fillSize = BigDecimal.ZERO

    val min = order1.quantity.min(order2.quantity);
    order1.fill(min)
    order2.fill(min)
    fillSize = min

    return if(order1.side == Side.SELLER)
      OrderMatch(order1,order2,fillSize,price,order1.currencyPair)
    else
      OrderMatch(order2,order1,fillSize,price,order2.currencyPair)
  }

  fun removeOrder(order:Order) {
    orders.remove(order)
    volume -= order.size()
    // if no orders left in this limit,remove this limit too
    if(orders.isEmpty()) {
      map.removeLimit(this)
    }
  }

  fun fill(order: Order) : List<OrderMatch>{

    val matches = mutableListOf<OrderMatch>()
    val log = LoggerFactory.getLogger(this.javaClass)

    for(o in orders) {
      if(order.isOrderFilled()) {
        break
      }
      val orderMatch : OrderMatch = fillOrder(o,order)
      matches.add(orderMatch)
      volume -= orderMatch.fillSize
    }
    return matches
  }
  // just remove old entity,and add updated one
  // since order's compareTo method only interested in orderId,its place in the ordering won't be changed.
  // complexity will be O(logN)(removal) + O(logN)(addition)
  fun updateOrder(order: Order) {
    val log = LoggerFactory.getLogger(this.javaClass)
    orders.remove(order)
    orders.add(order)
  }
}
