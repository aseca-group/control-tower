package endpoint

import com.example.modules.address.dao.addressDao
import com.example.modules.address.model.Address
import com.example.modules.address.model.Addresses
import com.example.modules.address.model.CreateAddressDTO
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

class AddressEndpointTest {

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
    fun testPostAddress() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val response = client.post("/address") {
            contentType(ContentType.Application.Json)
            setBody(CreateAddressDTO("Buenos Aires", "Av. del Libertador", 233))
        }
        val address = Json.decodeFromString<Address>(response.bodyAsText())
        TestCase.assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Buenos Aires", address.city)
        assertEquals("Av. del Libertador", address.road)
        assertEquals(233, address.number)
    }



    @Test
    fun testGetAddress() = testApplication {
        addressDao.addNewAddress(CreateAddressDTO("Buenos Aires", "Av. del Libertador", 233))

        val response = client.get("/address/1")
        TestCase.assertEquals(HttpStatusCode.OK, response.status)
        val address = Json.decodeFromString<Address>(response.bodyAsText())
        assertEquals("Buenos Aires", address.city)
        assertEquals("Av. del Libertador", address.road)
        assertEquals(233, address.number)
    }

    @Test
    fun testGetAddressDoesNotExist() = testApplication {
        val response = client.get("/address/1")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Address not found", response.bodyAsText())
    }


    @Test
    fun testGetAddressInvalid() = testApplication {
        val response = client.get("/address/invalid")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Invalid ID", response.bodyAsText())
    }

    @Test
    fun testDeleteAddress() = testApplication {
        addressDao.addNewAddress(CreateAddressDTO("Buenos Aires", "Av. del Libertador", 233))

        val response = client.delete("/address/1")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Address 1 deleted", response.bodyAsText())
    }

    @Test
    fun testDeleteNonExistingAddress() =
        testApplication {
            val response = client.delete("/address/1")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Address not found", response.bodyAsText())
        }

    @Test
    fun testDeleteInvalidIDAddress() =
        testApplication {
            val response = client.delete("/address/invalid")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Invalid ID", response.bodyAsText())
        }

    @Test
    fun testGetAllAddress() =
        testApplication {
            addressDao.addNewAddress(CreateAddressDTO("Buenos Aires", "Av. del Libertador", 233))

            val response = client.get("/address")
            TestCase.assertEquals(HttpStatusCode.OK, response.status)
            val addresses = Json.decodeFromString<List<Address>>(response.bodyAsText())
            val address = addresses[0]
            assertEquals("Buenos Aires", address.city)
            assertEquals("Av. del Libertador", address.road)
            assertEquals(233, address.number)
        }

    @Test
    fun testGetAllAddressEmpty() =
        testApplication {
            val response = client.get("/address")
            val addresses = Json.decodeFromString<List<Address>>(response.bodyAsText())
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(0, addresses.size)
        }

    @Test
    fun testGetAllAddresses() =
        testApplication {
            addressDao.addNewAddress(CreateAddressDTO("Buenos Aires", "Av. del Libertador", 233))
            addressDao.addNewAddress(CreateAddressDTO("Buenos Aires", "Av. del Libertador", 233))

            val response = client.get("/address")
            val addresses = Json.decodeFromString<List<Address>>(response.bodyAsText())
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(2, addresses.size)
            assertEquals("Buenos Aires", addresses[0].city)
            assertEquals("Av. del Libertador", addresses[0].road)
            assertEquals(233, addresses[0].number)
            assertEquals("Buenos Aires", addresses[1].city)
            assertEquals("Av. del Libertador", addresses[1].road)
            assertEquals(233, addresses[1].number)
        }
}