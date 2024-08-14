package com.valr.assessment

import com.valr.assessment.model.Order
import com.valr.assessment.model.OrderDTO
import io.reactivex.rxjava3.schedulers.Schedulers
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.rxjava3.core.eventbus.EventBus;

class NewOrderArrivedVerticle : AbstractVerticle () {
  override fun start(startPromise: Promise<Void>) {
    val log = LoggerFactory.getLogger(this.javaClass)
//    vertx.eventBus().consumer<JsonObject>("orderCreated") { o ->
//      var order: OrderDTO = o.body().mapTo(OrderDTO::class.java);
//      log.info("Received!!! $order");
//      vertx.eventBus().send("str","orderJsonBody");
//      val localMapRef = vertx.sharedData().getLocalMap<String,JsonObject>("DATA");
//      localMapRef["data"] = o.body();
//
//    };
    val e  = EventBus(vertx.eventBus());

    // reactive way of handling new order events with backpressure
    // so order and matching engine can cope with the load
    e.consumer<JsonObject>("newOrderArrived").toFlowable()
      .onBackpressureBuffer(10000)
      .doOnNext { /*message -> log.info("in do on next! $message")*/ }
      .observeOn(Schedulers.computation())
      .doOnError { err -> log.info(err.cause) }
      .subscribe { m ->
        val dto: OrderDTO = m.body().mapTo(OrderDTO::class.java);
        log.info("New Order Received! $dto")
        val obj = JsonObject();
        obj.put("order", Json.encode(Order(dto)))
        e.publish("orderAdded", obj)
        m.reply("ok");
      }


    startPromise.complete();

  }
}
