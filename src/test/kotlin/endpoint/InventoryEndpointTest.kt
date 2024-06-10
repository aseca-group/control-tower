package endpoint

import com.example.modules.address.dao.addressDao
import com.example.modules.address.model.Address
import com.example.modules.address.model.Addresses
import com.example.modules.address.model.CreateAddressDTO
import com.example.modules.customer.model.Customer
import com.example.modules.customer.model.Customers
import com.example.modules.inventory.dao.inventoryDao
import com.example.modules.inventory.model.AddStockDTO
import com.example.modules.inventory.model.CreateInventoryDTO
import com.example.modules.inventory.model.Inventories
import com.example.modules.inventory.model.Inventory
import com.example.modules.order.model.Orders
import com.example.modules.order.model.OrdersProducts
import com.example.modules.product.dao.productDao
import com.example.modules.product.model.CreateProductDTO
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

class InventoryEndpointTest {

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
    fun testPostInventory() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))

        var response = ""
        val postInventory = client.post("/inventory") {
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
    fun testPostNegativeInventory() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))

        var response = ""
        val postInventory = client.post("/inventory") {
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
    fun testGetInventory() = testApplication {

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
    fun testAddStock() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
        inventoryDao.addNewInventory(CreateInventoryDTO(1, 200))

        val response =  client.patch("/inventory/addStock"){
            contentType(ContentType.Application.Json)
            setBody(AddStockDTO(1, 200))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Updated stock amount: 400", response.bodyAsText())
    }

    @Test
    fun testAddNegativeStock() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
        inventoryDao.addNewInventory(CreateInventoryDTO(1, 200))

        val response =  client.patch("/inventory/addStock"){
            contentType(ContentType.Application.Json)
            setBody(AddStockDTO(1, -200))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Error: Stock to add must be a positive number.", response.bodyAsText())
    }








}