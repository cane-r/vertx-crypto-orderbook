package com.valr.assessment.service

import com.valr.assessment.enums.CurrencyPair
import com.valr.assessment.enums.Side
import com.valr.assessment.model.Order
import com.valr.assessment.model.OrderBook
import com.valr.assessment.model.OrderBookHolder
import com.valr.assessment.model.OrderLimitMap
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.shareddata.Shareable
import io.vertx.rxjava3.core.Vertx
import java.util.HashMap

class OrderBookService(
  private val orderBookHolder: OrderBookHolder = OrderBookHolder(HashMap())
  ) : Shareable {

  fun placeLimitOrder(currencyPair: CurrencyPair,order: Order) {
    val orderBook = getOrderBook(currencyPair)
    orderBook.placeLimitOrder(order)
  }

  fun cancelLimitOrder(currencyPair: CurrencyPair,order: Order) {
    val orderBook = getOrderBook(currencyPair)
    orderBook.cancelOrder()
  }

  fun createOrderBookForCurrency(currencyPair: CurrencyPair) {
    val log = LoggerFactory.getLogger(this.javaClass)
    val orderBookHolder = OrderBookHolder(HashMap())
    val sellerOrderLimitMap = OrderLimitMap(Side.SELLER)
    val buyerOrderLimitMap = OrderLimitMap(Side.BUYER)
    orderBookHolder.addCurrencyToExchange(currencyPair.toString(), OrderBook(buyerOrderLimitMap,sellerOrderLimitMap,
      Vertx.vertx()))
  }

  fun getOrderBook(currencyPair: CurrencyPair): OrderBook {
    if(orderBookHolder.isOrderBookExistsForCurrency(currencyPair.toString())) {
      createOrderBookForCurrency(currencyPair);
    }
    return orderBookHolder.getOrderBookByCurrencyPair(currencyPair.toString());
  }
}
