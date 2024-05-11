package com.example.modules.card

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
class Card(
    val id: Int,
    val bank: String,
    val number: String,
) {
}


object Cards : Table() {
    val id = integer("id").autoIncrement()
    val bank = varchar("bank", 255)
    val number = varchar("number", 255)

    override val primaryKey = PrimaryKey(id)
}