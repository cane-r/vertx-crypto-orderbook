package com.valr.assessment.model

import com.valr.assessment.enums.CurrencyPair
import com.valr.assessment.enums.Side
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.shareddata.Shareable
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ConcurrentSkipListSet
import java.util.stream.Collectors

/*
* This class holds the price-limit tuple as a map,sorted in ascending/descending order if the side is seller/buyer
* */
class OrderLimitMap (
  private val side:Side,
) : Shareable {
  var map:TreeMap<BigDecimal,OrderLimit>

  init {

     if(side == Side.SELLER) {
       map = TreeMap<BigDecimal,OrderLimit>();
     }
     else {
       map = TreeMap<BigDecimal,OrderLimit>(Collections.reverseOrder());
     }
 }
  fun addLimit(orderLimit: OrderLimit)  {
    map[orderLimit.price] = orderLimit;
    orderLimit.map = this
  }
  fun removeLimit(orderLimit: OrderLimit)  {
    map.remove(orderLimit.price);
  }
  fun size() : Int {
    return this.map.size;
  }
  fun getOrderLimit(limit:BigDecimal): OrderLimit? {
    return this.map[limit];
  }
  override fun toString() : String {
    return this.map.toString();
  }
  fun containsLimit(limit : BigDecimal) : Boolean{
    return this.map.containsKey(limit)
  }
  fun orders() : List<OrderResponseDto>? {
    val orders : List<OrderResponseDto>? = this.map.keys.stream().map { it ->

      val limit : OrderLimit = map[it]!!
      val o : Set<Order> = ConcurrentSkipListSet(limit.orders)
      val count = limit.orderCount()
      val side = side;
      val q = o.stream().map { e -> e.quantity}.reduce(BigDecimal.ZERO, BigDecimal::add);
      val price = limit.price
      val vol = limit.volume;
      val currency = o.first().currencyPair ?: CurrencyPair.BTCZAR
      val obj = OrderResponseDto(side, q,price,currency,count)

      obj
    }.collect(Collectors.toList())
    return orders;
  }
}
