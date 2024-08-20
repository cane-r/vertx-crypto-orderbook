package com.valr.assessment.model

import com.valr.assessment.enums.Side
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.shareddata.Shareable
import io.vertx.rxjava3.core.Vertx
import java.util.*

/***
 * Order book class
 */
class OrderBook (val buyers : OrderLimitMap,
                 val sellers : OrderLimitMap) : Shareable{

  fun placeLimitOrder(order: Order) {
    var limit : OrderLimit?;
    val log = LoggerFactory.getLogger(this.javaClass)
    if(order.side == Side.BUYER) {
      limit = buyers.getOrderLimit(order.price);
    }
    else {
      limit = sellers.getOrderLimit(order.price)
    }
    if(Objects.isNull(limit)) {
      limit = OrderLimit(order.price)
      //log.info("limit is null!!Adding a new limit")
      if(order.side == Side.BUYER) {
        buyers.addLimit(limit);
      }
      else {
        sellers.addLimit(limit);
      }
    }
    limit?.addOrderToLimit(order);

  }
  fun matchOrder () {
    // if not filled . a new order publish ???
    // vertx.eventBus().publish("");
  }
  fun cancelOrder () {}

  fun getOrderBooks () : Map<String,List<OrderResponseDto>?> {
    val log = LoggerFactory.getLogger(this.javaClass)
    val res = mutableMapOf<String,List<OrderResponseDto>?>();

    val buyerOrders = this.buyers.orders()
    val sellerOrders = this.sellers.orders()

    res["buyers"] = buyerOrders;
    res["sellers"] = sellerOrders;

    return res;
  }
  fun getBestAsk() : Order? {
    return this.buyers.map.firstEntry()?.value?.orders?.first();
  }
  fun getBestBid() : Order? {
    return this.sellers.map.firstEntry()?.value?.orders?.first();
  }
  override fun toString(): String {
    return "OrderBook(buyers=$buyers,seller=$sellers)"
  }
}
