package com.valr.assessment

import com.valr.assessment.enums.Side
import com.valr.assessment.model.*
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.vertx.core.Promise
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.rxjava3.core.AbstractVerticle
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors


class OrderProcessingVerticle : AbstractVerticle () {

  val log = LoggerFactory.getLogger(this.javaClass)

  override fun start(startPromise: Promise<Void>) {
    val localMapRef = vertx.sharedData().getLocalMap<String, OrderBook>("OrderBookData")
    val tradesRef = vertx.sharedData().getLocalMap<String,List<Trade>>("TradesData")

    // ****** INITIALIZATION OF ORDER BOOKS FOR A GIVEN CURRENCY ,FOR NOW,ONLY FOR BTCZAR ***************
    val orderBookHolder = OrderBookHolder(HashMap())

    val sellerOrderLimitMap = OrderLimitMap(Side.SELLER)
    val buyerOrderLimitMap = OrderLimitMap(Side.BUYER)

    val orderBook = OrderBook(buyerOrderLimitMap,sellerOrderLimitMap)
    localMapRef.put("orderBook",orderBook)
    orderBookHolder.addCurrencyToExchange("BTCZAR", orderBook)
    // *******************************************************************************************
    vertx.eventBus().consumer<String>("limitOrderPlaced").toFlowable()
      .onBackpressureBuffer(10000)
      .concatMap({e -> Flowable.just(e).delay(2, TimeUnit.MILLISECONDS)})
      .subscribe { m ->
        val book = localMapRef["orderBook"]

        val order = JsonObject(m.body()).mapTo(Order::class.java)

        if(book != null) {

          val matchedOrders = book.matchOrders(order)

          val newTrades = mapMatchesToTrades(matchedOrders);

          book.processOrders(matchedOrders)

          val trades = tradesRef["trades"]

          if(trades.isNullOrEmpty()) {
            val list = mutableListOf<Trade>();

            list.addAll(newTrades)

            tradesRef.put("trades", list)
          }

          if(!trades.isNullOrEmpty()) {
            val combined = mutableListOf<Trade>()

            combined.addAll(trades)
            combined.addAll(newTrades)

            tradesRef.put("trades", combined)
          }
        }
      }

    vertx.eventBus().consumer<JsonObject>("orderAdded").toFlowable()
      .onBackpressureBuffer(10000)
      .concatMap({e -> Flowable.just(e).delay(1, TimeUnit.MILLISECONDS)})
      .doOnNext { /* message -> log.info("in do on next! ${System.nanoTime()} $message") */ }
      .observeOn(Schedulers.computation())
      //.delay(3, TimeUnit.MILLISECONDS)
      .doOnError { err -> log.info(err.cause) }
      .subscribe { message ->
        val dto: OrderDTO = message.body().mapTo(OrderDTO::class.java)
        val order = Order(dto)

        val ob = orderBookHolder.getOrderBookByCurrencyPair(order.currencyPair.toString())
        ob.placeLimitOrder(order)

        message.reply("ok")

        vertx.eventBus().publish("limitOrderPlaced", Json.encode(order))
      }
    startPromise.complete()
  }
  private fun mapMatchesToTrades(matches: List<OrderMatch>): List<Trade> {
    val trades : List<Trade> = matches.stream().map { match ->
      val trade = Trade(match.price, match.fillSize, match.currencyPair, ZonedDateTime.now(), match.ask.orderId, match.bid.orderId)
      trade
    }.collect(Collectors.toList())
    return trades
  }
}

