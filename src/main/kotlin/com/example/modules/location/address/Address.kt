package com.example.modules.location.address

import com.example.modules.location.city.City
import org.jetbrains.exposed.sql.Table

class Address(
    val id: Int,
    val city: City,
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