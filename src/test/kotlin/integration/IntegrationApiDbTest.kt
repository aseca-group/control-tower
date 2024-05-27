package integration

import com.example.module
import com.example.modules.address.model.Address
import com.example.modules.address.model.Addresses
import com.example.modules.customer.model.Customers
import com.example.modules.order.model.Orders
import com.example.modules.order.model.OrdersProducts
import com.example.modules.product.model.Products
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import io.ktor.util.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Before
import org.junit.Test

class IntegrationApiDbTest {
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

    }
}