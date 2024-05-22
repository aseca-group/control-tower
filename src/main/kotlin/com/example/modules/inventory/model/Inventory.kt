package com.example.modules.inventory.model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable

@Serializable
data class Inventory(
    val id: Int,
    val warehouseId: Int,
    val productId: Int,
    val stock: Int,
    val reservedStock: Int,
)

object Inventories : IntIdTable() {
    val warehouseId = integer("warehouse_id")
    val productId = integer("product_id")
    val stock = integer("stock")
    val reservedStock = integer("reserved_stock")
}
