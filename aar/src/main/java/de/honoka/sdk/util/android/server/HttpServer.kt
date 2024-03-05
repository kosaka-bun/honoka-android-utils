@file:Suppress("MemberVisibilityCanBePrivate")

package de.honoka.sdk.util.android.server

import fi.iki.elonen.NanoHTTPD

@Suppress("ConstPropertyName")
object HttpServerVariables {

    var serverPort = 38081

    const val imageUrlPrefix = "/android/img"

    fun getUrlByPrefix(path: String) = "http://localhost:$serverPort$path"

    fun getImageUrlByPrefix(path: String) = getUrlByPrefix("$imageUrlPrefix$path")
}

class HttpServer(val port: Int = HttpServerVariables.serverPort) {

    companion object {

        lateinit var instance: HttpServer

        private val staticResourcesPrefixes = arrayOf(
            "/assets", "/font", "/img", "/js", "/favicon.ico"
        )

        fun createInstance() {
            HttpServerUtils.initServerPorts()
            instance = HttpServer().apply { start() }
        }

        fun checkOrRestartInstance() {
            if(!::instance.isInitialized) {
                createInstance()
                return
            }
            if(instance.isActive) return
            createInstance()
        }
    }

    private var engine: KtorEngine = KtorEngine(port)

    val isActive get() = engine.isActive

    fun start() = engine.start()

    fun stop() = engine.rawEngine?.stop()
}

object HttpServerUtils {

    fun getOneAvaliablePort(startPort: Int): Int {
        var port = startPort
        var successful = false
        //验证端口可用性
        for(i in 0 until 10) {
            try {
                DefaultNanoHttpd(port).apply {
                    start()
                    stop()
                }
                successful = true
                break
            } catch(t: Throwable) {
                port += 1
            }
        }
        if(!successful) throw Exception("端口范围（$startPort - ${startPort + 10}）均被占用")
        return port
    }

    fun initServerPorts() {
        HttpServerVariables.serverPort = getOneAvaliablePort(HttpServerVariables.serverPort)
    }
}

internal class DefaultNanoHttpd(port: Int) : NanoHTTPD(port)