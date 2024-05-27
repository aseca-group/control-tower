package com.example.modules.inventory.model

import kotlinx.serialization.Serializable

@Serializable
class AddStockDTO(
    val productId: Int,
    val stockToAdd: Int,
)
