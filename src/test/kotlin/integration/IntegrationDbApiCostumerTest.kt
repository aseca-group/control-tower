package integration

import com.example.module
import com.example.modules.address.model.Addresses
import com.example.modules.customer.model.CreateCustomerDTO
import com.example.modules.customer.model.Customers
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

class IntegrationDbApiCostumerTest {
    @Before
    fun setup() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.create(OrdersProducts)
            SchemaUtils.create(Products)
            SchemaUtils.create(Orders)
            SchemaUtils.create(Customers)
        }
    }

    @After
    fun teardown() {
        transaction {
            SchemaUtils.drop(OrdersProducts)
            SchemaUtils.drop(Products)
            SchemaUtils.drop(Orders)
            SchemaUtils.drop(Customers)
        }
    }

//    @Test
//    fun testGetCustomerById() = withTestApplication(Application::module) {
//        transaction {
//            addLogger(StdOutSqlLogger)
//            SchemaUtils.create(Addresses)
//
//            Customers.insert {
//                it[name] = "Tista"
//            }
//        }
//
//        runBlocking {
//            val response = client.get("/customer/1")
//            //assertEquals(HttpStatusCode.OK, response.status)
//            val responseBody = response.bodyAsText()
//            assert(responseBody.contains("Tista"))
//        }
//    }

    @Test
    fun postCustomer() =
        withTestApplication(Application::module) {
            runBlocking {
                val customerDTO =
                    CreateCustomerDTO(
                        name = "Tista",
                    )

                val call =
                    handleRequest(HttpMethod.Post, "/customer") {
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(Json.encodeToString(customerDTO))
                    }

                assertEquals(HttpStatusCode.OK, call.response.status())
                val responseBody = call.response.content
                println(responseBody)
                assert(responseBody!!.contains("Tista"))
            }
        }

    @Test
    fun testDeleteCustomer() =
        withTestApplication(Application::module) {
            transaction {
                Customers.insert {
                    it[name] = "Tista"
                }
            }

            runBlocking {
                val deleteCall =
                    handleRequest(HttpMethod.Delete, "/customer/1") {
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }

                assertEquals(HttpStatusCode.OK, deleteCall.response.status())
                val deleteResponseBody = deleteCall.response.content
                assert(deleteResponseBody!!.contains("Customer 1 deleted"))

                val getCall =
                    handleRequest(HttpMethod.Get, "/customer/1") {
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }

                assertEquals(HttpStatusCode.OK, getCall.response.status())
                val getResponseBody = getCall.response.content
                assert(getResponseBody!!.contains("Customer not found"))
            }
        }
}
