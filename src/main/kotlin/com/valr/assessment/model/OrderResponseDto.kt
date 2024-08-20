package com.valr.assessment.model

import com.valr.assessment.enums.CurrencyPair
import com.valr.assessment.enums.Side
import java.math.BigDecimal
import java.math.BigInteger
import java.security.Timestamp

data class OrderResponseDto(
  val side: Side,
  val quantity: BigDecimal,
  val price: BigDecimal,
  val currencyPair: CurrencyPair,
  val orderCount: Int,
  val volume : BigDecimal = quantity*price
)
