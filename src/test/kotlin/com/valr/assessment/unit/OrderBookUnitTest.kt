package com.valr.assessment.unit

import com.valr.assessment.enums.CurrencyPair
import com.valr.assessment.enums.Side
import com.valr.assessment.model.Order
import com.valr.assessment.model.OrderBook
import com.valr.assessment.model.OrderBookHolder
import com.valr.assessment.model.OrderLimitMap
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderBookUnitTest {

  private val orderBookHolder = OrderBookHolder(HashMap())

  @BeforeAll
  fun setUp() {
    val sellerOrderLimitMap = OrderLimitMap(Side.SELLER)
    val buyerOrderLimitMap = OrderLimitMap(Side.BUYER)
    val orderBook = OrderBook(buyerOrderLimitMap, sellerOrderLimitMap)

    orderBookHolder.addCurrencyToExchange("BTCZAR", orderBook);
  }

  @BeforeEach
  fun setup() {
    val orderBook = orderBookHolder.getOrderBookByCurrencyPair("BTCZAR")
    orderBook.destroyEverything()
  }

  @Test
  fun test_placed_limit_order_then_orderCount_and_volume_correctly_set() {
    val orderBook = orderBookHolder.getOrderBookByCurrencyPair("BTCZAR")

    val sell = Order(BigDecimal.valueOf(19.99), BigDecimal.valueOf(1), CurrencyPair.BTCZAR, Side.SELLER)
    val sell2 = Order(BigDecimal.valueOf(29.99), BigDecimal.valueOf(2), CurrencyPair.BTCZAR, Side.SELLER)

    orderBook.placeLimitOrder(sell)
    orderBook.placeLimitOrder(sell2)

    val books = orderBook.getOrderBooks()

    val asks = books["sellers"]

    assertTrue(asks!!.isNotEmpty())
    assertEquals(asks.size, 2)
    assertEquals(asks.get(0).orderCount, 1)
    assertEquals(asks.get(0).quantity, sell.quantity)
    assertTrue(asks[0].volume == sell.size())
  }

  @Test
  fun big_enough_buy_order_creates_a_new_bid_and_eats_sell_orders() {
    val orderBook = orderBookHolder.getOrderBookByCurrencyPair("BTCZAR")

    val sell = Order(BigDecimal.valueOf(10), BigDecimal.valueOf(10), CurrencyPair.BTCZAR, Side.SELLER)
    val sell2 = Order(BigDecimal.valueOf(11), BigDecimal.valueOf(10), CurrencyPair.BTCZAR, Side.SELLER)

    val buy = Order(BigDecimal.valueOf(11), BigDecimal.valueOf(21), CurrencyPair.BTCZAR, Side.BUYER)

    orderBook.placeLimitOrder(sell)
    orderBook.placeLimitOrder(sell2)
    orderBook.placeLimitOrder(buy)

    val matches = orderBook.matchOrders(buy)

    orderBook.processOrders(matches)

    val asks = orderBook.sellers.map
    val bids = orderBook.buyers.map

    assertTrue(asks.size == 0)
    assertTrue(bids.size == 1)
    assertTrue(bids.firstEntry().value.orderCount() == 1)
    assertTrue(bids.firstEntry().value.orders.first().quantity.compareTo(BigDecimal.ONE) == 0)
  }

  @Test
  fun test_placed_two_limit_order_then_orderCount_updatedCorrectly() {
    val orderBook = orderBookHolder.getOrderBookByCurrencyPair("BTCZAR")

    val buy = Order(BigDecimal.valueOf(19.99), BigDecimal.valueOf(1), CurrencyPair.BTCZAR, Side.BUYER)
    val sell = Order(BigDecimal.valueOf(19.99), BigDecimal.valueOf(1), CurrencyPair.BTCZAR, Side.SELLER)

    val buy2 = Order(BigDecimal.valueOf(19.99), BigDecimal.valueOf(1), CurrencyPair.BTCZAR, Side.BUYER)
    val sell2 = Order(BigDecimal.valueOf(29.99), BigDecimal.valueOf(1), CurrencyPair.BTCZAR, Side.SELLER)

    orderBook.placeLimitOrder(buy)
    orderBook.placeLimitOrder(buy2)
    orderBook.placeLimitOrder(sell)
    orderBook.placeLimitOrder(sell2)

    val asks = orderBook.sellers.map
    val bids = orderBook.buyers.map

    assertTrue(asks.size == 2)
    assertTrue(bids.size == 1)
    assertTrue(bids.firstEntry().value.orderCount() == 2)
    assertTrue(asks.firstEntry().value.orderCount() == 1)
  }

  @Test
  fun test_placed_two_limit_order_then_both_fill_each_other() {
    val orderBook = orderBookHolder.getOrderBookByCurrencyPair("BTCZAR")

    val buy = Order(BigDecimal.valueOf(19.99), BigDecimal.valueOf(1), CurrencyPair.BTCZAR, Side.BUYER)
    val sell = Order(BigDecimal.valueOf(19.99), BigDecimal.valueOf(1), CurrencyPair.BTCZAR, Side.SELLER)

    orderBook.placeLimitOrder(buy)
    orderBook.placeLimitOrder(sell)

    val matches = orderBook.matchOrders(buy)

    orderBook.processOrders(matches)

    val asks = orderBook.sellers.map
    val bids = orderBook.sellers.map

    // orders removed
    assertTrue(asks.isEmpty())
    assertTrue(bids.isEmpty())
  }

  @Test
  fun bigger_order_should_eat_other_order_away() {
    val orderBook = orderBookHolder.getOrderBookByCurrencyPair("BTCZAR")

    val buy = Order(BigDecimal.valueOf(19.99), BigDecimal.valueOf(1), CurrencyPair.BTCZAR, Side.BUYER)
    val sell = Order(BigDecimal.valueOf(19.99), BigDecimal.valueOf(2), CurrencyPair.BTCZAR, Side.SELLER)

    orderBook.placeLimitOrder(buy)
    orderBook.placeLimitOrder(sell)

    val matches = orderBook.matchOrders(buy)

    orderBook.processOrders(matches)

    val asks = orderBook.sellers.map
    val bids = orderBook.buyers.map

    // partial fill
    assertTrue(asks.isNotEmpty())
    assertTrue(bids.isEmpty())
    assertEquals(asks.firstEntry().value.volume, sell.price)
  }

  @Test
  fun bigger_order_should_eat_other_orders_away_partially() {
    val orderBook = orderBookHolder.getOrderBookByCurrencyPair("BTCZAR")

    val buy = Order(BigDecimal.valueOf(19.99), BigDecimal.valueOf(5), CurrencyPair.BTCZAR, Side.BUYER)
    val sell = Order(BigDecimal.valueOf(19.99), BigDecimal.valueOf(2), CurrencyPair.BTCZAR, Side.SELLER)
    val sell2 = Order(BigDecimal.valueOf(19.99), BigDecimal.valueOf(2), CurrencyPair.BTCZAR, Side.SELLER)

    orderBook.placeLimitOrder(buy)
    orderBook.placeLimitOrder(sell)
    orderBook.placeLimitOrder(sell2)

    val matches = orderBook.matchOrders(buy)

    orderBook.processOrders(matches)

    val asks = orderBook.sellers.map
    val bids = orderBook.buyers.map

    assertTrue(asks.isEmpty())
    assertTrue(bids.isNotEmpty())
    assertEquals(bids.firstEntry().value.volume, buy.price)
  }

  @Test
  // BigDecimal scale thing
  fun bigger_order_should_eat_other_orders_away_partially_small_amount() {
    val orderBook = orderBookHolder.getOrderBookByCurrencyPair("BTCZAR")

    val buy = Order(BigDecimal.valueOf(2), BigDecimal.valueOf(0.5), CurrencyPair.BTCZAR, Side.BUYER)
    val sell = Order(BigDecimal.valueOf(3), BigDecimal.valueOf(0.2), CurrencyPair.BTCZAR, Side.SELLER)
    val sell2 = Order(BigDecimal.valueOf(2), BigDecimal.valueOf(0.2), CurrencyPair.BTCZAR, Side.SELLER)

    orderBook.placeLimitOrder(buy)
    orderBook.placeLimitOrder(sell)
    orderBook.placeLimitOrder(sell2)

    val matches = orderBook.matchOrders(buy)

    orderBook.processOrders(matches)

    val asks = orderBook.sellers.map
    val bids = orderBook.buyers.map

    assertTrue(asks.isNotEmpty())
    assertTrue(bids.isNotEmpty())
    // 0.6
    assertTrue(bids.firstEntry().value.volume.compareTo(BigDecimal.ZERO) != 0)
    assertEquals(bids.firstEntry().value.orderCount(), 1)
    assertEquals(bids.firstEntry().value.orders.first().quantity, BigDecimal.valueOf(0.3))
  }

  @Test
  fun multi_fill_order_test() {
    val buy = Order(BigDecimal.valueOf(11), BigDecimal.valueOf(12), CurrencyPair.BTCZAR, Side.BUYER)
    val buy2 = Order(BigDecimal.valueOf(12), BigDecimal.valueOf(12), CurrencyPair.BTCZAR, Side.BUYER)
    val sell = Order(BigDecimal.valueOf(10), BigDecimal.valueOf(24), CurrencyPair.BTCZAR, Side.SELLER)

    val orderBook = orderBookHolder.getOrderBookByCurrencyPair("BTCZAR")

    orderBook.placeLimitOrder(buy)
    orderBook.placeLimitOrder(buy2)
    orderBook.placeLimitOrder(sell)

    val matches = orderBook.matchOrders(sell)

    orderBook.processOrders(matches)

    val asks = orderBook.sellers.map
    val bids = orderBook.buyers.map

    assertTrue(asks.isEmpty())
    assertTrue(bids.isEmpty())
  }

  // if an order is matched , and filled partially , and if cancelled , it will delete the partially filled order
  // which in practice,it should not be possible.But for simplicity,this is overlooked.This issue can be done via an order status
  // roughly like in removeOrder if(status == OrderStatus.PARTIALLY_FILLED) throw RuntimeException("Can not cancel an already filled order")
  @Test
  fun cancel_order_test() {
    val orderBook = orderBookHolder.getOrderBookByCurrencyPair("BTCZAR")

    val buy = Order(BigDecimal.valueOf(2), BigDecimal.valueOf(0.5), CurrencyPair.BTCZAR, Side.BUYER)

    orderBook.placeLimitOrder(buy)

    assertTrue(orderBook.buyers.map.size == 1)

    orderBook.cancelOrder(buy)
    assertTrue(orderBook.buyers.map.size == 0)
    // limit is deleted too
    assertNull(orderBook.buyers.getOrderLimit(buy.price))
  }
}

