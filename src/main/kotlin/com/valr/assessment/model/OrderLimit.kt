package com.valr.assessment.model

import com.valr.assessment.enums.Side
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.shareddata.Shareable
import java.io.Serializable
import java.math.BigDecimal
import java.util.concurrent.ConcurrentSkipListSet


/***
 * Limit is an orders list, sitting at a price level
 */
class OrderLimit (
  var price: BigDecimal,
  // orders will be processed as fifo manner,sorted by their timestamp. First order gets to be filled first.
  // linkedlist / arraylist can be used too
  // for thread safety(vertx) choose to use a concurrent tree set
  var orders: ConcurrentSkipListSet<Order>,
  var volume: BigDecimal,
) : Serializable,Shareable {
  constructor(price: BigDecimal) : this(price, ConcurrentSkipListSet<Order>{ o1, o2 -> o1.timestamp.compareTo(o2.timestamp)  },BigDecimal.ZERO)

  lateinit var map: OrderLimitMap

  override fun toString(): String {
    return "OrderLimit(price=$price,orders=$orders,volume=$volume)"
  }
  fun addOrderToLimit(order: Order) {
    val log = LoggerFactory.getLogger(this.javaClass)
    this.orders.add(order);
    val vol = order.size()
    volume = volume.add(vol);
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
    // if(order.status == OrderStatus.PARTIALLY_FILLED) // Raise exception
    orders.remove(order)
    volume = volume.subtract(order.size())
    // if no orders left in this limit,remove this limit too
    if(orders.isEmpty()) {
      map.removeLimit(this)
    }
  }

//  fun removeOrder(orderlist:List<Order>) {
//    orders.removeAll(orderlist.toSet())
//    //volume -= orderlist.stream().map { o -> o.size() }.s
//    // if no orders left in this limit,remove this limit too
//    if(orders.isEmpty()) {
//      map.removeLimit(this)
//    }
//  }

  fun fill(order: Order) : List<OrderMatch>{

    val matches = mutableListOf<OrderMatch>()
    val log = LoggerFactory.getLogger(this.javaClass)

    for(o in orders) {
      if(order.isOrderFilled()) {
        break
      }
      val orderMatch : OrderMatch = fillOrder(o,order)
      matches.add(orderMatch)
      volume = volume.subtract(orderMatch.fillSize.multiply(orderMatch.price))

//      if(order1.isOrderFilled()) {
//        ordersToDelete.add(order1)
//      }
//      else {
//        // partial fill,so update the quantity
//        updateOrder(order1)
//      }
//      if(order2.isOrderFilled()) {
//        ordersToDelete.add(order2)
//      }
//      else {
//        // partial fill,so update the quantity
//        updateOrder(order2)
//      }
    }
    //orders.removeAll(ordersToDelete)
    return matches
  }
  // just remove old entity,and add updated one
  // since order's compareTo method only interested in orderId,its place in the ordering won't be changed.
  // complexity will be O(logN)(removal) + O(logN)(addition)
  fun updateOrder(order: Order) {
    val log = LoggerFactory.getLogger(this.javaClass)
    volume = order.size()
    orders.remove(order)
    orders.add(order)
  }
}
