package com.example.modules.order.service

import com.example.modules.order.model.CreateOrderDTO
import com.example.modules.product.dao.productDao

class OrderService {
    suspend fun getTotal(order: CreateOrderDTO): Double {
        @Suppress("ktlint:standard:max-line-length")
        return order.productsId.fold(
            0.0,
        ) { total, productQty -> total + ((productDao.product(productQty.productId)?.price ?: 0.0) * productQty.qty) }
        // For each product retrieve the price from database and multiply by quantity, in case the product does not exist (null) multiply by 0.0
    }
}
