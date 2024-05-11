package com.example.modules.location.city

import org.jetbrains.exposed.sql.Table

class City(
    val id: Int,
    val name: String,
) {
}

object Cities : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)

    override val primaryKey = PrimaryKey(id)
}