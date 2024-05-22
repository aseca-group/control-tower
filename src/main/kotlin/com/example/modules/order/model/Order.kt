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

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime


// This is the kotlin object, we will use this to receive information about orders.
@Serializable
data class Order(val id: Int,
                 val productsId : List<@Contextual ProductQty>,
                 val  addressId: Int,
                 val customerId: Int,
                 val total: Double,
                 val deliveryId: Int,
                 @Contextual val date: LocalDateTime)

@Serializable()
data class ProductQty(val productId: Int, val qty: Int)

// This defines the database table
object Orders: IntIdTable(){
    val addressId = integer("addressId") references Addresses.id
    val customerId = integer("customerId") references Customers.id
    val deliveryId = integer("deliveryId")
    val total = double("total")
    val date : Column<LocalDateTime> = datetime("date").defaultExpression(CurrentDateTime)
}

class OrderEntity( id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<OrderEntity>(Orders)
    var products by ProductEntity via OrdersProducts
    var customerId by Orders.addressId
    var total by Orders.total
    var date by Orders.date
    var addressId by Orders.addressId
}

object OrdersProducts : Table() {
    val orderId = reference("orderId", Orders)
    val productId = reference("productId", Products)
    val qty = integer("qty")
    override val primaryKey = PrimaryKey(orderId, productId)
}
