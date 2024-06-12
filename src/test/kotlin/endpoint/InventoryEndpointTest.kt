package endpoint

import com.example.modules.address.model.Addresses
import com.example.modules.customer.model.Customers
import com.example.modules.inventory.dao.inventoryDao
import com.example.modules.inventory.model.AddStockDTO
import com.example.modules.inventory.model.CreateInventoryDTO
import com.example.modules.inventory.model.Inventories
import com.example.modules.inventory.model.Inventory
import com.example.modules.inventory.model.MarkAsReservedDTO
import com.example.modules.inventory.model.MarkAsUnreservedDTO
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
    fun testPostInventoryWhenNoProduct() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }


        val postInventory = client.post("/inventory") {
            contentType(ContentType.Application.Json)
            setBody(CreateInventoryDTO(1, 200))
        }

        assertEquals(HttpStatusCode.BadRequest, postInventory.status)
        assertEquals("Error: Product does not exists, thus inventory wasn't created.", postInventory.bodyAsText())
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
    fun testGetAllInventories() = testApplication {
        productDao.addNewProduct(CreateProductDTO(11.0, "Papas Lays Messi"))
        productDao.addNewProduct(CreateProductDTO(12.0, "Pan"))
        productDao.addNewProduct(CreateProductDTO(13.0, "Oreos"))
        inventoryDao.addNewInventory(CreateInventoryDTO(1, 200))
        inventoryDao.addNewInventory(CreateInventoryDTO(2, 200))
        inventoryDao.addNewInventory(CreateInventoryDTO(3, 200))

        val response = client.get("/inventory/")
        val inventories = Json.decodeFromString<List<Inventory>>(response.bodyAsText())
        assertEquals(3, inventories.size)
        assertEquals(HttpStatusCode.OK, response.status)

    }

    @Test
    fun testAddStockWhenInventoryExists() = testApplication {
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
    fun testAddStockWhenInventoryDoesntExist() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))

        val response =  client.patch("/inventory/addStock"){
            contentType(ContentType.Application.Json)
            setBody(AddStockDTO(1, 200))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Error: Failed to add stock.", response.bodyAsText())
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

    @Test
    fun testMarkStockAsReservedWhenStockAvailable() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
        inventoryDao.addNewInventory(CreateInventoryDTO(1, 200))

        val num = 10;

        val response = client.patch("/inventory/markAsReserved"){
            contentType(ContentType.Application.Json)
            setBody(MarkAsReservedDTO(1, num))
        }

        assertEquals("Updated reserved stock amount: $num", response.bodyAsText())
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testMarkStockAsReservedWhenNoStockAvailable() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
        inventoryDao.addNewInventory(CreateInventoryDTO(1, 0))

        val response = client.patch("/inventory/markAsReserved"){
            contentType(ContentType.Application.Json)
            setBody(MarkAsReservedDTO(1, 10))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun testMarkStockAsUnreserved() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val resStock = 100
        val unRes = 10

        productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
        inventoryDao.addNewInventory(CreateInventoryDTO(1, 1000))
        inventoryDao.markAsReserved(MarkAsReservedDTO(1, resStock))



        val response = client.patch("/inventory/unreserve"){
            contentType(ContentType.Application.Json)
            setBody(MarkAsUnreservedDTO(1, unRes))
        }

        assertEquals("Updated reserved stock amount: " + (resStock - unRes), response.bodyAsText())
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testMarkStockAsUnreservedWhenNoStockReserved() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val resStock = 0
        val unRes = 10

        productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
        inventoryDao.addNewInventory(CreateInventoryDTO(1, 1000))
        inventoryDao.markAsReserved(MarkAsReservedDTO(1, resStock))



        val response = client.patch("/inventory/unreserve"){
            contentType(ContentType.Application.Json)
            setBody(MarkAsUnreservedDTO(1, unRes))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }








}