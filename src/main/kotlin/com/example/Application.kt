package com.example

import com.example.article.controller.article
import com.example.db.DatabaseSingleton
import com.example.plugins.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DatabaseSingleton.init()
    install(ContentNegotiation) {
        jackson()
    }
    install(Routing) {
        article()
    }

}
