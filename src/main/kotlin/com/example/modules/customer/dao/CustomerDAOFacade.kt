package com.example.modules.customer.dao

import com.example.modules.customer.model.Customer

interface CustomerDAOFacade {
    suspend fun customer(id: Int): Customer?
    suspend fun addNewCustomer(customer: Customer): Customer?
    suspend fun deleteCustomer(id: Int): Boolean
}