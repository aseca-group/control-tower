package integration

import com.example.module
import com.example.modules.address.model.Addresses
import com.example.modules.address.model.CreateAddressDTO
import com.example.modules.customer.model.CreateCustomerDTO
import com.example.modules.customer.model.Customers
import com.example.modules.inventory.model.CreateInventoryDTO
import com.example.modules.inventory.model.Inventories
import com.example.modules.order.model.*
import com.example.modules.product.model.CreateProductDTO
import com.example.modules.product.model.Products
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import io.netty.handler.codec.http.HttpHeaders.addHeader
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull

class IntegrationCreateOrderTest {

    private val cl =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                    },
                )
            }
        }

    @Before
    fun setUp(): Unit = withTestApplication({module()}) {
        System.setProperty("env", "test")
    }

    @After
    fun tearDown(): Unit = withTestApplication({ module() }) {
        if (System.getProperty("env") == "test") {
            transaction {
                SchemaUtils.drop(Addresses, Customers, Orders, Products, OrdersProducts, Inventories)
            }
        }
        System.clearProperty("env")
    }

    @Test
    fun test001_postADriver() : Unit = withTestApplication({module()}) {
        runBlocking {
            val driver = """{
                   "name": "TistaDriver"
            }"""
            val response = cl.post("http://localhost:8081/driver"){
                contentType(ContentType.Application.Json)
                setBody(driver)
            }
            assertEquals(response.status, HttpStatusCode.OK)
        }
    }

    @Test
    fun test002_getAPostedDriver() : Unit = withTestApplication({module()}) {
        runBlocking {
            val driver = """{
                   "name": "TistaDriver"
            }"""
            cl.post("http://localhost:8081/driver"){
                contentType(ContentType.Application.Json)
                setBody(driver)
            }

            val resp = cl.get("http://localhost:8081/driver/1")
            val body = resp.bodyAsText()
            assertNotNull(body)
            assert(body.contains("TistaDriver"))
        }
    }

    @Test
    fun test003_createADelivery(): Unit = withTestApplication({module()}) {
        runBlocking {
            val driver = """{
                   "name": "TistaDriver"
            }"""
            cl.post("http://localhost:8081/driver"){ //post a driver
                contentType(ContentType.Application.Json)
                setBody(driver)
            }

            val orderDto = """{
                "addressId": 1   
            }"""
            cl.post("http://localhost:8081/delivery") { //creates a delivery
                contentType(ContentType.Application.Json)
                setBody(orderDto)
            }
            val response = cl.get("http://localhost:8081/delivery/1") //gets the delivery
            val body = response.bodyAsText()
            assertNotNull(body)
        }
    }

    @Test
    fun test004_createAnOrder(): Unit = withTestApplication({module()}) {
        runBlocking {
            val product1 = CreateProductDTO(2.2, "banana")
            val product2 = CreateProductDTO(3.5, "apple")
            val customer = CreateCustomerDTO("Tistaaaa")
            val driver = """{
                   "name": "TistaDriver"
            }"""
            cl.post("http://localhost:8081/driver"){ //post a driver
                contentType(ContentType.Application.Json)
                setBody(driver)
            }

            handleRequest(HttpMethod.Post, "/product") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(Json.encodeToString(product1))
            }

            handleRequest(HttpMethod.Post, "/product") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(Json.encodeToString(product2))
            }

            handleRequest(HttpMethod.Post, "/customer") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(Json.encodeToString(customer))
            }

            val createOrderDTO = CreateOrderDTO(
                listOf(ProductQty(1, 2), ProductQty(2, 3)),
                1,
                1
            )

            val addressDTO = CreateAddressDTO(city = "Cordoba", road = "San Martin", number = 10)

            handleRequest(HttpMethod.Post, "/address") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(Json.encodeToString(addressDTO))
            }

            handleRequest(HttpMethod.Post, "/order") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(Json.encodeToString(createOrderDTO))
            }

            val delivery = cl.get("http://localhost:8081/delivery/1")
            assertNotNull(delivery)
            val content = delivery.bodyAsText()
            assert(content.contains("1"))
            assert(content.contains("PENDING"))

            transaction {
                SchemaUtils.drop()
            }
        }
    }
}
