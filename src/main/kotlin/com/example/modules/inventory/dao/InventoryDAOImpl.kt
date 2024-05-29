@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.modules.inventory.dao

import com.example.db.DatabaseSingleton.dbQuery
import com.example.modules.inventory.model.AddStockDTO
import com.example.modules.inventory.model.CreateInventoryDTO
import com.example.modules.inventory.model.Inventories
import com.example.modules.inventory.model.Inventory
import com.example.modules.inventory.model.MarkAsReservedDTO
import com.example.modules.inventory.model.MarkAsUnreservedDTO
import com.example.modules.inventory.model.RemoveReservedStockDTO
import com.example.modules.inventory.model.RemoveStockDTO
import com.example.modules.order.model.Orders
import com.example.modules.order.model.OrdersProducts
import com.example.modules.order.model.ProductQty
import com.example.modules.product.model.Products
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class InventoryDAOImpl : InventoryDAOFacade {
    override suspend fun allInventories(): List<Inventory> =
        dbQuery {
            Inventories.selectAll().map(::resultRowToInventory)
        }

    override suspend fun inventory(productId: Int): Inventory? =
        dbQuery {
            val condition = (Inventories.productId eq productId)
            Inventories
                .select(condition)
                .map(::resultRowToInventory)
                .singleOrNull()
        }

    override suspend fun addNewInventory(inventory: CreateInventoryDTO): Inventory? {
        if (!existProduct(inventory.productId)) {
            return null
        }
        return dbQuery {
            val stockValue = maxOf(0, inventory.stock)
            val insertStatement =
                Inventories.insert {
                    it[productId] = inventory.productId
                    it[stock] = stockValue
                    it[reservedStock] = 0
                }
            insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToInventory)
        }
    }

    override suspend fun addStock(inventory: AddStockDTO): Inventory? {
        var updatedInventory: Inventory? = null
        dbQuery {
            val originalInventory =
                Inventories
                    .select { Inventories.productId eq inventory.productId }
                    .singleOrNull()
            if (originalInventory != null) {
                Inventories.update({ Inventories.productId eq inventory.productId }) {
                    it[stock] = originalInventory[stock] + inventory.stockToAdd
                }
                val updatedStocks = Inventories.select { Inventories.productId eq inventory.productId }.single()
                updatedInventory =
                    Inventory(
                        productId = updatedStocks[Inventories.productId],
                        stock = updatedStocks[Inventories.stock],
                        reservedStock = updatedStocks[Inventories.reservedStock],
                    )
            }
        }
        return updatedInventory
    }

    override suspend fun markAsReserved(inventory: MarkAsReservedDTO): Inventory? {
        var updatedInventory: Inventory? = null
        dbQuery {
            val originalInventory =
                Inventories
                    .select(Inventories.productId eq inventory.productId)
                    .singleOrNull()
            if (originalInventory != null) {
                val newReservedStock = originalInventory[Inventories.reservedStock] + maxOf(0, inventory.stockToReserve)
                if (newReservedStock <= originalInventory[Inventories.stock]) {
                    Inventories.update({ Inventories.productId eq inventory.productId }) {
                        it[reservedStock] = newReservedStock
                    }
                    val updatedStocks = Inventories.select { Inventories.productId eq inventory.productId }.single()
                    updatedInventory =
                        Inventory(
                            productId = updatedStocks[Inventories.productId],
                            stock = updatedStocks[Inventories.stock],
                            reservedStock = updatedStocks[Inventories.reservedStock],
                        )
                }
            }
        }
        return updatedInventory
    }

    override suspend fun markAsUnreserved(inventory: MarkAsUnreservedDTO): Inventory? {
        var updatedInventory: Inventory? = null
        dbQuery {
            val originalInventory =
                Inventories
                    .select(Inventories.productId eq inventory.productId)
                    .singleOrNull()
            if (originalInventory != null) {
                val newReservedStock = originalInventory[Inventories.reservedStock] - inventory.stockToUnreserve
                if (newReservedStock >= 0) {
                    Inventories.update({ Inventories.productId eq inventory.productId }) {
                        it[reservedStock] = newReservedStock
                    }
                    val updatedStocks = Inventories.select { Inventories.productId eq inventory.productId }.single()
                    updatedInventory =
                        Inventory(
                            productId = updatedStocks[Inventories.productId],
                            stock = updatedStocks[Inventories.stock],
                            reservedStock = updatedStocks[Inventories.reservedStock],
                        )
                }
            }
        }
        return updatedInventory
    }

    override suspend fun removeStock(inventory: RemoveStockDTO): Inventory? {
        var newInventoryStock: Inventory? = null
        dbQuery {
            val originalInventory =
                Inventories
                    .select(Inventories.productId eq inventory.productId)
                    .singleOrNull()
            if (originalInventory != null) {
                val newStock = originalInventory[Inventories.stock] - inventory.stockToRemove
                if (newStock >= originalInventory[Inventories.reservedStock]) {
                    Inventories.update({ Inventories.productId eq inventory.productId }) {
                        it[stock] = newStock
                    }
                    val updatedStocks = Inventories.select { Inventories.productId eq inventory.productId }.single()
                    newInventoryStock =
                        Inventory(
                            productId = updatedStocks[Inventories.productId],
                            stock = updatedStocks[Inventories.stock],
                            reservedStock = updatedStocks[Inventories.reservedStock],
                        )
                }
            }
        }
        return newInventoryStock
    }

    override suspend fun removeReservedStock(inventory: RemoveReservedStockDTO): List<Inventory> {
        val deliveryId = inventory.deliveryId
        val updatedInventories = mutableListOf<Inventory>()

        dbQuery {
            // Retrieve the order associated with the delivery ID
            val order = Orders.select { Orders.deliveryId eq deliveryId }.singleOrNull()
            if (order != null) {
                val orderId = order[Orders.id]
                // Retrieve products associated with the order
                val products = OrdersProducts.select { OrdersProducts.orderId eq orderId }.map {
                    ProductQty(it[OrdersProducts.productId].value, it[OrdersProducts.qty])
                }

                // For each product, update the inventory by subtracting the reserved stock
                products.forEach { productQty ->
                    val productId = productQty.productId
                    val reservedStock = productQty.qty
                    // Retrieve current inventory
                    val currentInventory = Inventories.select { Inventories.productId eq productId }.singleOrNull()
                    if (currentInventory != null) {
                        val currentStock = currentInventory[Inventories.stock]
                        val currentReservedStock = currentInventory[Inventories.reservedStock]
                        val updatedStock = currentStock - reservedStock
                        val updatedReservedStock = currentReservedStock - reservedStock
                        // Update the inventory in the database
                        Inventories.update({ Inventories.productId eq productId }) {
                            it[Inventories.stock] = updatedStock
                            it[Inventories.reservedStock] = updatedReservedStock
                        }
                        // Add the updated inventory to the list
                        updatedInventories.add(Inventory(productId, updatedStock, updatedReservedStock))
                    }
                }
            }
        }
        return updatedInventories
    }





    override suspend fun deleteInventory(productId: Int): Boolean =
        dbQuery {
            Inventories.deleteWhere { Inventories.productId eq productId } > 0
        }

    private fun resultRowToInventory(resultRow: ResultRow) =
        Inventory(
            productId = resultRow[Inventories.productId],
            stock = resultRow[Inventories.stock],
            reservedStock = resultRow[Inventories.reservedStock],
        )

    private suspend fun existProduct(productId: Int): Boolean {
        return dbQuery {
            Products
                .select(Products.id eq productId)
                .count() > 0
        }
    }
}

val inventoryDao = InventoryDAOImpl()
