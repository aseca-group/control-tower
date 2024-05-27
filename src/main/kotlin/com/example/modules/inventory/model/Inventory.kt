package com.example.modules.inventory.model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
data class Inventory(
    val productId: Int,
    val stock: Int,
    val reservedStock: Int,
)

object Inventories : Table() {
    val productId = integer("product_id")
    val stock = integer("stock")
    val reservedStock = integer("reserved_stock")
    override val primaryKey = PrimaryKey(productId)
}
