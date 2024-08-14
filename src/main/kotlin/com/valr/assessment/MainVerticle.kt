package com.valr.assessment

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.rxjava3.core.eventbus.EventBus
import kotlin.system.exitProcess


class MainVerticle : AbstractVerticle() {

  override fun start(startPromise: Promise<Void>) {
    vertx
      .createHttpServer()
      /*.requestHandler { req ->
        req.response()
          .putHeader("content-type", "text/plain")
          .end("Hello from Vert.x!")
      }*/
      .requestHandler(setupRouters())
      .listen(8888).onComplete { http ->
        if (http.succeeded()) {
          startPromise.complete()
          println("HTTP server started on port 8888")
        } else {
          startPromise.fail(http.cause());
        }
      }
    val log = LoggerFactory.getLogger(this.javaClass)
    vertx.deployVerticle(NewOrderArrivedVerticle()) { ar ->
      if (ar.succeeded()) {
        vertx.deployVerticle(OrderProcessingVerticle())
      } else {
        log.error("Error!",ar.cause())
        exitProcess(1)
      }
    }
  }

  private fun setupRouters(): Router {
        val router = Router.router(vertx)

        router.route("/v1/orders/limit").handler(BodyHandler.create())
        router.post("/v1/orders/limit").handler(::addOrder)
        router.get("/v1/orders/").handler(GetOpenOrdersHandler())
        router.get("/v1/trades/").handler(GetTradesHandler())
        return router
  }
  fun addOrder(ctx: RoutingContext) {
      val log = LoggerFactory.getLogger(this.javaClass)
      val orderJsonBody: JsonObject = ctx.body().asJsonObject()
      val e  = EventBus(vertx.eventBus());
      // send to eventbus so order engine can pick up orders asynchronously/concurrently and process them.
      e.publish("newOrderArrived",orderJsonBody);
      //log.info("Order is sent to event bus!")
      ctx.json(json {
        obj(
          "message" to "Your order is submitted to be processed.Note that it does not necessarily mean your order will be filled now/in the future!",
        )
      })
  }
}
