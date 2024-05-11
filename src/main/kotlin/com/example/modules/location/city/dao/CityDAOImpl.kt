package com.example.modules.location.city.dao

import com.example.modules.location.city.model.Cities
import com.example.modules.location.city.model.City
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class CityDAOImpl : CityDAOFacade {
    private fun resultRowToCity(resultRow: ResultRow) = City  (
        id = resultRow[Cities.id],
        countryId = resultRow[Cities.countryId],
        name = resultRow[Cities.name],
    )

    override suspend fun city(id: Int): City? {
        return Cities
            .select(Cities.id eq id)
            .map(::resultRowToCity)
            .singleOrNull()
    }

    override suspend fun addNewCity(city: City): City? {
        val insertStatement = Cities.insert {
            it[countryId] = city.countryId
            it[name] = city.name
        }
        return insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToCity)
    }

    override suspend fun deleteCity(id: Int): Boolean {
        return Cities.deleteWhere { Cities.id eq id } > 0
    }
}