package persistence

import com.example.modules.address.model.Addresses
import com.example.modules.inventory.model.Inventories
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class PersistenceInsertionTests {
    @Before
    fun setup() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.create(Addresses)
            SchemaUtils.create(Inventories)
        }
    }

    @After
    fun teardown() {
        transaction {
            SchemaUtils.drop(Addresses)
            SchemaUtils.drop(Inventories)
        }
    }

    @Test
    fun testAddressInsertion() {
        transaction {
            val addressId = Addresses.insert {
                it[city] = "Buenos Aires"
                it[road] = "Estanislao Diaz"
                it[number] = 201
            } get Addresses.id

            assertNotNull(addressId)

            val insertedAddress = Addresses.select { Addresses.id eq addressId }.singleOrNull()
            assertNotNull(insertedAddress)
            assertEquals("Buenos Aires", insertedAddress?.get(Addresses.city))
        }
    }

    @Test
    fun testInventoryInsertion() {
        transaction {
            val inventoryId = Inventories.insert {
                it[productId] = 3
                it[stock] = 4
                it[reservedStock] = 0
            } get Inventories.productId

            assertNotNull(inventoryId)

            val insertedInventory = Inventories.select {Inventories.productId eq inventoryId }.singleOrNull()
            assertNotNull(insertedInventory)
            assertEquals(4, insertedInventory?.get(Inventories.stock))
        }
    }
}
