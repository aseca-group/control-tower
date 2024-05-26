@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.example.modules.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*

class HttpClientService {
    private val client = HttpClient(CIO)

    @OptIn(InternalAPI::class)
    suspend fun getDeliveryId(addressId: Int): Int {
        return client.use { client ->
            val deliveryCreationUrl = "http://127.0.0.1:8081/delivery"
            // send addressId as body
            val response: HttpResponse =
                client.post(deliveryCreationUrl) {
                    contentType(ContentType.Application.Json)
                    body = addressId
                }
            response.body<String>().toInt()
        }
    }
}
