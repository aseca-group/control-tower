@file:Suppress("ktlint:standard:no-wildcard-imports")

package endpoint

import com.example.modules.address.model.Addresses
import com.example.modules.customer.dao.customerDao
import com.example.modules.customer.model.CreateCustomerDTO
import com.example.modules.customer.model.Customer
import com.example.modules.customer.model.Customers
import com.example.modules.inventory.model.Inventories
import com.example.modules.order.model.Orders
import com.example.modules.order.model.OrdersProducts
import com.example.modules.product.model.Products
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import junit.framework.TestCase
import kotlinx.serialization.json.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Before
import kotlin.test.*

class CustomerEndpointTest {
    @Before
    fun init() {
        val driverClassName = "org.h2.Driver"
        val jdbcURL = "jdbc:h2:file:./build/db"
        val database = Database.connect(jdbcURL, driverClassName)
        transaction(database) {
            SchemaUtils.drop(Addresses, Customers, Orders, Products, OrdersProducts, Inventories)
            SchemaUtils.create(Addresses, Customers, Orders, Products, OrdersProducts, Inventories)
        }
    }

    @Test
    fun testPostCustomer() =
        testApplication {
            val client =
                createClient {
                    install(ContentNegotiation) {
                        json()
                    }
                }
            val response =
                client.post("/customer") {
                    contentType(ContentType.Application.Json)
                    setBody(CreateCustomerDTO("Pipo Gorosito"))
                }
            val customer = Json.decodeFromString<Customer>(response.bodyAsText())
            TestCase.assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Pipo Gorosito", customer.name)
        }

    @Test
    fun testGetCustomer() =
        testApplication {
            customerDao.addNewCustomer(CreateCustomerDTO("Pipo Gorosito"))

            val response = client.get("/customer/1")
            TestCase.assertEquals(HttpStatusCode.OK, response.status)
            val customer = Json.decodeFromString<Customer>(response.bodyAsText())
            assertEquals("Pipo Gorosito", customer.name)
        }

    @Test
    fun testGetNotExistingCustomer() =
        testApplication {
            val response = client.get("/customer/1")
            TestCase.assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Customer not found", response.bodyAsText())
        }

    @Test
    fun testGetCustomerWithInvalidID() =
        testApplication {
            val response = client.get("/customer/invalid")
            TestCase.assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Invalid ID", response.bodyAsText())
        }

    @Test
    fun testDeleteCustomer() =
        testApplication {
            customerDao.addNewCustomer(CreateCustomerDTO("Pipo Gorosito"))
            val response = client.delete("/customer/1")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Customer 1 deleted", response.bodyAsText())
        }

    @Test
    fun testDeleteCustomerThatDoesNotExist() =
        testApplication {
            val response = client.delete("/customer/1")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Customer not found", response.bodyAsText())
        }

    @Test
    fun testDeleteCustomerWithInvalidID() =
        testApplication {
            val response = client.delete("/customer/invalid")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Invalid ID", response.bodyAsText())
        }

    @Test
    fun testGetAllCustomer() =
        testApplication {
            customerDao.addNewCustomer(CreateCustomerDTO("Pipo Gorosito"))
            val response = client.get("/customer")
            assertEquals(HttpStatusCode.OK, response.status)
        }

    @Test
    fun testGetAllCustomers() =
        testApplication {
            customerDao.addNewCustomer(CreateCustomerDTO("Pipo Gorosito"))
            customerDao.addNewCustomer(CreateCustomerDTO("Coco Basile"))

            val response = client.get("/customer")
            assertEquals(HttpStatusCode.OK, response.status)
        }

    @Test
    fun testGetAllCustomersWhenNoneExists() =
        testApplication {
            val response = client.get("/customer")
            assertEquals(HttpStatusCode.OK, response.status)
        }
}
