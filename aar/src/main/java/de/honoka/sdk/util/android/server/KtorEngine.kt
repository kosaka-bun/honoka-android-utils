package de.honoka.sdk.util.android.server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.isActive

class KtorEngine(private val port: Int) {

    var rawEngine: ApplicationEngine? = null

    val isActive: Boolean get() = rawEngine.let {
        it ?: return@let false
        it.application.isActive
    }

    fun start() {
        if(isActive) rawEngine!!.stop()
        rawEngine = embeddedServer(Netty, port = port, module = { module() }).apply {
            start(false)
        }
    }

    private fun Application.module() {
        routing {
            get("/") {
                call.respondText("Hello World!")
            }
        }
    }
}