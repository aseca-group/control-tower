package com.example.modules.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

class HttpClientService {

    private val client = HttpClient(CIO)

    suspend fun getDeliveryId(): Int {
       return client.use { client ->
            val deliveryCreationUrl = "" //TODO: Define this url.
            val response = client.post(deliveryCreationUrl)
            response.body<String>().toInt()
       }
    }
}