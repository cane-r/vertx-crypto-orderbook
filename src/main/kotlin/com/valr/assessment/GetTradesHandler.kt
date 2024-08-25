package com.valr.assessment

import com.valr.assessment.model.Trade
import io.netty.handler.codec.http.HttpHeaderValues
import io.vertx.core.Handler
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.rxjava3.ext.web.RoutingContext
/*
* Handler for viewing all trades
* */
class GetTradesHandler : Handler<RoutingContext>  {

  private val allTrades = mutableListOf<Trade>()

  override fun handle(ctx: RoutingContext) {
    val response = ctx.response()
    response.setChunked(true)
    response.putHeader(io.vertx.core.http.HttpHeaders.CONTENT_TYPE, HttpHeaderValues.TEXT_EVENT_STREAM);

//    ctx.vertx().eventBus().consumer<JsonObject>("tradeAdded")
//      .toFlowable()
//      .subscribe({e-> //... })


    val log = LoggerFactory.getLogger(this.javaClass)

    val vertx = ctx.vertx()
    val tradesRef = vertx.sharedData().getLocalMap<String, List<Trade>>("TradesData")
    val trades = tradesRef["trades"]

//    if(!Objects.isNull(trades)) {
//      allTrades.addAll(trades)
//    }

    // for now,spit out random trades via server sent events(SSE streams)
//    val trade = Trade(
//      BigDecimal.valueOf(abs(SecureRandom().nextDouble(50.0))),
//      BigDecimal.valueOf(abs(SecureRandom().nextDouble(50.0))),
//      if(Random.nextDouble() > 0.5 ) CurrencyPair.BTCZAR else CurrencyPair.BTCUSDT ,
//      ZonedDateTime.now(),
//      Order(),
//      Order(),
//    )

//    val obs = Observable.interval(1, TimeUnit.SECONDS)
//      .map { LocalDateTime.now() }.share()
//
//    val disposal = obs.subscribe({
//      response.write(json { trade }.toString())
//    }, ::println, {
//      response.end()
//    })
//
//    response.closeHandler{
//      disposal.dispose()
//    }

    ctx.json(
      json {
        obj(
          "tradeCount" to trades.size,
          "trades" to trades
        )
      }
    )
  }
}
