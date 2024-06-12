package endpoint

import com.example.modules.product.dao.productDao
import com.example.modules.product.model.CreateProductDTO
import com.example.modules.product.model.Product
import com.example.modules.address.model.Addresses
import com.example.modules.customer.dao.customerDao
import com.example.modules.customer.model.CreateCustomerDTO
import com.example.modules.customer.model.Customer
import com.example.modules.customer.model.Customers
import com.example.modules.inventory.model.Inventories
import com.example.modules.order.model.Orders
import com.example.modules.order.model.OrdersProducts
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
    fun testPostProduct() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val response = client.post("/product") {
            contentType(ContentType.Application.Json)
            setBody(CreateProductDTO(10.0, "Papas Lays Messi"))
        }
        val product = Json.decodeFromString<Product>(response.bodyAsText())
        TestCase.assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Papas Lays Messi", product.name)
        assertEquals(10.0, product.price)
    }

    @Test
    fun testGetProduct() = testApplication {
        productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))

        val response = client.get("/product/1")
        TestCase.assertEquals(HttpStatusCode.OK, response.status)
        val product = Json.decodeFromString<Product>(response.bodyAsText())
        TestCase.assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Papas Lays Messi", product.name)
        assertEquals(10.0, product.price)
    }


    @Test
    fun testDeleteProduct() = testApplication {
        productDao.addNewProduct(CreateProductDTO(10.0, "Papas Lays Messi"))
        val response = client.delete("/product/1")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Product 1 deleted", response.bodyAsText())
    }
}