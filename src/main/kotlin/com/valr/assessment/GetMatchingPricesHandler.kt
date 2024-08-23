package com.valr.assessment

import com.valr.assessment.enums.Side
import com.valr.assessment.model.OrderBook
import io.vertx.core.Handler
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.rxjava3.ext.web.RoutingContext
import java.math.BigDecimal

/*
* Test class to see if price matching works
* http://localhost:8888/v1/orders/prices?price=<price>&side=BUYER&depth=15
* */
class GetMatchingPricesHandler : Handler<RoutingContext> {

  override fun handle(ctx: RoutingContext) {

    val price : BigDecimal = ctx.request().getParam("price").toBigDecimal() ?: BigDecimal.ZERO;
    val side : Side = Side.valueOf(ctx.request().getParam("side"))
    val depth = ctx.request().getParam("depth").toInt()

    val localMapRef = ctx.vertx().sharedData().getLocalMap<String, OrderBook>("OrderBookData");
    val orderBook = localMapRef["orderBook"];

    val matchedPrices = orderBook?.getPossibleLimitPrices(price,side,depth)


    ctx.json(
      json {
        obj(
          "prices" to "${matchedPrices?.reversed()}"
        )
      }
    )

  }
}
