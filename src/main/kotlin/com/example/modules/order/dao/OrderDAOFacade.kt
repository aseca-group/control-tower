package com.example.modules.order.dao

import com.example.modules.order.model.CreateOrderDTO
import com.example.modules.order.model.Order

interface OrderDAOFacade {
    suspend fun allOrders(): List<Order>
    suspend fun order(id: Int): Order?
    suspend fun addNewOrder(order: CreateOrderDTO, createdDeliveryId: Int, createdTotal: Double): Order?
    suspend fun editOrder(order: Order): Boolean
    suspend fun deleteOrder(id: Int): Boolean
}