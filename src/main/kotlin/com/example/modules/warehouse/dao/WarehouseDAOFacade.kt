package com.example.modules.warehouse.dao

import com.example.modules.warehouse.model.Warehouse

interface WarehouseDAOFacade {
    suspend fun allWarehouses(): List<Warehouse>
    suspend fun warehouse(id: Int): Warehouse?
    suspend fun addNewWarehouse(warehouse: Warehouse): Warehouse?
    suspend fun editWarehouse(warehouse: Warehouse): Boolean
    suspend fun deleteWarehouse(id: Int): Boolean
}