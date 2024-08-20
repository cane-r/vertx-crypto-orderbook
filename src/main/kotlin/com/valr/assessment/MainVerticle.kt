package com.valr.assessment

//import io.vertx.core.AbstractVerticle
//import io.vertx.ext.web.Router
import io.reactivex.rxjava3.core.Completable
import io.vertx.core.Launcher
import io.vertx.core.Promise
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.SLF4JLogDelegate
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.rxjava3.core.AbstractVerticle
import io.vertx.rxjava3.core.RxHelper
import io.vertx.rxjava3.ext.web.Router
import io.vertx.rxjava3.ext.web.RoutingContext
import io.vertx.rxjava3.ext.web.handler.BodyHandler
import kotlin.system.exitProcess


class MainVerticle : AbstractVerticle() {
  fun main() {
    Launcher.executeCommand("run", MainVerticle::class.java.name)
  }

  override fun rxStart(): Completable {
    val log = LoggerFactory.getLogger(this.javaClass)

    val logFactory = System.getProperty("org.vertx.logger-delegate-factory-class-name")
    if (logFactory == null) {
      System.setProperty(
        "org.vertx.logger-delegate-factory-class-name",
        SLF4JLogDelegate::class.java.name
      )
    }

    RxHelper
      .deployVerticle(vertx, NewOrderArrivedVerticle())
      .doOnError({e-> log.error(e);exitProcess(1)})
      .doOnSuccess({m -> vertx.deployVerticle(OrderProcessingVerticle())
        .doOnError({e-> log.error(e);exitProcess(1)})
      })
      .subscribe(
        { message -> log.info(message) },
      )


     return vertx.createHttpServer().requestHandler(setupRouters())
      .rxListen(8888)
       .ignoreElement()
      .doOnError{err ->  log.error(err);exitProcess(1)}
//      .subscribe( { message -> log.info("deployed: $message")
//      })
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
      val e  = vertx.eventBus()
      // send to eventbus so order engine can pick up orders asynchronously/concurrently and process them.
      e.publish("newOrderArrived",orderJsonBody)
      //log.info("Order is sent to event bus!")
      ctx.json(
        json {
          obj(
            "message" to "Your order is submitted to be processed.Note that it does not necessarily mean your order will be filled now/in the future!"
          )
        }
      )
  }
}
