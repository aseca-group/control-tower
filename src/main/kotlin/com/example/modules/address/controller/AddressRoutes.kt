package com.example.modules.address.controller

import com.example.modules.address.dao.addressDao
import com.example.modules.address.model.Address
import com.example.modules.address.model.CreateAddressDTO
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Route.address() {
    get("/") {
        call.respondRedirect("address")
    }

    route("address") {
        post {
            val address = call.receive<CreateAddressDTO>()
            val createdAddress = addressDao.addNewAddress(address)
            call.respondRedirect("/address/${createdAddress?.id}")
        }

        get("{id}") {
            val id = call.parameters.getOrFail<Int>("id").toInt()
            val address: Address = addressDao.address(id)!!
            call.respond(address)
        }
    }
}

