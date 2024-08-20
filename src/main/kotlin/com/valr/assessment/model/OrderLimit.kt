package com.valr.assessment.model

import com.valr.assessment.MainVerticle
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.logging.SLF4JLogDelegate
import io.vertx.core.shareddata.Shareable
import java.io.Serializable
import java.math.BigDecimal
import java.util.*


/***
 * Limit is an orders list, sitting at a price level
 */
class OrderLimit (
  var price: BigDecimal,
  // orders will be processed as fifo manner,sorted by their timestamp. First order gets to be filled first.
  // linkedlist / arraylist can be used too
  var orders: TreeSet<Order>,
  var volume: BigDecimal
) : Serializable,Shareable {
  constructor(price: BigDecimal) : this(price, TreeSet { o1, o2 -> o2.timestamp.compareTo(o1.timestamp) },BigDecimal.ZERO)

  override fun toString(): String {
    return "OrderLimit(price=$price,orders=$orders,volume=$volume)"
  }
  fun addOrderToLimit(order: Order) {
    val log = LoggerFactory.getLogger(this.javaClass)

    this.orders.add(order);
    val vol = (order.price)*(order.quantity);
    this.volume += vol;
    //log.info("${volume} is now volume of the limit $price ")
    order.setLimit(this);
  }
  fun orders () : Set<Order> {
    return orders;
  }
  fun orderCount () : Int {
    return orders.size;
  }
}
