package com.example.modules.order.dao

import com.example.modules.article.model.Articles
import com.example.db.DatabaseSingleton.dbQuery
import com.example.modules.order.model.Order
import com.example.modules.order.model.Orders
import com.example.modules.order.model.Orders.id
import com.example.modules.order.model.OrdersProducts
import com.example.modules.order.model.OrdersProducts.productId
import com.example.modules.order.model.OrdersProducts.qty
import com.example.modules.order.model.ProductQty
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class OrderDAOFacadeImpl : OrderDAOFacade {

    private fun resultRowToOrder(row: ResultRow) = Order (
        id = row[Orders.id].value,
        productsId = OrdersProducts.select { OrdersProducts.orderId eq id }.map { ProductQty (it[productId].value, it[qty]) }, //Select all entries that have our order id and get the product id and quantity.
        addressId = row[Orders.addressId],
        customerId = row[Orders.customerId],
        total = row[Orders.total],
        deliveryId = row[Orders.deliveryId],
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
            it[Orders.addressId] = order.addressId
            it[Orders.customerId] = order.customerId
            it[Orders.deliveryId] = order.deliveryId
            it[Orders.date] = order.date
            it[Orders.total] = order.total
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToOrder)
    }

    override suspend fun editOrder(order: Order): Boolean  = dbQuery {
        Orders.update({ Orders.id eq id }) {
            it[Orders.addressId] = order.addressId
            it[Orders.customerId] = order.customerId
            it[Orders.deliveryId] = order.deliveryId
            it[Orders.date] = order.date
            it[Orders.total] = order.total
        } > 0
    }

    override suspend fun deleteOrder(id: Int): Boolean = dbQuery {
        Orders.deleteWhere { Orders.id eq id } > 0
    }


}

val orderDao: OrderDAOFacadeImpl = OrderDAOFacadeImpl()