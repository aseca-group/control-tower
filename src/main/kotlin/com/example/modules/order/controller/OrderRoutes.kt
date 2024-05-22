@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.modules.order.controller

import com.example.modules.order.dao.orderDao
import com.example.modules.order.model.Order
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Route.order() {
    get("/") {
        call.respondRedirect("order")
    }

    route("order") {
        get {
            call.respond(orderDao.allOrders())
        }

        post {
            val order = call.receive<Order>()
            val createdOrder = orderDao.addNewOrder(order)
            call.respondRedirect("/orders/${createdOrder?.id}")
        }

        get {
            val id = call.parameters.getOrFail<Int>("id").toInt()
            val order: Order = orderDao.order(id)!!
            call.respond(order)
        }

        put("update") {
            val order = call.receive<Order>()
            orderDao.editOrder(order)
            call.respondRedirect("/orders/${order.id}")
        }

        put("delete") {
            val id = call.parameters.getOrFail<Int>("id").toInt()
            orderDao.deleteOrder(id)
            call.respondRedirect("/orders/$id")
        }
    }
}
