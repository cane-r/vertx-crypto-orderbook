package com.valr.assessment.model

import com.valr.assessment.enums.CurrencyPair
import com.valr.assessment.enums.Side
import io.vertx.core.impl.logging.LoggerFactory
import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger
import java.security.SecureRandom
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

) : Serializable {
  private lateinit var orderLimit: OrderLimit

  constructor() : this(BigDecimal.ZERO,BigDecimal.ZERO,CurrencyPair.BTCUSDT,Side.SELLER)
  constructor(dto : OrderDTO) : this(dto.price,dto.quantity,dto.currencyPair,dto.side)

  override fun toString(): String {
    return "Order(price=$price,quantity=$quantity,currencyPair=$currencyPair,side=$side,orderId=$orderId,timestamp=$timestamp)"
  }
  fun orderFilled() : Boolean {
    return quantity == BigDecimal.ZERO;
  }
  fun setLimit(orderLimit: OrderLimit) {
    this.orderLimit=orderLimit;
  }
  fun size() : BigDecimal {
    return price*quantity;
  }
 }

