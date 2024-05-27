package com.example.modules.inventory.dao

import com.example.modules.inventory.model.AddStockDTO
import com.example.modules.inventory.model.CreateInventoryDTO
import com.example.modules.inventory.model.Inventory
import com.example.modules.inventory.model.MarkAsReservedDTO
import com.example.modules.inventory.model.MarkAsUnreservedDTO
import com.example.modules.inventory.model.RemoveReservedStockDTO
import com.example.modules.inventory.model.RemoveStockDTO

interface InventoryDAOFacade {
    suspend fun allInventories(): List<Inventory>

    suspend fun inventory(productId: Int): Inventory?

    suspend fun addNewInventory(inventory: CreateInventoryDTO): Inventory?

    suspend fun addStock(inventory: AddStockDTO): Inventory?

    suspend fun markAsReserved(inventory: MarkAsReservedDTO): Inventory?

    suspend fun markAsUnreserved(inventory: MarkAsUnreservedDTO): Inventory?

    suspend fun removeStock(inventory: RemoveStockDTO): Inventory?

    suspend fun removeReservedStock(inventory: RemoveReservedStockDTO): Inventory?

    suspend fun deleteInventory(productId: Int): Boolean
}
