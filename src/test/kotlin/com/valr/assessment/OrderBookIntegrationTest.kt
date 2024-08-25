package com.valr.assessment


import com.valr.assessment.enums.CurrencyPair
import com.valr.assessment.enums.Side
import com.valr.assessment.model.OrderDTO
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.TestScheduler
import io.vertx.core.http.HttpMethod
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.rxjava3.core.RxHelper
import io.vertx.rxjava3.core.Vertx
import io.vertx.rxjava3.core.http.HttpClientResponse
import io.vertx.rxjava3.ext.web.client.WebClient
import kotlinx.coroutines.awaitAll
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.util.concurrent.TimeUnit


//@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension::class)
class OrderBookIntegrationTest {

  private val vertx = Vertx.vertx();

  private val client : WebClient = WebClient.create(vertx);

  private val log = LoggerFactory.getLogger(this.javaClass)


  @BeforeEach
  fun setUp(testContext: VertxTestContext) {
//    vertx.deployVerticle(MainVerticle())
//      .doOnSuccess({
//        vertx.deployVerticle(OrderProcessingVerticle())
//          .doOnSuccess({
//            log.info("deployed verticle ")
//            testContext.completeNow()
//          }).doOnError({err -> testContext.failNow(err)})
//      }).doOnError({err -> testContext.failNow(err)}).
//      subscribe({s -> testContext.verify {
//            log.info("deployed verticle : $s")
//           testContext.completeNow()
//          }})

    RxHelper
      .deployVerticle(vertx, MainVerticle())
      .doOnError({e-> log.error(e)})
      .doOnSuccess({m -> vertx.deployVerticle(OrderProcessingVerticle())
        .doOnError({e-> log.error(e)})
      })
      .doOnSuccess({s -> log.info("$")})
      .subscribe(
        { message: String? ->
          testContext.verify {
            log.info("deployed verticle : $message")
            testContext.completeNow()
          }
        },
        { t -> testContext.failNow(t) })
  }

  @AfterEach
  fun tearDown(testContext: VertxTestContext) {
    vertx.close().subscribe() {
      testContext.completeNow()
    }
  }

  //@Order(2)
  @Test
  fun test_placing_limit_order (testContext: VertxTestContext) {
    val dto = OrderDTO(BigDecimal.TEN,BigDecimal.TEN,CurrencyPair.BTCZAR,Side.BUYER)

    client.post(8888,"localhost","/v1/orders/limit")
      .sendJson(dto)
      .doOnSuccess({s ->
        client.get(8888,"localhost","/v1/orders/")
          .rxSend()
          .doOnError({ err -> testContext.failNow(err)})
          .subscribe(
            {resp -> testContext.verify({
              assertNotNull(resp.body().toString());
              assertEquals(resp.statusCode(),200)
              testContext.completeNow()
            })}, { err->
              testContext.failNow(err)
            }
          )
      })
      .subscribe(
        {resp -> testContext.verify({
          assertEquals(resp.statusCode(),200)
          assertTrue(resp.body().toString().contains("Your order is submitted"))
          //testContext.completeNow()
        })}, { err->
          //testContext.failNow(err)
        }
      )
//
//    client.get(8888,"localhost","/v1/orders/")
//      .rxSend()
//      //.doOnError({ err -> testContext.failNow(err)})
//      .subscribe(
//        {resp -> testContext.verify({
//          log.info("test_placing_limit_order ${resp.body()}")
//          assertNotNull(resp.body().toString());
//          assertEquals(resp.statusCode(),200)
//          testContext.completeNow()
//        })}, { err->
//          testContext.failNow(err)
//        }
//      )

  }
  @Test
  fun test_get_order_book (testContext: VertxTestContext) {
    client.get(8888,"localhost","/v1/orders/")
      .rxSend()
      //.doOnError({ err -> testContext.failNow(err)})
      .subscribe(
        {resp -> testContext.verify({
          assertNotNull(resp.body().toString());
          assertEquals(resp.statusCode(),200)
          testContext.completeNow()
        })}, { err->
          testContext.failNow(err)
        }
      )
  }
}
