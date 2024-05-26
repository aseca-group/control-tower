package com.example.modules.inventory.dao

import com.example.modules.inventory.model.CreateInventoryDTO
import com.example.modules.inventory.model.Inventory

interface InventoryDAOFacade {
    suspend fun allInventories(): List<Inventory>

    suspend fun inventory(productId: Int): Inventory?

    suspend fun addNewInventory(inventory: CreateInventoryDTO): Inventory?

    suspend fun editInventory(inventory: Inventory): Boolean

    suspend fun deleteInventory(productId: Int): Boolean
}
