package com.example.modules.location.country.model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
class Country(
    val id: Int,
    val name: String,
) {
}

object Countries : Table() {
    val id = integer("country_id").autoIncrement()
    val name = varchar("country_name", 255)

    override val primaryKey = PrimaryKey(id)
}
