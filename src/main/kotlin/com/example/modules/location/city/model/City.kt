package com.example.modules.location.city.model

import com.example.modules.location.country.model.Countries
import org.jetbrains.exposed.sql.Table

class City(
    val id: Int,
    val countryId: Int,
    val name: String,
) {
}

object Cities : Table() {
    val id = integer("id").autoIncrement()
    val countryId = integer("countryId") references Countries.id
    val name = varchar("name", 255)

    override val primaryKey = PrimaryKey(id)
}