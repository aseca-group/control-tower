@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.db

import com.example.modules.address.model.Addresses
import com.example.modules.customer.model.Customers
import com.example.modules.inventory.model.Inventories
import com.example.modules.order.model.Orders
import com.example.modules.order.model.OrdersProducts
import com.example.modules.product.model.Products
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.*
import org.jetbrains.exposed.sql.transactions.experimental.*

object DatabaseSingleton {
    fun init() {
        val driverClassName = "org.h2.Driver"
        val jdbcURL = if (isTestEnvironment()) {
            "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;" // Base de datos en memoria para pruebas
        } else {
            "jdbc:h2:file:./build/db"
        }
        val database = Database.connect(jdbcURL, driverClassName)
        transaction(database) {
            if (isTestEnvironment()) {
                SchemaUtils.drop(Addresses, Customers, Orders, Products, OrdersProducts, Inventories)
            }
            SchemaUtils.create(Addresses, Customers, Orders, Products, OrdersProducts, Inventories)
        }
    }

    private fun isTestEnvironment() = System.getProperty("env") == "test"

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
