@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.modules.warehouse.dao

import com.example.modules.warehouse.model.Warehouse
import com.example.modules.warehouse.model.Warehouses
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class WarehouseDAOImpl : WarehouseDAOFacade {
    override suspend fun allWarehouses(): List<Warehouse> {
        return Warehouses.selectAll().map(::resultRowToWarehouse)
    }

    override suspend fun warehouse(id: Int): Warehouse? {
        return Warehouses
            .select(Warehouses.id eq id)
            .map(::resultRowToWarehouse)
            .singleOrNull()
    }

    override suspend fun addNewWarehouse(warehouse: Warehouse): Warehouse? {
        val insertStatement =
            Warehouses.insert {
                it[name] = warehouse.name
            }
        return insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToWarehouse)
    }

    override suspend fun editWarehouse(warehouse: Warehouse): Boolean {
        return Warehouses.update({ Warehouses.id eq warehouse.id }) {
            it[name] = warehouse.name
        } > 0
    }

    override suspend fun deleteWarehouse(id: Int): Boolean {
        return Warehouses.deleteWhere { Warehouses.id eq id } > 0
    }

    private fun resultRowToWarehouse(resultRow: ResultRow) =
        Warehouse(
            id = resultRow[Warehouses.id].value,
            name = resultRow[Warehouses.name],
        )
}

val warehouseDao = WarehouseDAOImpl()
