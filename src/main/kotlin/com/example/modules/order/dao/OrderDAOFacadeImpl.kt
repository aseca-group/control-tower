@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.modules.order.dao

import com.example.db.DatabaseSingleton.dbQuery
import com.example.modules.inventory.model.Inventories
import com.example.modules.order.model.CreateOrderDTO
import com.example.modules.order.model.Order
import com.example.modules.order.model.Orders
import com.example.modules.order.model.Orders.id
import com.example.modules.order.model.OrdersProducts
import com.example.modules.order.model.OrdersProducts.productId
import com.example.modules.order.model.OrdersProducts.qty
import com.example.modules.order.model.ProductQty
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime

class OrderDAOFacadeImpl : OrderDAOFacade {
    private fun resultRowToOrder(row: ResultRow): Order {
        val orderId = row[Orders.id].value
        val products =
            OrdersProducts
                .innerJoin(Orders)
                .slice(OrdersProducts.productId, OrdersProducts.qty)
                .select { Orders.id eq orderId }
                .map { ProductQty(it[OrdersProducts.productId].value, it[OrdersProducts.qty]) }

        return Order(
            id = row[Orders.id].value,
            productsId = products,
            addressId = row[Orders.addressId],
            customerId = row[Orders.customerId],
            total = row[Orders.total],
            deliveryId = row[Orders.deliveryId],
            date = row[Orders.date],
        )
    }

    private suspend fun checkStockAvailability(products: List<ProductQty>): Boolean {
        // Convertir la lista de productos a un conjunto de IDs para la consulta
        val productIds = products.map { it.productId }.toSet()

        // Hacer una sola consulta para obtener todos los inventarios necesarios
        val productStocks =
            dbQuery {
                Inventories
                    .select { Inventories.productId inList productIds }
                    .map { it[Inventories.productId] to it }
                    .toMap()
            }

        // Verificar la disponibilidad de stock en memoria
        for (product in products) {
            val productStock = productStocks[product.productId]
            if (productStock != null) {
                val newReservedStock = productStock[Inventories.reservedStock] + product.qty
                if (newReservedStock > productStock[Inventories.stock]) {
                    return false
                }
            } else {
                return false
            }
        }
        return true
    }

    private suspend fun addReservedStock(products: List<ProductQty>) {
        dbQuery {
            // Convertir la lista de productos a un conjunto de IDs para la consulta
            val productIds = products.map { it.productId }.toSet()

            // Hacer una sola consulta para obtener todos los inventarios necesarios
            val productStocks =
                Inventories
                    .select { Inventories.productId inList productIds }
                    .associateBy { it[Inventories.productId] }

            // Realizar todas las actualizaciones dentro de una transacción
            products.forEach { product ->
                val productStock = productStocks[product.productId]
                if (productStock != null) {
                    val newReservedStock = productStock[Inventories.reservedStock] + product.qty
                    Inventories.update({ Inventories.productId eq product.productId }) {
                        it[reservedStock] = newReservedStock
                    }
                }
            }
        }
    }

    override suspend fun allOrders(): List<Order> =
        dbQuery {
            Orders.selectAll().map(::resultRowToOrder)
        }

    override suspend fun order(id: Int): Order? =
        dbQuery {
            Orders
                .select { Orders.id eq id }
                .map(::resultRowToOrder)
                .singleOrNull()
        }

    override suspend fun addNewOrder(
        order: CreateOrderDTO,
        createdDeliveryId: Int,
        createdTotal: Double,
    ): Order? {
        if (!checkStockAvailability(order.productsId)) {
            return null
        }
        val newOrder =
            dbQuery {
                val insertStatement =
                    Orders.insert {
                        it[addressId] = order.addressId
                        it[customerId] = order.customerId
                        it[deliveryId] = createdDeliveryId
                        it[total] = createdTotal
                        it[date] = LocalDateTime.now().toString()
                    }
                val createdOrder: Order? = insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToOrder)

                if (createdOrder != null) {
                    order.productsId.forEach { productQty ->
                        OrdersProducts.insert {
                            it[orderId] = createdOrder.id
                            it[productId] = productQty.productId
                            it[qty] = productQty.qty
                        }
                    }
                }

                createdOrder
            }
        if (newOrder != null) {
            addReservedStock(order.productsId)
            return newOrder
        }
        return null
    }

    override suspend fun editOrder(order: Order): Boolean =
        dbQuery {
            Orders.update({ Orders.id eq id }) {
                it[addressId] = order.addressId
                it[customerId] = order.customerId
                it[deliveryId] = order.deliveryId
                it[date] = order.date
                it[total] = order.total
            } > 0
        }

    override suspend fun deleteOrder(id: Int): Boolean =
        dbQuery {
            Orders.deleteWhere { Orders.id eq id } > 0
        }
}

val orderDao: OrderDAOFacadeImpl = OrderDAOFacadeImpl()
