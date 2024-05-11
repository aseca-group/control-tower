package com.example.modules.cart

import com.example.modules.product.model.Product
import com.example.modules.product.model.Products
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Table

class Cart(
    val id: Int,

) {
}

object Carts : Table() {
    val id = integer("id").autoIncrement()

    override val primaryKey = PrimaryKey(Products.id)
}

object CartProducts : Table() {
    val cartId = integer("cartId").references(Carts.id)
    val productId = integer("productId").references(Products.id)
}