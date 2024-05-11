package com.example.modules.location.country

import org.jetbrains.exposed.sql.Table

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
