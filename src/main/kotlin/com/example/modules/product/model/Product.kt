package com.example.modules.product.model

import com.example.modules.price.model.Prices
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
class Product(
    val id: Int,
    val priceId: Int,
    val costId: Int,
    val name: String,
) {
}

object Products: Table() {
    val id = integer("id").autoIncrement()
    val priceId = integer("priceId") references Prices.id
    val costId = integer("costId") references Prices.id
    val name = varchar("name", 255)

    override val primaryKey = PrimaryKey(id)
}