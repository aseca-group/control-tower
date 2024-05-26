package com.example.modules.customer.controller

import com.example.modules.customer.dao.customerDao
import com.example.modules.customer.model.CreateCustomerDTO
import com.example.modules.customer.model.Customer
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Route.customer() {
    route("/customer") {
        get {
            call.respond(customerDao.getAllCustomers())
        }
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respondText("Invalid ID")
            } else {
                val customer = customerDao.customer(id)
                if (customer == null) {
                    call.respondText("Customer not found")
                } else {
                    call.respond(customer)
                }
            }
        }
        post {
            val customerDTO = call.receive<CreateCustomerDTO>()
            val createdCustomer = customerDao.addNewCustomer(customerDTO)
            if (createdCustomer == null) {
                call.respondText("Failed to create customer")
            } else {
                call.respond(createdCustomer)
            }
        }
        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respondText("Invalid ID")
            } else {
                val deleted = customerDao.deleteCustomer(id)
                if (deleted) {
                    call.respondText("Customer $id deleted")
                } else {
                    call.respondText("Customer not found")
                }
            }
        }
    }
}