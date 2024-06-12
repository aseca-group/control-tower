@file:Suppress("ktlint:standard:no-wildcard-imports")

package endpoint

import com.example.modules.address.model.Addresses
import com.example.modules.customer.model.Customers
import com.example.modules.inventory.dao.inventoryDao
import com.example.modules.inventory.model.CreateInventoryDTO
import com.example.modules.inventory.model.Inventories
import com.example.modules.inventory.model.Inventory
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
import junit.framework.TestCase
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class InventoryGetInventoryTest {
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
    fun testGetInventory() =
        testApplication {
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            inventoryDao.addNewInventory(CreateInventoryDTO(1, 200))
            val response = client.get("/inventory/1")
            TestCase.assertEquals(HttpStatusCode.OK, response.status)
            val inventory = Json.decodeFromString<Inventory>(response.bodyAsText())
            assertEquals(1, inventory.productId)
            assertEquals(200, inventory.stock)
            assertEquals(0, inventory.reservedStock)
        }

    @Test
    fun testGetNonExistingInventory() =
        testApplication {
            val response = client.get("/inventory/1")
            TestCase.assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun testGetInventoryWhenMultipleExists() {
        testApplication {
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays clasicas"))
            inventoryDao.addNewInventory(CreateInventoryDTO(1, 200))
            inventoryDao.addNewInventory(CreateInventoryDTO(2, 200))
            val response = client.get("/inventory/2")
            TestCase.assertEquals(HttpStatusCode.OK, response.status)
            val inventory = Json.decodeFromString<Inventory>(response.bodyAsText())
            assertEquals(2, inventory.productId)
            assertEquals(200, inventory.stock)
            assertEquals(0, inventory.reservedStock)
        }
    }
}
