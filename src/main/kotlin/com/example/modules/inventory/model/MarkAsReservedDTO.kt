package com.example.modules.inventory.model

import kotlinx.serialization.Serializable

@Serializable
class MarkAsReservedDTO(
    val productId: Int,
    val stockToReserve: Int,
) {
}
