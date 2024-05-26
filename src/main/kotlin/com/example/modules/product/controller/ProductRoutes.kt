package com.example.modules.product.controller

import com.example.modules.address.dao.addressDao
import com.example.modules.address.model.Address
import com.example.modules.product.dao.productDao
import com.example.modules.product.model.CreateProductDTO
import com.example.modules.product.model.Product
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Route.product() {
    route("/product") {
        get {
            call.respond(productDao.allProducts())
        }
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respondText("Invalid ID")
            } else {
                val product = productDao.product(id)
                if (product == null) {
                    call.respondText("Product not found")
                } else {
                    call.respond(product)
                }
            }
        }
        post {
            val productDTO = call.receive<CreateProductDTO>()
            val createdProduct = productDao.addNewProduct(productDTO)
            if (createdProduct == null) {
                call.respondText("Failed to create product")
            } else {
                call.respond(createdProduct)
            }
        }
        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respondText("Invalid ID")
            } else {
                val deleted = productDao.deleteProduct(id)
                if (deleted) {
                    call.respondText("Product $id deleted")
                } else {
                    call.respondText("Product not found")
                }
            }
        }
    }
}