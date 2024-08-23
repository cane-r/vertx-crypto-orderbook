package com.valr.assessment.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.valr.assessment.enums.CurrencyPair
import com.valr.assessment.enums.Side
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.shareddata.Shareable
import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger
import java.security.SecureRandom
import java.util.Comparator
import kotlin.math.abs

/**
 * Represents an order that will be added to the order book.
 *
 * Took reference from : https://api.valr.com/BTCZAR/orderbook
 */
 class Order(
  var price: BigDecimal,
  var quantity: BigDecimal,
  var currencyPair: CurrencyPair,
  var side: Side,
  var orderId: Long = abs(SecureRandom().nextLong()),
  var timestamp: Long = System.nanoTime() ,

) : Serializable , Shareable , Comparator<Order> , Comparable<Order> {
  private lateinit var orderLimit: OrderLimit

  constructor() : this(BigDecimal.ZERO,BigDecimal.ZERO,CurrencyPair.BTCUSDT,Side.SELLER)
  constructor(dto : OrderDTO) : this(dto.price,dto.quantity,dto.currencyPair,dto.side)
  constructor(order : Order) : this(order.price,order.quantity,order.currencyPair,order.side,order.orderId,order.timestamp)

  override fun toString(): String {
    return "Order(price=$price,quantity=$quantity,currencyPair=$currencyPair,side=$side,orderId=$orderId,timestamp=$timestamp)"
  }

  override fun compare(o1: Order, o2: Order): Int {
    return o1.timestamp.compareTo(o2.timestamp)
  }
  @JsonIgnore
  fun isOrderFilled() : Boolean {
    return quantity == BigDecimal.ZERO;
  }
  fun setLimit(orderLimit: OrderLimit) {
    this.orderLimit=orderLimit;
  }
  fun size() : BigDecimal {
    return price*quantity;
  }
  override fun hashCode(): Int {
    return orderId.hashCode()
  }
  fun fill(amount : BigDecimal) {
    if(amount > quantity) {
      throw RuntimeException("Order $orderId can not be filled : remaining $quantity : amount : $amount")
    }
    quantity = quantity.subtract(amount)
  }
  override fun compareTo(other: Order): Int {
    return other.orderId.compareTo(orderId)
  }
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Order

    return orderId == other.orderId
  }
}

