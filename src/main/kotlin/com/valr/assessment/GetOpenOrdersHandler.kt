package com.valr.assessment
import com.valr.assessment.model.Order
import io.vertx.core.Handler
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj

class GetOpenOrdersHandler : Handler<RoutingContext> {

  override fun handle(ctx: RoutingContext) {
    val log = LoggerFactory.getLogger(this.javaClass)
    val localMapRef = ctx.vertx().sharedData().getLocalMap<String, List<Order>>("DATA");

    log.info("Getting open orders!")
    val orders = localMapRef["openorders"];

    ctx.json(json {
      obj(
        "orderSize" to orders?.size,
        "orders" to "$orders",
      )
    })
  }
}
