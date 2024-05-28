package com.example.modules.delivery.controller

import com.example.modules.client.HttpClientService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.delivery() {
    val clientService = HttpClientService()
    route("/delivery") {
        get {
            val deliveries = clientService.getDeliveries()
            call.respond(deliveries)
        }

    }
}