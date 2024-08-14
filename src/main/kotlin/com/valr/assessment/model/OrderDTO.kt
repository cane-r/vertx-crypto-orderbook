package com.valr.assessment.model

import com.valr.assessment.enums.CurrencyPair
import com.valr.assessment.enums.Side
import java.math.BigDecimal

/***
 * Order DTO that comes to rest controller to be deserialized
 */
data class OrderDTO (
  val price: BigDecimal,
  val quantity: BigDecimal,
  val currencyPair: CurrencyPair,
  val side: Side,) {
    constructor() : this(BigDecimal.ZERO,BigDecimal.ZERO,CurrencyPair.BTCZAR,Side.SELLER)
}

