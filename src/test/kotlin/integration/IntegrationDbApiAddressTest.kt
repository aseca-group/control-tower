package integration

import com.example.module
import com.example.modules.address.model.Addresses
import com.example.modules.address.model.CreateAddressDTO
import com.example.modules.order.model.Orders
import com.example.modules.order.model.OrdersProducts
import com.example.modules.product.model.Products
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Before
import org.junit.Test

class IntegrationDbApiAddressTest {
    @Before
    fun setup() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.create(OrdersProducts)
            SchemaUtils.create(Products)
            SchemaUtils.create(Orders)
            SchemaUtils.create(Addresses)
        }
    }

    @After
    fun teardown() {
        transaction {
            SchemaUtils.drop(OrdersProducts)
            SchemaUtils.drop(Products)
            SchemaUtils.drop(Orders)
            SchemaUtils.drop(Addresses)
        }
    }


    @Test
    fun testGetAddressById() = withTestApplication(Application::module) {
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Addresses)

            Addresses.insert {
                it[city] = "Buenos Aires"
                it[road] = "Bocaaaa"
                it[number] = 7
            }
        }

        runBlocking {
            val response = client.get("/address/1")
            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = response.bodyAsText()
            assert(responseBody.contains("Buenos Aires"))
            assert(responseBody.contains("Bocaaaa"))
            assert(responseBody.contains("7"))
        }
    }

    @Test
    fun postAddress() = withTestApplication(Application::module) {
        runBlocking {
            val addressDTO = CreateAddressDTO(
                city = "Cordoba",
                road = "San Martin",
                number = 10
            )

            val call = handleRequest(HttpMethod.Post, "/address") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(Json.encodeToString(addressDTO))
            }

            assertEquals(HttpStatusCode.OK, call.response.status())
            val responseBody = call.response.content
            println(responseBody)
            assert(responseBody!!.contains("Cordoba"))
            assert(responseBody.contains("San Martin"))
            assert(responseBody.contains("10"))
        }
    }

    @Test
    fun testDeleteAddress() = withTestApplication(Application::module) {
        transaction {
            Addresses.insert {
                it[city] = "Buenos Aires"
                it[road] = "Bocaaaa"
                it[number] = 7
            }
        }

        runBlocking {
            val deleteCall = handleRequest(HttpMethod.Delete, "/address/1") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }

            assertEquals(HttpStatusCode.OK, deleteCall.response.status())
            val deleteResponseBody = deleteCall.response.content
            assert(deleteResponseBody!!.contains("Address 1 deleted"))

            val getCall = handleRequest(HttpMethod.Get, "/address/1") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }

            assertEquals(HttpStatusCode.OK, getCall.response.status())
            val getResponseBody = getCall.response.content
            assert(getResponseBody!!.contains("Address not found"))
        }
    }
}
