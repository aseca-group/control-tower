@file:Suppress("ktlint:standard:no-wildcard-imports")

package endpoint

import com.example.modules.address.model.Addresses
import com.example.modules.customer.model.Customers
import com.example.modules.inventory.dao.inventoryDao
import com.example.modules.inventory.model.CreateInventoryDTO
import com.example.modules.inventory.model.Inventories
import com.example.modules.inventory.model.MarkAsReservedDTO
import com.example.modules.order.model.Orders
import com.example.modules.order.model.OrdersProducts
import com.example.modules.product.dao.productDao
import com.example.modules.product.model.CreateProductDTO
import com.example.modules.product.model.Products
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class InventoryMarkAsReservedEndpointTest {
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

    private fun ApplicationTestBuilder.httpClient(): HttpClient {
        val client =
            createClient {
                install(ContentNegotiation) {
                    json()
                }
            }
        return client
    }

    @Test
    fun testMarkStockAsReservedWhenStockAvailable() =
        testApplication {
            val client = httpClient()
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            inventoryDao.addNewInventory(CreateInventoryDTO(1, 200))
            val num = 10
            val response =
                client.patch("/inventory/markAsReserved") {
                    contentType(ContentType.Application.Json)
                    setBody(MarkAsReservedDTO(1, num))
                }
            assertEquals("Updated reserved stock amount: $num", response.bodyAsText())
            assertEquals(HttpStatusCode.OK, response.status)
        }

    @Test
    fun testMarkStockAsReservedWhenNoStockAvailable() =
        testApplication {
            val client = httpClient()
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            inventoryDao.addNewInventory(CreateInventoryDTO(1, 0))
            val response =
                client.patch("/inventory/markAsReserved") {
                    contentType(ContentType.Application.Json)
                    setBody(MarkAsReservedDTO(1, 10))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun testMarkStockAsReservedWhenInventoryDoesNotExist() =
        testApplication {
            val client = httpClient()
            val response =
                client.patch("/inventory/markAsReserved") {
                    contentType(ContentType.Application.Json)
                    setBody(MarkAsReservedDTO(1, 10))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun testMarkStockAsReservedWhenMultipleInventoriesExists() =
        testApplication {
            val client = httpClient()
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays clasicas"))
            inventoryDao.addNewInventory(CreateInventoryDTO(1, 200))
            inventoryDao.addNewInventory(CreateInventoryDTO(2, 200))
            val num = 10
            val response =
                client.patch("/inventory/markAsReserved") {
                    contentType(ContentType.Application.Json)
                    setBody(MarkAsReservedDTO(2, num))
                }
            assertEquals("Updated reserved stock amount: $num", response.bodyAsText())
            assertEquals(HttpStatusCode.OK, response.status)
        }
}
