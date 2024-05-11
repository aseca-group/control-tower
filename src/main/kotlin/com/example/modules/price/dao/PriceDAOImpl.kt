package com.example.modules.price.dao

import com.example.modules.price.model.Price
import com.example.modules.price.model.Prices
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class PriceDAOImpl : PriceDAOFacade {
    private fun resultRowToPrice(row: ResultRow) = Price(   //Esto de aca convierte una linea en un Price
        id = row[Prices.id],
        value = row[Prices.value],
        isUnit = row[Prices.isUnit],
        qty = row[Prices.qty],
    )

    override suspend fun allPrices(): List<Price> {
        return Prices.selectAll().map(::resultRowToPrice)  // De la tabla, por cada linea, mapea a result para agregarlo a la lista
    }

    override suspend fun price(id: Int): Price?{
        return Prices                       // De la tabla
            .select { Prices.id eq id }     // Si encuentra un id igual al que me dan
            .map(::resultRowToPrice)        // convertilo en Price
            .singleOrNull()                 // Si no encontraste nada, damelo null
    }

    override suspend fun addNewPrice(price: Price): Price? {
        val insertStatement = Prices.insert {
            it[value] = value
            it[isUnit] = isUnit
            it[qty] = qty
        }
        return insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToPrice)
    }

    override suspend fun editPrice(price: Price): Boolean {
        return Prices.update({ Prices.id eq price.id }) {
            it[value] = value
            it[isUnit] = isUnit
            it[qty] = qty
        } > 0
    }

    override suspend fun deletePrice(id: Int): Boolean {
        return  Prices.deleteWhere { Prices.id eq id } > 0
    }
}