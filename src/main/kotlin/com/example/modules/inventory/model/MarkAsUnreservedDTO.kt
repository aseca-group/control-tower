package com.example.modules.inventory.model

import kotlinx.serialization.Serializable

@Serializable
class MarkAsUnreservedDTO(
    val productId: Int,
    val stockToUnreserve: Int,
)
