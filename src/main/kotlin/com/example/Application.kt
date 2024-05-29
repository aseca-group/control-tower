package com.example

import com.example.db.DatabaseSingleton
import com.example.modules.address.controller.address
import com.example.modules.customer.controller.customer
import com.example.modules.delivery.controller.delivery
import com.example.modules.inventory.controller.inventory
import com.example.modules.order.controller.order
import com.example.modules.product.controller.product
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DatabaseSingleton.init()
    install(ContentNegotiation) {
        json()
    }

    install(CORS) {
        anyHost() // Allows requests from any host. For production, restrict this to specific origins.
        allowCredentials = true
        allowNonSimpleContentTypes = true
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader("Content-Type")
        allowHeader("Authorization")
    }

    install(Routing) {
        order()
        address()
        product()
        customer()
        inventory()
        delivery()
    }
}
