package com.example.modules.order.model

import com.example.modules.card.Cards
import com.example.modules.address.model.Addresses
import com.example.modules.customer.model.Customers
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime


// This is the kotlin object, we will use this to receive information about orders.
@Serializable
data class Order(val id: Int,
//                 val productsId : List<Int>,
                 val  addressId: Int, val customerId: Int, val cardId: Int, val total: Double, @Contextual val date: LocalDateTime)

// This defines the database table
object Orders: IntIdTable(){
    val addressId = integer("addressId") references Addresses.id
    val customerId = integer("customerId") references Customers.id
    val cardId = integer("cardId") references Cards.id
    val total = double("total")
    val date : Column<LocalDateTime> = datetime("date").defaultExpression(CurrentDateTime)
}
