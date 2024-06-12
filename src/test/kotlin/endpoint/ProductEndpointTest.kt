@file:Suppress("ktlint:standard:no-wildcard-imports")

package endpoint

import com.example.modules.address.model.Addresses
import com.example.modules.customer.model.Customers
import com.example.modules.inventory.model.Inventories
import com.example.modules.order.model.Orders
import com.example.modules.order.model.OrdersProducts
import com.example.modules.product.dao.productDao
import com.example.modules.product.model.CreateProductDTO
import com.example.modules.product.model.Product
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

class ProductEndpointTest {
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
    fun testPostProduct() =
        testApplication {
            val client =
                createClient {
                    install(ContentNegotiation) {
                        json()
                    }
                }
            val response =
                client.post("/product") {
                    contentType(ContentType.Application.Json)
                    setBody(CreateProductDTO(10.0, "Papas Lays Messi"))
                }
            val product = Json.decodeFromString<Product>(response.bodyAsText())
            TestCase.assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Papas Lays Messi", product.name)
            assertEquals(10.0, product.price)
        }

    @Test
    fun testGetProduct() =
        testApplication {
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))

            val response = client.get("/product/1")
            TestCase.assertEquals(HttpStatusCode.OK, response.status)
            val product = Json.decodeFromString<Product>(response.bodyAsText())
            TestCase.assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Papas Lays Messi", product.name)
            assertEquals(10.0, product.price)
        }

    @Test
    fun testGetNonExistingProduct() =
        testApplication {
            val response = client.get("/product/1")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Product not found", response.bodyAsText())
        }

    @Test
    fun testGetInvalidIDProduct() =
        testApplication {
            val response = client.get("/product/invalid")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Invalid ID", response.bodyAsText())
        }

    @Test
    fun testDeleteProduct() =
        testApplication {
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            val response = client.delete("/product/1")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Product 1 deleted", response.bodyAsText())
        }

    @Test
    fun testDeleteNonExistingProduct() =
        testApplication {
            val response = client.delete("/product/1")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Product not found", response.bodyAsText())
        }

    @Test
    fun testDeleteInvalidIDProduct() =
        testApplication {
            val response = client.delete("/product/invalid")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Invalid ID", response.bodyAsText())
        }

    @Test
    fun testGetAllProduct() =
        testApplication {
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))

            val response = client.get("/product")
            val products = Json.decodeFromString<List<Product>>(response.bodyAsText())
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(1, products.size)
            assertEquals("Papas Lays Messi", products[0].name)
            assertEquals(10.0, products[0].price)
        }

    @Test
    fun testGetAllProductEmpty() =
        testApplication {
            val response = client.get("/product")
            val products = Json.decodeFromString<List<Product>>(response.bodyAsText())
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(0, products.size)
        }

    @Test
    fun testGetAllProducts() =
        testApplication {
            productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
            productDao.addNewProduct(CreateProductDTO(20.0, "Papas Lays clasicas"))

            val response = client.get("/product")
            val products = Json.decodeFromString<List<Product>>(response.bodyAsText())
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(2, products.size)
            assertEquals("Papas Lays Messi", products[0].name)
            assertEquals(10.0, products[0].price)
            assertEquals("Papas Lays clasicas", products[1].name)
            assertEquals(20.0, products[1].price)
        }
}
