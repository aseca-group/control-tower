@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.modules.inventory.dao

import com.example.db.DatabaseSingleton.dbQuery
import com.example.modules.inventory.model.CreateInventoryDTO
import com.example.modules.inventory.model.Inventories
import com.example.modules.inventory.model.Inventory
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class InventoryDAOImpl : InventoryDAOFacade {
    override suspend fun allInventories(): List<Inventory> {
        return Inventories.selectAll().map(::resultRowToInventory)
    }

    override suspend fun inventory(productId: Int): Inventory? =
        dbQuery {
            val condition = (Inventories.productId eq productId)
            Inventories
                .select(condition)
                .map(::resultRowToInventory)
                .singleOrNull()
        }

    override suspend fun addNewInventory(inventory: CreateInventoryDTO): Inventory? =
        dbQuery {
            val insertStatement =
                Inventories.insert {
                    it[productId] = inventory.productId
                    it[stock] = inventory.stock
                    it[reservedStock] = 0
                }
            insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToInventory)
        }

    override suspend fun editInventory(inventory: Inventory): Boolean =
        dbQuery {
            Inventories.update({ Inventories.productId eq inventory.productId }) {
                it[stock] = inventory.stock
                it[reservedStock] = inventory.reservedStock
            } > 0
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
}

val inventoryDao = InventoryDAOImpl()
