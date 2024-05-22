package com.example.modules.inventory.dao

import com.example.modules.inventory.model.Inventory

interface InventoryDAOFacade {
    suspend fun allInventories(): List<Inventory>

    suspend fun inventory(
        warehouseId: Int,
        productId: Int,
    ): Inventory?

    suspend fun addNewInventory(inventory: Inventory): Inventory?

    suspend fun editInventory(inventory: Inventory): Boolean

    suspend fun deleteInventory(
        warehouseId: Int,
        productId: Int,
    ): Boolean
}
