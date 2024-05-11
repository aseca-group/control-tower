package com.example.modules.location.country.dao

import com.example.modules.location.country.model.Countries
import com.example.modules.location.country.model.Country
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class CountryDAOImpl : CountryDAOFacade {
    private fun resultRowToCountry(row: ResultRow) = Country(
        id = row[Countries.id],
        name = row[Countries.name],
    )

    override suspend fun country(id: Int): Country? {
        return Countries
            .select(Countries.id eq id)
            .map(::resultRowToCountry)
            .singleOrNull()
    }

    override suspend fun addNewCountry(country: Country): Country? {
        val insertStatement = Countries.insert {
            it[name] = country.name
        }
        return insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToCountry)
    }

    override suspend fun deleteCountry(id: Int): Boolean {
        return Countries.deleteWhere { Countries.id eq id } > 0
    }

}