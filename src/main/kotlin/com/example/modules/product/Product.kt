package com.example.modules.product

import com.example.modules.price.Price
import kotlinx.serialization.Serializable

@Serializable
class Product(
    val id: Int,
    val price: Price,
    val cost: Price,
    val name: String,
) {
}