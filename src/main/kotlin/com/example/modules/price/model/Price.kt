package com.example.modules.price.model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
class Price(
    val id: Int,
    val value: Double,
    val isUnit: Boolean,
    val qty: Double? = null,
) {
}

object Prices: Table(){
    val id = integer("id").autoIncrement()
    val value = double("value")
    val isUnit = bool("is_unit")
    val weight = double("weight").nullable()

    override val primaryKey = PrimaryKey(id)
}

