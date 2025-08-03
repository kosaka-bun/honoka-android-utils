@file:Suppress("MemberVisibilityCanBePrivate")

package de.honoka.sdk.util.android.server

import de.honoka.sdk.util.android.server.ktor.KtorEngine

object HttpServerVariables {

    internal const val FIRST_TRY_PORT = 38081

    internal const val IMAGE_URL_PREFIX = "/android/img"

    fun getUrlByPath(path: String): String = "http://localhost:${HttpServer.server!!.port}$path"

    fun getImageUrlByPath(path: String): String = getUrlByPath("$IMAGE_URL_PREFIX$path")

    fun getApiUrlByPath(path: String): String = getUrlByPath("/api$path")
}

object HttpServer {

    internal var server: KtorEngine? = null

    internal val staticResourcesPrefixes = arrayOf(
        "/assets", "/font", "/img", "/js", "/favicon.ico"
    )

    private lateinit var usingOptions: KtorEngine.Options

    @Synchronized
    fun start(options: KtorEngine.Options? = null): KtorEngine {
        if(server?.isActive == true) {
            server!!.stop()
        }
        options?.let {
            usingOptions = it
        }
        server = KtorEngine(usingOptions).apply {
            start()
        }
        return server!!
    }

    @Synchronized
    fun restartIfStopped(): KtorEngine {
        if(server?.isActive == true) {
            return server!!
        }
        return start()
    }
}
