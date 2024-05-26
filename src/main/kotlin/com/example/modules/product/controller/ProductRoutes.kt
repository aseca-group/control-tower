@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.modules.product.controller

import com.example.modules.product.dao.productDao
import com.example.modules.product.model.CreateProductDTO
import com.example.modules.product.model.Product
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Route.product() {
    get("/") {
        call.respondRedirect("product")
    }

    route("product") {
        post {
            val product = call.receive<CreateProductDTO>()
            val createdProduct = productDao.addNewProduct(product)
            call.respondRedirect("/product/${createdProduct?.id}")
        }

        get("{id}") {
            val id = call.parameters.getOrFail<Int>("id").toInt()
            val product: Product = productDao.product(id)!!
            call.respond(product)
        }

        delete("{id}") {
            val id = call.parameters.getOrFail<Int>("id").toInt()
            val deleted = productDao.deleteProduct(id)
            call.respond(deleted)
        }
    }
}
