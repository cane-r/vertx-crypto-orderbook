package com.valr.assessment
import com.valr.assessment.model.OrderBook
import com.valr.assessment.model.OrderBookDto
import io.vertx.core.Handler
import io.vertx.core.impl.logging.LoggerFactory
//import io.vertx.ext.web.RoutingContext
import io.vertx.rxjava3.ext.web.RoutingContext

class GetOpenOrdersHandler : Handler<RoutingContext> {

  override fun handle(ctx: RoutingContext) {
    val log = LoggerFactory.getLogger(this.javaClass)
    val localMapRef = ctx.vertx().sharedData().getLocalMap<String, OrderBook>("OrderBookData");
    val orderBook = localMapRef["orderBook"];

    val books = orderBook?.getOrderBooks();

    val bids = books?.get("buyers")
    val asks = books?.get("sellers")

    val orderBookDto = OrderBookDto(asks,bids);
    //log.info("Returning from OpenOrdersHandler $orderBookDto")
    ctx.json(orderBookDto)
  }
}
