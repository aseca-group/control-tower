@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.modules.address.controller

import com.example.modules.address.dao.addressDao
import com.example.modules.address.model.CreateAddressDTO
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Route.address() {
    route("/address") {
        get {
            call.respond(addressDao.getAllAddresses())
        }
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respondText("Invalid ID")
            } else {
                val address = addressDao.address(id)
                if (address == null) {
                    call.respondText("Address not found")
                } else {
                    call.respond(address)
                }
            }
        }
        post {
            val addressDTO = call.receive<CreateAddressDTO>()
            val createdAddress = addressDao.addNewAddress(addressDTO)
            if (createdAddress == null) {
                call.respondText("Failed to create address")
            } else {
                call.respond(createdAddress)
            }
        }
        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respondText("Invalid ID")
            } else {
                val deleted = addressDao.deleteAddress(id)
                if (deleted) {
                    call.respondText("Address $id deleted")
                } else {
                    call.respondText("Address not found")
                }
            }
        }
    }
}
