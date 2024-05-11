package com.example.modules.order.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime


// This is the kotlin object, we will use this to receive information about orders.
@Serializable
data class Order(val id: Int,val productsId : List<Int>, val  addressId: Int, val customerId: Int, val cardId: Int, val total: Double, @Contextual val date: LocalDateTime)

// This defines the database table
object Orders: IntIdTable(){
    val address = reference("address", Addresses)
    val customer = reference("customer", Customers)
    val card = reference("card", Card)
    val total = double("total")
    val date : Column<LocalDateTime> = datetime("date").defaultExpression(CurrentDateTime)
}

// This is the entity
class OrderEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<OrderEntity>(Orders)
    var products by Product via OrderProducts
    var address by Orders.address
    var customer by Orders.customer
    var card by Orders.card
    var total by Orders.total
    var date by Orders.date

//    fun toOrder(): Order {
//
//        return Order(this.id.value,productsId , address.id.value, customer.id.value, card.id.value, total, date)
//    }
}