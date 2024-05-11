package com.example.modules.user

import com.example.modules.cart.Cart
import kotlinx.serialization.Serializable

@Serializable
class Customer(
    val id: Int,
    val name: String,
    val cart: Cart,
) {
}