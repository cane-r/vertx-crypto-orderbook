package com.valr.assessment.model

import com.valr.assessment.enums.CurrencyPair
import com.valr.assessment.enums.Side
import java.math.BigDecimal
import java.security.SecureRandom
import java.time.ZonedDateTime
import java.util.*
import kotlin.math.abs

/**
* Represents a trade.Once an order is processed,it will become a trade.
 *
* Took reference from : https://api.valr.com/BTCZAR/tradehistory
* */
data class Trade(
  val price: BigDecimal,
  val quantity: BigDecimal,
  val currencyPair: CurrencyPair,
  val tradedAt: ZonedDateTime,
  val takerSide: Side,
  val quoteVolume: BigDecimal = price * quantity,
  val id: UUID = UUID.randomUUID(),
  val sequenceId: Long = abs(SecureRandom().nextLong()),
)

