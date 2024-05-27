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

    suspend fun getDeliveryId(addressId: Int): Int {
        // val url = "http://app/delivery"
        val url = "http://last-mile-planning-back-app-1:8081/delivery"
        val response: HttpResponse =
            client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(AddressIdWrapper(addressId))
            }
        val responseBody = response.body<String>()
        return responseBody.toInt()
    }
}
