@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.modules.inventory.controller

import com.example.modules.inventory.dao.inventoryDao
import com.example.modules.inventory.model.CreateInventoryDTO
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
        post {
            val inventory = call.receive<CreateInventoryDTO>()
            val createdInventory = inventoryDao.addNewInventory(inventory)
            call.respondRedirect("/inventory/${createdInventory?.productId}")
        }

        get("/") {
            call.respond(inventoryDao.allInventories())
        }

        get {
            val productId = call.parameters.getOrFail<Int>("productId").toInt()
            val inventory = inventoryDao.inventory(productId)!!
            call.respond(inventory)
        }

        put("update") {
            val inventory = call.receive<Inventory>()
            inventoryDao.editInventory(inventory)
            call.respondRedirect("/inventory/${inventory.productId}")
        }

        delete("delete") {
            val productId = call.parameters.getOrFail<Int>("productId").toInt()
            inventoryDao.deleteInventory(productId)
            call.respondRedirect("/inventory/$productId")
        }
    }
}
