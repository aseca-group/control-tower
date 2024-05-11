package com.example.modules.location.address.model

import com.example.modules.location.city.model.Cities
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
class Address(
    val id: Int,
    val cityId: Int,
    val road: String,
    val number: Int,
) {
}

object Addresses : Table() {
    val id = integer("id").autoIncrement()
    val cityId = integer("cityId") references Cities.id
    val road = varchar("road", 50)
    val number = integer("number")

    override val primaryKey = PrimaryKey(id)
}