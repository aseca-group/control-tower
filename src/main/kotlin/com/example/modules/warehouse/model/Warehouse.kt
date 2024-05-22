package com.example.modules.warehouse.model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable

@Serializable
data class Warehouse(
    val id: Int,
    val name: String,
)

object Warehouses : IntIdTable() {
    val name = varchar("name", 255)
}
