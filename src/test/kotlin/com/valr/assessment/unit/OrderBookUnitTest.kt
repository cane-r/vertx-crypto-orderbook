package com.valr.assessment.unit

import com.valr.assessment.enums.CurrencyPair
import com.valr.assessment.enums.Side
import com.valr.assessment.model.Order
import com.valr.assessment.model.OrderBook
import com.valr.assessment.model.OrderLimitMap
import io.mockk.MockKCancellation
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.TestScheduler
import io.vertx.junit5.VertxExtension
import io.vertx.rxjava3.core.Vertx
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension::class)
//@ExtendWith(MockKExtension::class)
class OrderBookUnitTest {

  private lateinit var orderBook: OrderBook

  //@MockK
  private val vertx = Vertx.vertx()

  @BeforeAll
  fun setup() {
    val sellerOrderLimitMap = OrderLimitMap(Side.SELLER)
    val buyerOrderLimitMap = OrderLimitMap(Side.BUYER)

    orderBook = OrderBook(buyerOrderLimitMap,sellerOrderLimitMap,vertx)
  }

  @Test
  fun test() {

    val testScheduler = TestScheduler()
    RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

    val sell =  Order(BigDecimal.valueOf(19.99), BigDecimal.valueOf(1), CurrencyPair.BTCZAR,Side.SELLER)
    val seller2 =  Order(BigDecimal.valueOf(19.99), BigDecimal.valueOf(2), CurrencyPair.BTCZAR,Side.SELLER)

    orderBook.placeLimitOrder(sell)

    orderBook.placeLimitOrder(seller2)

    testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

    val books = orderBook.getOrderBooks()

    val asks = books.get("sellers")

    assertTrue(asks!!.isNotEmpty())
    assertEquals(asks.size , 1)
    assertEquals(asks.get(0).orderCount,2)
    assertTrue(asks[0].volume == BigDecimal.valueOf(59.97))

//    Awaitility.await().atMost(Duration.ofSeconds(3))
//      .untilAsserted({
//        if (asks != null) {
//          assertTrue(asks.isNotEmpty())
//        }
//      })
  }
}
