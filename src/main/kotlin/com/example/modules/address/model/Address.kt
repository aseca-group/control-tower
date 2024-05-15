package com.example.modules.address.model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
class Address(
    val id: Int,
    val city: String,
    val road: String,
    val number: Int,
) {
}

object Addresses : Table() {
    val id = integer("id").autoIncrement()
    val city = varchar("city", 50)
    val road = varchar("road", 50)
    val number = integer("number")

    override val primaryKey = PrimaryKey(id)
}