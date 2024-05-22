package com.example.modules.order.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
class CreateOrderDTO(
    val productsId : List<@Contextual ProductQty>,
    val  addressId: Int,
    val customerId: Int,
    val total: Double
)