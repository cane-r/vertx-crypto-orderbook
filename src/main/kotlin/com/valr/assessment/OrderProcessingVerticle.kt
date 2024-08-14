package com.valr.assessment

import com.valr.assessment.model.Order
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject


class OrderProcessingVerticle : AbstractVerticle () {
  override fun start(startPromise: Promise<Void>) {
    val log = LoggerFactory.getLogger(this.javaClass)
    val orderList  = mutableListOf<Order>()
    // for now , add to a list
    vertx.eventBus().consumer<JsonObject>("orderAdded") { message ->
      val b : String = message.body().getString("order");
      val o : Order = JsonObject(b).mapTo(Order::class.java)
      orderList.add(o)
      message.reply("ok")
    }
    val localMapRef = vertx.sharedData().getLocalMap<String,List<Order>>("DATA");
    localMapRef["openorders"] = orderList;
    startPromise.complete();
  }
}
