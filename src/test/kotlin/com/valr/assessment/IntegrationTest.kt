package com.valr.assessment


//import io.vertx.core.Vertx
import com.valr.assessment.enums.CurrencyPair
import com.valr.assessment.enums.Side
import com.valr.assessment.model.OrderDTO
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.rxjava3.core.RxHelper
import io.vertx.rxjava3.core.Vertx
import io.vertx.rxjava3.ext.web.client.WebClient
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension::class)
class IntegrationTest {

  private val vertx = Vertx.vertx();

  //private val client: HttpClient = vertx.createHttpClient();

  private var client : WebClient = WebClient.create(vertx);

  @BeforeAll
  fun setUp(testContext: VertxTestContext) {
    val log = LoggerFactory.getLogger(this.javaClass)
    RxHelper
      .deployVerticle(vertx, MainVerticle())
      .subscribe(
        { message: String? ->
          testContext.verify {
            log.info("deployed: $message")
            testContext.completeNow()
          }
        },
        { t -> testContext.failNow(t) })
  }
  @AfterAll
  fun tearDown(testContext: VertxTestContext) {
    vertx.close().subscribe() {
      testContext.completeNow()
    }
  }

  @Order(2)
  @Test
  fun test_placing_limit_order (testContext: VertxTestContext) {
    val log = LoggerFactory.getLogger(this.javaClass)
    val dto = OrderDTO(BigDecimal.TEN,BigDecimal.TEN,CurrencyPair.BTCZAR,Side.BUYER)

    client.post(8888,"localhost","/v1/orders/limit")
      .sendJson(dto)
      .subscribe(
        {resp -> testContext.verify({
          assertEquals(resp.statusCode(),200)
          assertTrue(resp.body().toString().contains("Your order is submitted"))
          testContext.completeNow()
        })}, { err->
          testContext.failNow(err)
        }
      )
  }
  @Test
  @Order(1)
  fun test_get_order_book (testContext: VertxTestContext) {
    val log = LoggerFactory.getLogger(this.javaClass)
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
