package com.example.modules.address.model

import kotlinx.serialization.Serializable

@Serializable
class CreateAddressDTO(
    val city: String,
    val road: String,
    val number: Int,
) {
}