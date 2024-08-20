package com.valr.assessment.model

data class OrderBookDto(val asks: List<OrderResponseDto>?,
                        val bids: List<OrderResponseDto>?
)
