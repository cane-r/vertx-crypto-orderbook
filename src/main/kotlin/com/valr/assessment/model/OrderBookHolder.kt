package com.valr.assessment.model

import io.vertx.core.impl.logging.LoggerFactory

class OrderBookHolder (private val map : HashMap<String,OrderBook>)  {

  // like listing a crypto in an exchange
  fun addCurrencyToExchange(name:String,book: OrderBook) {
    this.map[name] = book;
  }
  fun getOrderBookByCurrencyPair (name: String) : OrderBook {
    return this.map[name]!!;
  }
}
