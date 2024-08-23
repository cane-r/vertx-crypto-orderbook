package com.valr.assessment

import com.valr.assessment.model.Order
import com.valr.assessment.model.OrderDTO
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.vertx.core.Promise
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.rxjava3.core.AbstractVerticle
import java.util.concurrent.TimeUnit

class NewOrderArrivedVerticle : AbstractVerticle () {
  override fun start(startPromise: Promise<Void>) {
    val log = LoggerFactory.getLogger(this.javaClass)

    val eventBus  = vertx.eventBus()

    // reactive way of handling new order events with backpressure
    // this is an intermediary verticle to act as a front event bus
    // so order and matching engine can cope with the load hopefully
    eventBus.consumer<JsonObject>("newOrderArrived").toFlowable()
      .onBackpressureBuffer(10000)
      .concatMap({e -> Flowable.just(e).delay(1, TimeUnit.MILLISECONDS)})
      .doOnNext { /* message -> log.info("in do on next! ${System.nanoTime()} $message") */ }
      .observeOn(Schedulers.computation())
      //.delay(2, TimeUnit.MILLISECONDS)
      .doOnError { err -> log.error(err.cause) }
      .subscribe { m ->
        val dto: OrderDTO = m.body().mapTo(OrderDTO::class.java)
        //log.info("New Order Received! $dto")
        val obj = JsonObject()
        obj.put("order", Json.encode(Order(dto)))
        eventBus.publish("orderAdded", obj)
        m.reply("ok")
      }
    startPromise.complete()
  }
}
