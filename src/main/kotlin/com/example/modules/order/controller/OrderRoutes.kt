package com.example.modules.order.controller

import com.example.modules.client.HttpClientService
import com.example.modules.order.dao.orderDao
import com.example.modules.order.model.CreateOrderDTO
import com.example.modules.order.model.Order
import com.example.modules.order.service.OrderService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlinx.coroutines.runBlocking

val clientService = HttpClientService()
val orderService = OrderService()

fun Route.order() {
    get("/") {
        call.respondRedirect("/order")
    }

    route("/order") {
        get {
            call.respond(orderDao.allOrders())
        }

        post {
            val order = call.receive<CreateOrderDTO>()
            val total = orderService.getTotal(order)
            if (!orderDao.checkStockAvailability(order.productsId)) {
                call.respond(HttpStatusCode.BadRequest, "Not enough stock available")
                return@post
            }
            val deliveryId = runBlocking { clientService.getDeliveryId(order.addressId) }
            val createdOrder = orderDao.addNewOrder(order, deliveryId, total)
            call.respondRedirect("/order/${createdOrder?.id}")
        }

        get("/{id}") {
            val id = call.parameters.getOrFail<Int>("id").toInt()
            val order: Order? = orderDao.order(id)
            if (order != null) {
                call.respond(order)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        put("/{id}/update") {
            val id = call.parameters.getOrFail<Int>("id").toInt()
            val order = call.receive<Order>()
            if (order.id == id) {
                if (orderDao.editOrder(order)) {
                    call.respondRedirect("/orders/${order.id}")
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Order ID in path does not match ID in body")
            }
        }

        delete("/{id}") {
            val id = call.parameters.getOrFail<Int>("id").toInt()
            if (orderDao.deleteOrder(id)) {
                call.respondRedirect("/orders/${id}")
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}