package com.example.modules.customer.dao

import com.example.db.DatabaseSingleton.dbQuery
import com.example.modules.customer.model.Customer
import com.example.modules.customer.model.Customers
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class CustomerDaoImpl:CustomerDAOFacade {

    private fun resultRowToCustomer(resultRow: ResultRow) = Customer (
        id = resultRow[Customers.id],
        name = resultRow[Customers.name]
    )

    override suspend fun customer(id: Int): Customer? = dbQuery {
        Customers.select(Customers.id eq id).map(::resultRowToCustomer).singleOrNull()

    }

    override suspend fun addNewCustomer(customer: Customer): Customer? = dbQuery {
        val insertStatement = Customers.insert {
            it[name] = customer.name
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToCustomer)
    }

    override suspend fun deleteCustomer(id: Int): Boolean {
        return Customers.deleteWhere {Customers.id eq id} > 0
    }
}

val customerDao: CustomerDaoImpl = CustomerDaoImpl()