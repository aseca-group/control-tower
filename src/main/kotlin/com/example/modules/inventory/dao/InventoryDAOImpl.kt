package com.example.modules.inventory.dao

import com.example.modules.inventory.model.Inventories
import com.example.modules.inventory.model.Inventory
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class InventoryDAOImpl : InventoryDAOFacade {
    override suspend fun allInventories(): List<Inventory> {
        return Inventories.selectAll().map(::resultRowToInventory)
    }

    override suspend fun inventory(
        warehouseId: Int,
        productId: Int,
    ): Inventory? {
        val condition = (Inventories.warehouseId eq warehouseId) and (Inventories.productId eq productId)
        return Inventories
            .select(condition)
            .map(::resultRowToInventory)
            .singleOrNull()
    }

    override suspend fun addNewInventory(inventory: Inventory): Inventory? {
        val insertStatement =
            Inventories.insert {
                it[warehouseId] = inventory.warehouseId
                it[productId] = inventory.productId
                it[stock] = inventory.stock
                it[reservedStock] = inventory.reservedStock
            }
        return insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToInventory)
    }

    override suspend fun editInventory(inventory: Inventory): Boolean {
        val condition = (Inventories.warehouseId eq inventory.warehouseId) and (Inventories.productId eq inventory.productId)
        return Inventories.update({ condition }) {
            it[warehouseId] = inventory.warehouseId
            it[productId] = inventory.productId
            it[stock] = inventory.stock
            it[reservedStock] = inventory.reservedStock
        } > 0
    }

    override suspend fun deleteInventory(
        warehouseId: Int,
        productId: Int,
    ): Boolean {
        val condition = (Inventories.warehouseId eq warehouseId) and (Inventories.productId eq productId)
        return Inventories.deleteWhere { condition } > 0
    }

    private fun resultRowToInventory(resultRow: ResultRow) =
        Inventory(
            id = resultRow[Inventories.id].value,
            warehouseId = resultRow[Inventories.warehouseId],
            productId = resultRow[Inventories.productId],
            stock = resultRow[Inventories.stock],
            reservedStock = resultRow[Inventories.reservedStock],
        )
}

val inventoryDao = InventoryDAOImpl()
