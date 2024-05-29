package com.example.modules.order.model

import com.example.modules.address.model.Addresses
import com.example.modules.customer.model.Customers
import com.example.modules.product.model.ProductEntity
import com.example.modules.product.model.Products
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

import org.jetbrains.exposed.sql.Table


// This is the kotlin object, we will use this to receive information about orders.
@Serializable
data class Order(val id: Int,
                 val productsId : List<@Contextual ProductQty>,
                 val  addressId: Int,
                 val customerId: Int,
                 val total: Double,
                 val deliveryId: Int,
                 val date: String)

@Serializable()
data class ProductQty(val productId: Int, val qty: Int)

// This defines the database table
object Orders: IntIdTable(){
    val addressId = integer("addressId") references Addresses.id
    val customerId = integer("customerId") references Customers.id
    val deliveryId = integer("deliveryId")
    val total = double("total")
    val date = varchar("date", 50)
}



object OrdersProducts : Table() {
    val orderId = reference("orderId", Orders)
    val productId = reference("productId", Products)
    val qty = integer("qty")
    override val primaryKey = PrimaryKey(orderId, productId)
}
