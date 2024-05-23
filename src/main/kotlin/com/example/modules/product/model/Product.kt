package com.example.modules.product.model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

@Serializable
class Product(
    val id: Int,
    val price: Double,
    val name: String,
) {
}

object Products: IntIdTable() {
    val price = double("price")
    val name = varchar("name", 255)

}

class ProductEntity(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<ProductEntity>(Products)
    var price by Products.price
    var name by Products.name
}