package com.example.modules.product.model

import kotlinx.serialization.Serializable

@Serializable
class CreateProductDTO(
    val price: Double,
    val name: String,
)
