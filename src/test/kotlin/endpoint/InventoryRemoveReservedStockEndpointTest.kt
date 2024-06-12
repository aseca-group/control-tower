@file:Suppress("ktlint:standard:no-wildcard-imports")

package endpoint

import com.example.modules.address.dao.addressDao
import com.example.modules.address.model.Addresses
import com.example.modules.address.model.CreateAddressDTO
import com.example.modules.customer.dao.customerDao
import com.example.modules.customer.model.CreateCustomerDTO
import com.example.modules.customer.model.Customers
import com.example.modules.inventory.dao.inventoryDao
import com.example.modules.inventory.model.CreateInventoryDTO
import com.example.modules.inventory.model.Inventories
import com.example.modules.inventory.model.RemoveReservedStockDTO
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
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class InventoryRemoveReservedStockEndpointTest {
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
