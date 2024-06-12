@file:Suppress("ktlint:standard:no-wildcard-imports")

package endpoint

import com.example.modules.address.model.Addresses
import com.example.modules.customer.model.Customers
import com.example.modules.inventory.dao.inventoryDao
import com.example.modules.inventory.model.CreateInventoryDTO
import com.example.modules.inventory.model.Inventories
import com.example.modules.inventory.model.MarkAsReservedDTO
import com.example.modules.inventory.model.RemoveStockDTO
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

class InventoryRemoveStockEndpointTest {
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
    fun testRemoveAllStock() {
        testApplication {
            val client = httpClient()
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            inventoryDao.addNewInventory(CreateInventoryDTO(1, 200))
            val response =
                client.patch("/inventory/removeStock") {
                    contentType(ContentType.Application.Json)
                    setBody(RemoveStockDTO(1, 200))
                }
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Updated stock amount: 0", response.bodyAsText())
        }
    }

    @Test
    fun removeHalfTheStock() {
        testApplication {
            val client = httpClient()
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            inventoryDao.addNewInventory(CreateInventoryDTO(1, 200))
            val response =
                client.patch("/inventory/removeStock") {
                    contentType(ContentType.Application.Json)
                    setBody(RemoveStockDTO(1, 100))
                }
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Updated stock amount: 100", response.bodyAsText())
        }
    }

    @Test
    fun removeNegativeAmountOfStock() {
        testApplication {
            val client = httpClient()
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            inventoryDao.addNewInventory(CreateInventoryDTO(1, 200))
            val response =
                client.patch("/inventory/removeStock") {
                    contentType(ContentType.Application.Json)
                    setBody(RemoveStockDTO(1, -100))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Error: Stock to remove must be a positive number.", response.bodyAsText())
        }
    }

    @Test
    fun removeZeroStock() {
        testApplication {
            val client = httpClient()
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            inventoryDao.addNewInventory(CreateInventoryDTO(1, 200))
            val response =
                client.patch("/inventory/removeStock") {
                    contentType(ContentType.Application.Json)
                    setBody(RemoveStockDTO(1, 0))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Error: Stock to remove must be a positive number.", response.bodyAsText())
        }
    }

    @Test
    fun removeStockFromInvalidID() {
        testApplication {
            val client = httpClient()
            val response =
                client.patch("/inventory/removeStock") {
                    contentType(ContentType.Application.Json)
                    setBody(RemoveStockDTO(1, 200))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Error: Stock could not be removed.", response.bodyAsText())
        }
    }

    @Test
    fun removeMoreStockThanExisting() {
        testApplication {
            val client = httpClient()
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            inventoryDao.addNewInventory(CreateInventoryDTO(1, 200))
            val response =
                client.patch("/inventory/removeStock") {
                    contentType(ContentType.Application.Json)
                    setBody(RemoveStockDTO(1, 300))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Error: Stock could not be removed.", response.bodyAsText())
        }
    }

    @Test
    fun removeStockWhenReservedSuccess() {
        testApplication {
            val client = httpClient()
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            inventoryDao.addNewInventory(CreateInventoryDTO(1, 200))
            inventoryDao.markAsReserved(MarkAsReservedDTO(1, 100))
            val response =
                client.patch("/inventory/removeStock") {
                    contentType(ContentType.Application.Json)
                    setBody(RemoveStockDTO(1, 100))
                }
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Updated stock amount: 100", response.bodyAsText())
        }
    }

    @Test
    fun removeStockWhenReservedFail() {
        testApplication {
            val client = httpClient()
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            inventoryDao.addNewInventory(CreateInventoryDTO(1, 200))
            inventoryDao.markAsReserved(MarkAsReservedDTO(1, 100))
            val response =
                client.patch("/inventory/removeStock") {
                    contentType(ContentType.Application.Json)
                    setBody(RemoveStockDTO(1, 200))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Error: Stock could not be removed.", response.bodyAsText())
        }
    }
}
