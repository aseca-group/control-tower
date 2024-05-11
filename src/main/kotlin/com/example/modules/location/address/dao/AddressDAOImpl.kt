package com.example.modules.location.address.dao

import com.example.modules.location.address.model.Address
import com.example.modules.location.address.model.Addresses
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class AddressDAOImpl : AddressDAOFacade{
    private fun resultRowToAddress(resultRow: ResultRow) = Address (
        id = resultRow[Addresses.id],
        cityId = resultRow[Addresses.cityId],
        road = resultRow[Addresses.road],
        number = resultRow[Addresses.number],
    )

    override suspend fun address(id: Int): Address? {
        return Addresses
            .select(Addresses.id eq id)
            .map(::resultRowToAddress)
            .singleOrNull()
    }

    override suspend fun addNewAddress(address: Address): Address? {
        val insertStatement = Addresses.insert {
            it[cityId] = cityId
            it[road] = road
            it[number] = number
        }
        return insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToAddress)
    }

    override suspend fun deleteAddress(id: Int): Boolean {
        return Addresses.deleteWhere { Addresses.id eq id } > 0
    }
}