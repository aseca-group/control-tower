package com.example.modules.address.dao

import com.example.db.DatabaseSingleton.dbQuery
import com.example.modules.address.model.Address
import com.example.modules.address.model.Addresses
import com.example.modules.address.model.CreateAddressDTO
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class AddressDAOImpl : AddressDAOFacade{
    private fun resultRowToAddress(resultRow: ResultRow) = Address (
        id = resultRow[Addresses.id],
        city = resultRow[Addresses.city],
        road = resultRow[Addresses.road],
        number = resultRow[Addresses.number],
    )

    override suspend fun address(id: Int): Address? = dbQuery {
         Addresses
            .select(Addresses.id eq id)
            .map(::resultRowToAddress)
            .singleOrNull()
    }

    override suspend fun addNewAddress(address: CreateAddressDTO): Address? = dbQuery {
        val insertStatement = Addresses.insert {
            it[city] = address.city
            it[road] = address.road
            it[number] = address.number
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToAddress)
    }

    override suspend fun getAllAddresses(): List<Address> = dbQuery {
        Addresses
            .selectAll()
            .map(::resultRowToAddress)
    }

    override suspend fun deleteAddress(id: Int): Boolean {
        return Addresses.deleteWhere { Addresses.id eq id } > 0
    }
}

val addressDao: AddressDAOImpl = AddressDAOImpl()