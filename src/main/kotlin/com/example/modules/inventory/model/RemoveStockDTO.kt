package com.example.modules.inventory.model

import kotlinx.serialization.Serializable

@Serializable
class RemoveStockDTO(
    val productId: Int,
    val stockToRemove: Int,
)
