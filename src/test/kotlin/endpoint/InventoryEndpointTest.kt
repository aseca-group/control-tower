@file:Suppress("ktlint:standard:no-wildcard-imports")

package endpoint

import com.example.modules.address.dao.addressDao
import com.example.modules.address.model.Addresses
import com.example.modules.address.model.CreateAddressDTO
import com.example.modules.customer.dao.customerDao
import com.example.modules.customer.model.CreateCustomerDTO
import com.example.modules.customer.model.Customers
import com.example.modules.inventory.dao.inventoryDao
import com.example.modules.inventory.model.AddStockDTO
import com.example.modules.inventory.model.CreateInventoryDTO
import com.example.modules.inventory.model.Inventories
import com.example.modules.inventory.model.Inventory
import com.example.modules.inventory.model.MarkAsReservedDTO
import com.example.modules.inventory.model.MarkAsUnreservedDTO
import com.example.modules.inventory.model.RemoveReservedStockDTO
import com.example.modules.inventory.model.RemoveStockDTO
import com.example.modules.order.dao.orderDao
import com.example.modules.order.model.CreateOrderDTO
import com.example.modules.order.model.Orders
import com.example.modules.order.model.OrdersProducts
import com.example.modules.order.model.ProductQty
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
    fun testGetAllInventories() =
        testApplication {
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
    fun testAddStockWhenInventoryExists() =
        testApplication {
            val client = httpClient()
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            inventoryDao.addNewInventory(CreateInventoryDTO(1, 200))
            val response =
                client.patch("/inventory/addStock") {
                    contentType(ContentType.Application.Json)
                    setBody(AddStockDTO(1, 200))
                }
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Updated stock amount: 400", response.bodyAsText())
        }

    @Test
    fun testAddStockWhenInventoryDoesntExist() =
        testApplication {
            val client = httpClient()
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            val response =
                client.patch("/inventory/addStock") {
                    contentType(ContentType.Application.Json)
                    setBody(AddStockDTO(1, 200))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Error: Failed to add stock.", response.bodyAsText())
        }

    @Test
    fun testAddNegativeStock() =
        testApplication {
            val client = httpClient()
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            inventoryDao.addNewInventory(CreateInventoryDTO(1, 200))
            val response =
                client.patch("/inventory/addStock") {
                    contentType(ContentType.Application.Json)
                    setBody(AddStockDTO(1, -200))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Error: Stock to add must be a positive number.", response.bodyAsText())
        }

    @Test
    fun testAddZeroStock() =
        testApplication {
            val client = httpClient()
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            inventoryDao.addNewInventory(CreateInventoryDTO(1, 200))
            val response =
                client.patch("/inventory/addStock") {
                    contentType(ContentType.Application.Json)
                    setBody(AddStockDTO(1, -200))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Error: Stock to add must be a positive number.", response.bodyAsText())
        }

    @Test
    fun testDeleteInventory() {
        testApplication {
            val client = httpClient()
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            inventoryDao.addNewInventory(CreateInventoryDTO(1, 200))
            val response =
                client.delete("/inventory/delete/1")
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun testDeleteNonExistentInventory() {
        testApplication {
            val client = httpClient()
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            val response =
                client.delete("/inventory/delete/1")
            assertEquals(HttpStatusCode.NotFound, response.status)
            assertEquals("Error: inventory not found.", response.bodyAsText())
        }
    }

    @Test
    fun testDeleteWhenHavingMultipleInventories() {
        testApplication {
            val client = httpClient()
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays normales"))
            inventoryDao.addNewInventory(CreateInventoryDTO(1, 200))
            inventoryDao.addNewInventory(CreateInventoryDTO(2, 200))
            val response =
                client.delete("/inventory/delete/2")
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun testDeleteWithoutID() {
        testApplication {
            val client = httpClient()
            val response = client.delete("/inventory/delete/abc")
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Error: invalid id.", response.bodyAsText())
        }
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
    fun testMarkStockAsUnreserved() =
        testApplication {
            val client = httpClient()
            val resStock = 100
            val unRes = 10
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            inventoryDao.addNewInventory(CreateInventoryDTO(1, 1000))
            inventoryDao.markAsReserved(MarkAsReservedDTO(1, resStock))
            val response =
                client.patch("/inventory/unreserve") {
                    contentType(ContentType.Application.Json)
                    setBody(MarkAsUnreservedDTO(1, unRes))
                }
            assertEquals("Updated reserved stock amount: " + (resStock - unRes), response.bodyAsText())
            assertEquals(HttpStatusCode.OK, response.status)
        }

    @Test
    fun testMarkStockAsUnreservedWhenNoStockReserved() =
        testApplication {
            val client = httpClient()
            val resStock = 0
            val unRes = 10
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            inventoryDao.addNewInventory(CreateInventoryDTO(1, 1000))
            inventoryDao.markAsReserved(MarkAsReservedDTO(1, resStock))
            val response =
                client.patch("/inventory/unreserve") {
                    contentType(ContentType.Application.Json)
                    setBody(MarkAsUnreservedDTO(1, unRes))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
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

    @Test
    fun removeReservedStock() {
        testApplication {
            val client = httpClient()
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            inventoryDao.addNewInventory(CreateInventoryDTO(1, 1000))
            val productList = listOf(ProductQty(1, 10))
            addressDao.addNewAddress(CreateAddressDTO("Miami", "Messi avenue", 12345))
            customerDao.addNewCustomer(CreateCustomerDTO("Leonel Messi"))
            orderDao.addNewOrder(CreateOrderDTO(productList, 1, 1), 1, 100.0)
            val response =
                client.patch("/inventory/removeReservedStock") {
                    contentType(ContentType.Application.Json)
                    setBody(RemoveReservedStockDTO(1))
                }
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("[\"Product ID: 1, Updated reserved stock: 0\"]", response.bodyAsText())
        }
    }

    @Test
    fun removeReservedStockWhenNoOrder() {
        testApplication {
            val client = httpClient()
            val response =
                client.patch("/inventory/removeReservedStock") {
                    contentType(ContentType.Application.Json)
                    setBody(RemoveReservedStockDTO(1))
                }
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Error: Reserved stock could not be removed.", response.bodyAsText())
        }
    }

    @Test
    fun removeReservedStockFromMultipleInventories() {
        testApplication {
            val client = httpClient()
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays normales"))
            inventoryDao.addNewInventory(CreateInventoryDTO(1, 1000))
            inventoryDao.addNewInventory(CreateInventoryDTO(2, 1000))
            val productList = listOf(ProductQty(1, 10), ProductQty(2, 10))
            addressDao.addNewAddress(CreateAddressDTO("Miami", "Messi avenue", 12345))
            customerDao.addNewCustomer(CreateCustomerDTO("Leonel Messi"))
            orderDao.addNewOrder(CreateOrderDTO(productList, 1, 1), 1, 200.0)
            val response =
                client.patch("/inventory/removeReservedStock") {
                    contentType(ContentType.Application.Json)
                    setBody(RemoveReservedStockDTO(1))
                }
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(
                "[\"Product ID: 1, Updated reserved stock: 0\",\"Product ID: 2, Updated reserved stock: 0\"]",
                response.bodyAsText(),
            )
        }
    }

    @Test
    fun removeReservedStockWhenThereIsMultipleOrders() {
        testApplication {
            val client = httpClient()
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays normales"))
            inventoryDao.addNewInventory(CreateInventoryDTO(1, 1000))
            inventoryDao.addNewInventory(CreateInventoryDTO(2, 1000))
            val productList = listOf(ProductQty(1, 10), ProductQty(2, 10))
            addressDao.addNewAddress(CreateAddressDTO("Miami", "Messi avenue", 12345))
            customerDao.addNewCustomer(CreateCustomerDTO("Leonel Messi"))
            orderDao.addNewOrder(CreateOrderDTO(productList, 1, 1), 1, 200.0)
            orderDao.addNewOrder(CreateOrderDTO(productList, 1, 1), 2, 200.0)
            val response =
                client.patch("/inventory/removeReservedStock") {
                    contentType(ContentType.Application.Json)
                    setBody(RemoveReservedStockDTO(2))
                }
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(
                "[\"Product ID: 1, Updated reserved stock: 10\",\"Product ID: 2, Updated reserved stock: 10\"]",
                response.bodyAsText(),
            )
        }
    }
}
