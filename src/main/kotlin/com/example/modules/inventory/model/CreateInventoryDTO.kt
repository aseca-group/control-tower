package com.example.modules.inventory.model

import kotlinx.serialization.Serializable

@Serializable
class CreateInventoryDTO(
    val productId: Int,
    val stock: Int,
)
