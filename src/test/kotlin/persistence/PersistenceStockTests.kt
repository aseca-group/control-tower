package persistence

import com.example.modules.inventory.model.Inventories
import com.example.modules.product.model.Products
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class PersistenceStockTests {
    @Before
    fun setup() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.create(Inventories)
            SchemaUtils.create(Products)
        }

        transaction {
            Inventories.insert {
                it[productId] = 3
                it[stock] = 4
                it[reservedStock] = 0
            }
            Products.insert {
                it[price] = 33.2
                it[name] = "banana"
            }
        }
    }

    @After
    fun teardown() {
        transaction {
            SchemaUtils.drop(Inventories)
            SchemaUtils.drop(Products)
        }
    }

    @Test
    fun testReserveTwoBananas() {
        transaction {
            Inventories.update({ Inventories.productId eq 3 }) {
                it[reservedStock] = 2
            }

            val insertedInventory = Inventories.select {Inventories.productId eq 3}.singleOrNull()
            assertNotNull(insertedInventory)
            assertEquals(2, insertedInventory?.get(Inventories.reservedStock))
        }
    }

    @Test
    fun testRemoveReservedBananas() {
        transaction {
            Inventories.update({ Inventories.productId eq 3 }) {
                it[reservedStock] = 2
            }

            val inventory = Inventories.select {Inventories.productId eq 3}.singleOrNull()
            assertEquals(2, inventory?.get(Inventories.reservedStock))


            Inventories.update({ Inventories.productId eq 3 }) {
                it[reservedStock] = 4
            }

            val unreservedInventoried = Inventories.select {Inventories.productId eq 3 }.singleOrNull()
            assertEquals(4, unreservedInventoried?.get(Inventories.reservedStock))
        }
    }
}