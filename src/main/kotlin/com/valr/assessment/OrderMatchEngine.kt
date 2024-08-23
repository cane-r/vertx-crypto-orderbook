package com.valr.assessment

import com.valr.assessment.enums.Side
import com.valr.assessment.model.Order
import com.valr.assessment.model.OrderBook
import com.valr.assessment.model.OrderMatch
import com.valr.assessment.model.Trade
import io.reactivex.rxjava3.core.Maybe
import io.vertx.core.Promise
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import io.vertx.rxjava3.core.AbstractVerticle
import io.vertx.rxjava3.core.Vertx
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.stream.Collectors

class OrderMatchEngine() : AbstractVerticle() {
  override fun start(startPromise: Promise<Void>) {

    val log = LoggerFactory.getLogger(this.javaClass)

    val localMapRef = vertx.sharedData().getLocalMap<String, OrderBook>("OrderBookData")
    val tradesRef = vertx.sharedData().getLocalMap<String,List<Trade>>("TradesData")

    vertx.eventBus().consumer<String>("limitOrderPlaced").toFlowable()
      .onBackpressureBuffer(10000)
      .subscribe { m ->
        val order = JsonObject(m.body()).mapTo(Order::class.java)

        val orderBook = localMapRef["orderBook"]

        if(orderBook != null) {
          // try to match the placed limit order

          // or try to match them all in an infinite loop ?
          /*
            while (true) {
            if (bids.isEmpty() || asks.isEmpty()) {
                break;
            }
           */
          val matches = orderBook.matchOrders(order)
          if(matches.isNotEmpty())
          log.info(matches)
          val incomingTrades = mapMatchesToTrades(matches)

          for(match in matches) {

            val o1 = match.ask
            val o2 = match.bid;

            if(o1.isOrderFilled()) {
              orderBook.removeOrderFromBook(o1)
            }
            else {
              // partial fill,so update the quantity
              orderBook.updateOrderQuantity(o1)
            }
            if(o2.isOrderFilled()) {
              orderBook.removeOrderFromBook(o2)
            }
            else {
              // partial fill,so update the quantity
              orderBook.updateOrderQuantity(o2)
            }
          }
          val trades = tradesRef["trades"]

          if(trades.isNullOrEmpty()) {
            val list = mutableListOf<Trade>();

            list.addAll(incomingTrades)

            tradesRef.put("trades", list)
          }

          if(!trades.isNullOrEmpty()) {
            val combined = mutableListOf<Trade>()

            combined.addAll(trades)
            combined.addAll(incomingTrades)

            tradesRef.put("trades", combined)
          }

        }
        m.reply("ok")
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
