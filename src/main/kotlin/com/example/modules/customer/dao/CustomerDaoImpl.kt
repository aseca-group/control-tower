package com.example.modules.customer.dao

import com.example.db.DatabaseSingleton.dbQuery
import com.example.modules.customer.model.CreateCustomerDTO
import com.example.modules.customer.model.Customer
import com.example.modules.customer.model.Customers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class CustomerDaoImpl:CustomerDAOFacade {

    private fun resultRowToCustomer(resultRow: ResultRow) = Customer (
        id = resultRow[Customers.id],
        name = resultRow[Customers.name]
    )

    override suspend fun customer(id: Int): Customer? = dbQuery {
        Customers.select(Customers.id eq id).map(::resultRowToCustomer).singleOrNull()

    }

    override suspend fun addNewCustomer(customer: CreateCustomerDTO): Customer? = dbQuery {
        val insertStatement = Customers.insert {
            it[name] = customer.name
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToCustomer)
    }

    override suspend fun deleteCustomer(id: Int): Boolean = dbQuery {
        Customers.deleteWhere {Customers.id eq id} > 0
    }

    override suspend fun getAllCustomers(): List<Customer> = dbQuery {
        Customers.selectAll().map(::resultRowToCustomer)
    }
}

val customerDao: CustomerDaoImpl = CustomerDaoImpl()