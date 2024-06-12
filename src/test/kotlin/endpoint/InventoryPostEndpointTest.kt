@file:Suppress("ktlint:standard:no-wildcard-imports")

package endpoint

import com.example.modules.address.model.Addresses
import com.example.modules.customer.model.Customers
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
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Before
import kotlin.test.*
import kotlin.test.assertEquals

class InventoryPostEndpointTest {
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
    fun testPostInventory() =
        testApplication {
            val client = httpClient()
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            var response = ""
            val postInventory =
                client.post("/inventory") {
                    contentType(ContentType.Application.Json)
                    setBody(CreateInventoryDTO(1, 200))
                }.apply {
                    assertEquals(HttpStatusCode.Found, status)
                    assertEquals("/inventory/1", headers[HttpHeaders.Location])
                    response = client.get(headers[HttpHeaders.Location].toString()).bodyAsText()
                }
            val inventory = Json.decodeFromString<Inventory>(response)
            assertEquals(1, inventory.productId)
            assertEquals(200, inventory.stock)
            assertEquals(0, inventory.reservedStock)
        }

    @Test
    fun testPostInventoryWhenNoProduct() =
        testApplication {
            val client = httpClient()
            val postInventory =
                client.post("/inventory") {
                    contentType(ContentType.Application.Json)
                    setBody(CreateInventoryDTO(1, 200))
                }
            assertEquals(HttpStatusCode.BadRequest, postInventory.status)
            assertEquals("Error: Product does not exists, thus inventory wasn't created.", postInventory.bodyAsText())
        }

    @Test
    fun testPostNegativeInventory() =
        testApplication {
            val client = httpClient()
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            var response = ""
            val postInventory =
                client.post("/inventory") {
                    contentType(ContentType.Application.Json)
                    setBody(CreateInventoryDTO(1, -200))
                }.apply {
                    assertEquals(HttpStatusCode.Found, status)
                    assertEquals("/inventory/1", headers[HttpHeaders.Location])
                    response = client.get(headers[HttpHeaders.Location].toString()).bodyAsText()
                }
            val inventory = Json.decodeFromString<Inventory>(response)
            assertEquals(1, inventory.productId)
            assertEquals(0, inventory.stock)
            assertEquals(0, inventory.reservedStock)
        }

    @Test
    fun testPostInventoryWithZeroStock() =
        testApplication {
            val client = httpClient()
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            var response = ""
            val postInventory =
                client.post("/inventory") {
                    contentType(ContentType.Application.Json)
                    setBody(CreateInventoryDTO(1, 0))
                }.apply {
                    assertEquals(HttpStatusCode.Found, status)
                    assertEquals("/inventory/1", headers[HttpHeaders.Location])
                    response = client.get(headers[HttpHeaders.Location].toString()).bodyAsText()
                }
            val inventory = Json.decodeFromString<Inventory>(response)
            assertEquals(1, inventory.productId)
            assertEquals(0, inventory.stock)
            assertEquals(0, inventory.reservedStock)
        }
}
