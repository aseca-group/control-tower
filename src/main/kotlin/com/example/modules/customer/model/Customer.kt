package com.example.modules.customer.model

import com.example.modules.cart.Carts
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
class Customer(
    val id: Int,
    val name: String,
    val cartId: Int,
) {
}

object Customers : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val cartId = integer("cart_id") references Carts.id

    override val primaryKey = PrimaryKey(id)
}