package com.valr.assessment.service

import com.valr.assessment.enums.CurrencyPair
import com.valr.assessment.enums.Side
import com.valr.assessment.model.Order
import com.valr.assessment.model.OrderBook
import com.valr.assessment.model.OrderBookHolder
import com.valr.assessment.model.OrderLimitMap
import io.vertx.core.impl.logging.LoggerFactory
import java.util.HashMap

class OrderBookService(
  private val orderBookHolder: OrderBookHolder = OrderBookHolder(HashMap())
  ) {
 // private val orderBook: OrderBook,
  fun placeLimitOrder(currencyPair: CurrencyPair,order: Order) {
    val orderBook = orderBookHolder.getOrderBookByCurrencyPair(currencyPair.toString())
    orderBook.placeLimitOrder(order)
  }

  fun cancelLimitOrder(currencyPair: CurrencyPair,order: Order) {
    val orderBook = orderBookHolder.getOrderBookByCurrencyPair(currencyPair.toString())
    orderBook.cancelOrder()
  }

  fun createOrderBookForCurrency(currencyPair: CurrencyPair) {
    val log = LoggerFactory.getLogger(this.javaClass)
    val orderBookHolder = OrderBookHolder(HashMap())
    val sellerOrderLimitMap = OrderLimitMap(Side.SELLER)
    val buyerOrderLimitMap = OrderLimitMap(Side.BUYER)
    orderBookHolder.addCurrencyToExchange(currencyPair.toString(), OrderBook(buyerOrderLimitMap,sellerOrderLimitMap))
    //log.info(orderBookHolder.getOrderBookByCurrencyPair(currencyPair.toString()))
  }
  fun getOrderBook(currencyPair: CurrencyPair): OrderBook {
    return orderBookHolder.getOrderBookByCurrencyPair(currencyPair.toString());
  }
}
