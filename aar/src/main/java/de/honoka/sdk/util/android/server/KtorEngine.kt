package de.honoka.sdk.util.android.server

import android.webkit.MimeTypeMap
import cn.hutool.core.exceptions.ExceptionUtil
import cn.hutool.json.JSONObject
import de.honoka.sdk.util.android.common.GlobalComponents
import de.honoka.sdk.util.framework.web.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.isActive
import java.io.File

class KtorEngine(
    private val port: Int,
    private val customRoutingList: List<Routing.() -> Unit> = listOf()
) {

    var rawEngine: ApplicationEngine? = null

    val isActive: Boolean get() = rawEngine.let {
        it ?: return@let false
        it.application.isActive
    }

    fun start() {
        if(isActive) rawEngine!!.stop()
        rawEngine = embeddedServer(Netty, port = port, module = { module() }).apply {
            start()
        }
    }

    private fun Application.module() {
        routing {
            customRoutingList.forEach { it() }
            KtorRequestMappings(this).run {
                root()
                staticResource()
                androidImage()
                other()
            }
        }
        install(StatusPages) {
            KtorHttpStatusHandler(this).run {
                notFound()
                notFoundException()
                exception()
            }
        }
    }
}

class KtorRequestMappings(private val routing: Routing) {

    private val assets = GlobalComponents.application.assets

    private val indexHandler: PipelineInterceptor<Unit, ApplicationCall> = {
        val content = assets.open("web/index.html").use { it.readBytes() }
        call.respondBytes(content, ContentType.Text.Html)
    }

    fun root() = listOf("/", "/index.html").forEach {
        routing.get(it, indexHandler)
    }

    fun staticResource() = HttpServer.staticResourcesPrefixes.forEach { path ->
        val mappingPath = if(path.contains(".")) path else "$path/{...}"
        routing.get(mappingPath) {
            val requestPath = call.request.path()
            val content = assets.open("web$requestPath").use { it.readBytes() }
            respondStaticResponse(requestPath, content)
        }
    }

    private suspend fun PipelineContext<Unit, ApplicationCall>.respondStaticResponse(
        urlPath: String,
        content: ByteArray
    ) {
        val fileExt = urlPath.substring(urlPath.lastIndexOf(".") + 1)
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt)!!
        call.respondBytes(content, ContentType.parse(mimeType))
    }

    fun androidImage() = routing.get("${HttpServerVariables.imageUrlPrefix}/{...}") {
        val requestPath = call.request.path()
        val filePath = "${GlobalComponents.application.dataDir}${
            requestPath.substring(HttpServerVariables.imageUrlPrefix.length)
        }"
        val file = File(filePath)
        if(!file.exists()) throw KtorNotFoundException()
        respondStaticResponse(requestPath, file.readBytes())
    }

    fun other() = routing.get("/{...}", indexHandler)
}

class KtorHttpStatusHandler(private val config: StatusPagesConfig) {

    private val notFoundHandler: suspend (ApplicationCall, HttpStatusCode) -> Unit = { call, status ->
        call.respondJson(ApiResponse.fail(status.value, "path: ${call.request.path()}"), status)
    }

    fun notFound() = config.status(HttpStatusCode.NotFound, notFoundHandler)

    fun notFoundException() = config.exception<KtorNotFoundException> { call, _ ->
        notFoundHandler(call, HttpStatusCode.NotFound)
    }

    fun exception() = config.exception<Throwable> { call, t ->
        val status = HttpStatusCode.InternalServerError
        call.respondJson(ApiResponse<Any>().apply {
            code = status.value
            msg = ExceptionUtil.getMessage(t)
            data = JSONObject().also {
                it["stackTrace"] = ExceptionUtil.stacktraceToString(t)
            }
        }, status)
    }
}

internal class KtorNotFoundException : RuntimeException()