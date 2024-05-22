@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.modules.inventory.controller

import com.example.modules.inventory.dao.inventoryDao
import com.example.modules.inventory.model.Inventory
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Route.inventory() {
    get("/") {
        call.respondRedirect("inventory")
    }

    route("inventory") {
        get("/") {
            call.respond(inventoryDao.allInventories())
        }

        get {
            val warehouseId = call.parameters.getOrFail<Int>("warehouseId").toInt()
            val productId = call.parameters.getOrFail<Int>("productId").toInt()
            val inventory = inventoryDao.inventory(warehouseId, productId)!!
            call.respond(inventory)
        }

        post {
            val inventory = call.receive<Inventory>()
            val createdInventory = inventoryDao.addNewInventory(inventory)
            call.respondRedirect("/inventory/${createdInventory?.id}")
        }

        put("update") {
            val inventory = call.receive<Inventory>()
            inventoryDao.editInventory(inventory)
            call.respondRedirect("/inventory/${inventory.id}")
        }

        delete("delete") {
            val warehouseId = call.parameters.getOrFail<Int>("warehouseId").toInt()
            val productId = call.parameters.getOrFail<Int>("productId").toInt()
            inventoryDao.deleteInventory(warehouseId, productId)
            call.respondRedirect("/inventory/$warehouseId/$productId")
        }
    }
}
