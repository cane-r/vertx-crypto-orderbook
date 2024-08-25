package com.valr.assessment.model

/*
* Books keeper class that holds all the books in a tuple currency->book fashion
*
* */
class OrderBookHolder (private val map : HashMap<String,OrderBook>)  {

  // like listing a crypto in an exchange
  fun addCurrencyToExchange(name:String,book: OrderBook) {
    this.map[name] = book;
  }
  fun getOrderBookByCurrencyPair (name: String) : OrderBook {
    return this.map[name]!!;
  }
  fun isOrderBookExistsForCurrency(name: String) : Boolean {
    return this.map.containsKey(name)
  }
}
