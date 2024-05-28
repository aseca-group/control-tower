@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.modules.inventory.controller

import com.example.modules.inventory.dao.inventoryDao
import com.example.modules.inventory.model.AddStockDTO
import com.example.modules.inventory.model.CreateInventoryDTO
import com.example.modules.inventory.model.MarkAsReservedDTO
import com.example.modules.inventory.model.MarkAsUnreservedDTO
import com.example.modules.inventory.model.RemoveReservedStockDTO
import com.example.modules.inventory.model.RemoveStockDTO
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.inventory() {
    route("inventory") {
        // funciona
        post {
            val inventory = call.receive<CreateInventoryDTO>()
            val createdInventory = inventoryDao.addNewInventory(inventory)
            if (createdInventory != null) {
                call.respondRedirect("/inventory/${createdInventory.productId}")
            } else {
                call.respond(HttpStatusCode.BadRequest, "Error: Product does not exists, thus inventory wasn't created.")
            }
        }

        // funciona
        get("/") {
            call.respond(inventoryDao.allInventories())
        }

        // funciona
        get("/{productId}") {
            val productId = call.parameters["productId"]?.toIntOrNull()
            if (productId == null) {
                call.respond(HttpStatusCode.BadRequest, "Error: invalid id.")
            } else {
                val inventory = inventoryDao.inventory(productId)
                if (inventory == null) {
                    call.respond(HttpStatusCode.NotFound, "Error: inventory not found.")
                } else {
                    call.respond(inventory)
                }
            }
        }

        // funciona
        delete("/delete/{productId}") {
            val productId = call.parameters["productId"]?.toIntOrNull()
            if (productId == null) {
                call.respond(HttpStatusCode.BadRequest, "Error: invalid id.")
            } else {
                val deleted = inventoryDao.deleteInventory(productId)
                if (deleted) {
                    call.respond(HttpStatusCode.OK, "product $productId inventory deleted.")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Error: inventory not found.")
                }
            }
        }

        patch("/addStock") {
            val addStockDTO = call.receive<AddStockDTO>()
            if (addStockDTO.stockToAdd <= 0) {
                call.respond(HttpStatusCode.BadRequest, "Error: Stock to add must be a positive number.")
                return@patch
            }
            val updatedInventory = inventoryDao.addStock(addStockDTO)
            if (updatedInventory != null) {
                call.respond(HttpStatusCode.OK, "Updated stock amount: ${updatedInventory.stock}")
            } else {
                call.respond(HttpStatusCode.BadRequest, "Error: Failed to add stock.")
            }
        }

        patch("/markAsReserved") {
            val markAsReservedDTO = call.receive<MarkAsReservedDTO>()
            if (markAsReservedDTO.stockToReserve <= 0) {
                call.respond(HttpStatusCode.BadRequest, "Error: Reserved stock must be a positive number.")
                return@patch
            }
            val updatedInventory = inventoryDao.markAsReserved(markAsReservedDTO)
            if (updatedInventory != null) {
                call.respond(HttpStatusCode.OK, "Updated reserved stock amount: ${updatedInventory.reservedStock}")
            } else {
                call.respond(HttpStatusCode.BadRequest, "Error: Failed to mark stock as reserved.")
            }
        }

        patch("/unreserve") {
            val markAsUnreservedDTO = call.receive<MarkAsUnreservedDTO>()
            if (markAsUnreservedDTO.stockToUnreserve <= 0) {
                call.respond(HttpStatusCode.BadRequest, "Error: Amount to unreserved must be positive.")
                return@patch
            }
            val updatedInventory = inventoryDao.markAsUnreserved(markAsUnreservedDTO)
            if (updatedInventory != null) {
                call.respond(HttpStatusCode.OK, "Updated reserved stock amount: ${updatedInventory.reservedStock}")
            } else {
                call.respond(HttpStatusCode.BadRequest, "Error: Failed to unmark stock as reserved.")
            }
        }

        patch("/removeStock") {
            val removeStockDTO = call.receive<RemoveStockDTO>()
            if (removeStockDTO.stockToRemove <= 0) {
                call.respond(HttpStatusCode.BadRequest, "Error: Stock to remove must be a positive number.")
                return@patch
            }
            val updatedInventory = inventoryDao.removeStock(removeStockDTO)
            if (updatedInventory != null) {
                call.respond(HttpStatusCode.OK, "Updated stock amount: ${updatedInventory.stock}")
            } else {
                call.respond(HttpStatusCode.BadRequest, "Error: Stock could not be removed.")
            }
        }

        patch("/removeReservedStock") {
            val removeReservedStockDTO = call.receive<RemoveReservedStockDTO>()
            val updatedInventories = inventoryDao.removeReservedStock(removeReservedStockDTO)
            if (updatedInventories.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, updatedInventories.map { "Product ID: ${it.productId}, Updated reserved stock: ${it.reservedStock}" })
            } else {
                call.respond(HttpStatusCode.BadRequest, "Error: Reserved stock could not be removed.")
            }
        }
    }
}
