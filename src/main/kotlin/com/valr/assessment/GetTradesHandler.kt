package com.valr.assessment

import com.valr.assessment.enums.CurrencyPair
import com.valr.assessment.enums.Side
import com.valr.assessment.model.Trade
import io.netty.handler.codec.http.HttpHeaderValues
import io.reactivex.rxjava3.core.Observable
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.rxjava3.ext.web.RoutingContext;
import io.vertx.kotlin.core.json.json
import java.math.BigDecimal
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.random.Random

class GetTradesHandler : Handler<RoutingContext>  {
  override fun handle(ctx: RoutingContext) {
    val response = ctx.response()
    response.setChunked(true)
    response.putHeader(io.vertx.core.http.HttpHeaders.CONTENT_TYPE, HttpHeaderValues.TEXT_EVENT_STREAM);

//    ctx.vertx().eventBus().consumer<JsonObject>("tradeAdded")
//      .toFlowable()
//      .subscribe({e-> //... })

    // for now,spit out random trades via server sent events(SSE streams)
    val trade = Trade(
      BigDecimal.valueOf(abs(SecureRandom().nextDouble(50.0))),
      BigDecimal.valueOf(abs(SecureRandom().nextDouble(50.0))),
      if(Random.nextDouble() > 0.5 ) CurrencyPair.BTCZAR else CurrencyPair.BTCUSDT ,
      ZonedDateTime.now(),
      if(Random.nextDouble() > 0.5 ) Side.BUYER else Side.SELLER,
    );

    val obs = Observable.interval(1, TimeUnit.SECONDS)
      .map { LocalDateTime.now() }.share()

    val disposal = obs.subscribe({
      response.write(json { trade }.toString())
    }, ::println, {
      response.end()
    })

    response.closeHandler{
      disposal.dispose()
    }
  }
}
