package com.valr.assessment.model

import com.valr.assessment.enums.CurrencyPair
import com.valr.assessment.enums.Side
import io.vertx.core.shareddata.Shareable
import java.io.Serializable
import java.math.BigDecimal
import java.security.SecureRandom
import java.time.ZonedDateTime
import java.util.*
import kotlin.math.abs

/**
  Represents a trade.Once an order is processed,it will become a trade.

  Took reference from : https://api.valr.com/BTCZAR/tradehistory

  But for simpler approach,bundled buy and sell order in one object with an id( because in real life,there would be a database)
  even if the order is deleted,you can still search though database

 **/
data class Trade(
  val price: BigDecimal,
  val quantity: BigDecimal,
  val currencyPair: CurrencyPair,
  val tradedAt: ZonedDateTime,
  val sellerOrderId : Long,
  val buyerOrderId : Long,
  val quoteVolume: BigDecimal = price * quantity,
  val id: UUID = UUID.randomUUID(),
  val sequenceId: Long = abs(SecureRandom().nextLong()),
) : Shareable,Serializable

