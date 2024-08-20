package com.valr.assessment

import com.valr.assessment.enums.Side
import com.valr.assessment.model.*
import com.valr.assessment.service.OrderBookService
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.vertx.core.Promise
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import io.vertx.rxjava3.core.AbstractVerticle
import java.util.HashMap
import java.util.concurrent.TimeUnit


class OrderProcessingVerticle : AbstractVerticle () {
  override fun start(startPromise: Promise<Void>) {

    val log = LoggerFactory.getLogger(this.javaClass)

    // ****** INITIALIZATION OF ORDER BOOKS FOR A GIVEN CURRENCY ,FOR NOW,ONLY FOR BTCZAR***************
    // later use orderbook service
    val orderBookHolder = OrderBookHolder(HashMap())

    val sellerOrderLimitMap = OrderLimitMap(Side.SELLER)
    val buyerOrderLimitMap = OrderLimitMap(Side.BUYER)

    orderBookHolder.addCurrencyToExchange("BTCZAR", OrderBook(buyerOrderLimitMap,sellerOrderLimitMap))
    // *******************************************************************************************
    val service = OrderBookService();

    vertx.eventBus().consumer<JsonObject>("orderAdded").toFlowable()
      .onBackpressureBuffer(10000)
      .concatMap({e -> Flowable.just(e).delay(1, TimeUnit.MILLISECONDS)})
      .doOnNext { /* message -> log.info("in do on next! ${System.nanoTime()} $message") */ }
      .observeOn(Schedulers.computation())
      //.delay(3, TimeUnit.MILLISECONDS)
      .doOnError { err -> log.info(err.cause) }
      .subscribe { message ->
        val b: String = message.body().getString("order")
        val o: Order = JsonObject(b).mapTo(Order::class.java)

        val orderBook = orderBookHolder.getOrderBookByCurrencyPair(o.currencyPair.toString())
        orderBook.placeLimitOrder(o)
        message.reply("ok")

        val orderBookMapRef = vertx.sharedData().getLocalMap<String,OrderBook>("OrderBookData")
        orderBookMapRef.put("orderBook",orderBook)
      }
    startPromise.complete()
  }
}

