package com.valr.assessment.model

import com.valr.assessment.enums.CurrencyPair
import com.valr.assessment.model.Order
import java.math.BigDecimal

data class OrderMatch ( val ask : Order,
                        val bid : Order,
                        val fillSize : BigDecimal,
                        val price : BigDecimal,
                        val currencyPair: CurrencyPair)
