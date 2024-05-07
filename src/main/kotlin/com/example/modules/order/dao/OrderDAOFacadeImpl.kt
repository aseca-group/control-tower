package com.example.modules.order.dao

import com.example.modules.article.model.Articles
import com.example.db.DatabaseSingleton.dbQuery
import com.example.modules.order.model.Order
import com.example.modules.order.model.Orders
import com.example.modules.order.model.Orders.id
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class OrderDAOFacadeImpl : OrderDAOFacade {

    private fun resultRowToOrder(row: ResultRow) = Order (
        id = row[Orders.id].value,
        productsId = OrderProducts.id,
        addressId = row[Orders.address].id,
        customerId = row[Orders.customer].id,
        cardId = row[Orders.card].id,
        total = row[Orders.total],
        date = row[Orders.date]
    )
    override suspend fun allOrders(): List<Order> = dbQuery{
        Orders.selectAll().map(::resultRowToOrder)
    }

    override suspend fun order(id: Int): Order? = dbQuery {
        Orders
            .select { Articles.id eq id }
            .map(::resultRowToOrder)
            .singleOrNull()
    }

    override suspend fun addNewOrder(order: Order): Order? = dbQuery{
        val insertStatement = Orders.insert { it ->
            it[Orders.address] = AddressEntity[order.addressId]
            it[Orders.card] = CardEntity[order.cardId]
            it[Orders.customer] = CustomerEntity[order.customerId]
            it[Orders.date] = order.date
            it[Orders.total] = order.total
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToOrder)
    }

    override suspend fun editOrder(order: Order): Boolean  = dbQuery {
        Orders.update({ Orders.id eq id }) {
            it[Orders.address] = AddressEntity[order.addressId]
            it[Orders.card] = CardEntity[order.cardId]
            it[Orders.customer] = CustomerEntity[order.customerId]
            it[Orders.date] = order.date
            it[Orders.total] = order.total
        } > 0
    }

    override suspend fun deleteOrder(id: Int): Boolean = dbQuery {
        Orders.deleteWhere { Orders.id eq id } > 0
    }


}

val orderDao: OrderDAOFacadeImpl = OrderDAOFacadeImpl()