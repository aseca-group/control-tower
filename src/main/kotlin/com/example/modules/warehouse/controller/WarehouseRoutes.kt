@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.modules.warehouse.controller

import com.example.modules.warehouse.dao.warehouseDao
import com.example.modules.warehouse.model.Warehouse
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Route.warehouse() {
    get("/") {
        call.respondText("Warehouse")
    }

    route("warehouse") {
        get {
            call.respond(warehouseDao.allWarehouses())
        }

        get {
            val id = call.parameters.getOrFail<Int>("id").toInt()
            val warehouse = warehouseDao.warehouse(id)!!
            call.respond(warehouse)
        }

        post {
            val warehouse = call.receive<Warehouse>()
            val createdWarehouse = warehouseDao.addNewWarehouse(warehouse)
            call.respondRedirect("/warehouse/${createdWarehouse?.id}")
        }

        put("update") {
            val warehouse = call.receive<Warehouse>()
            warehouseDao.editWarehouse(warehouse)
            call.respondRedirect("/warehouse/${warehouse.id}")
        }

        delete("delete") {
            val id = call.parameters.getOrFail<Int>("id").toInt()
            warehouseDao.deleteWarehouse(id)
            call.respondRedirect("/warehouse/$id")
        }
    }
}
