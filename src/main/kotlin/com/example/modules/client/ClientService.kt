@file:Suppress("ktlint:standard:filename", "ktlint:standard:no-wildcard-imports")

package com.example.modules.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException

class HttpClientService {
    private val client =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                    },
                )
            }
        }

    @Serializable
    data class AddressIdWrapper(val addressId: Int)

    @Serializable
    data class DeliveryDTO(val id: Int, val date: String, val status: String, val addressId: Int, val driverId: Int)

    suspend fun getDeliveryId(addressId: Int): Int {
        //val url = "http://localhost:8081/delivery"
        val url = "http://last-mile-planning-back-app-1:8081/delivery"
        val response: HttpResponse =
            client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(AddressIdWrapper(addressId))
            }
        val responseBody = response.body<String>()
        return responseBody.toInt()
    }

    suspend fun getDeliveries(): List<DeliveryDTO> {
        //val url = "http://localhost:8081/delivery"
        val url = "http://last-mile-planning-back-app-1:8081/delivery"
        val response: HttpResponse = client.get(url)

        if (response.status.isSuccess()) {
            val responseBody = response.body<String>()
            return Json.decodeFromString<List<DeliveryDTO>>(responseBody)
        } else {
            // Handle the error response here if needed
            throw IOException("Failed to fetch deliveries: ${response.status}")
        }
    }

}
