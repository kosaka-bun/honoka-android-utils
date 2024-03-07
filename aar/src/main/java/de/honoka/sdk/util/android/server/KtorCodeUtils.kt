package de.honoka.sdk.util.android.server

import cn.hutool.json.JSONUtil
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

object KtorCodeUtils {

    fun getNestedRoutingDefinition(prefix: String, definition: Route.() -> Unit): RoutingDefinition = {
        route(prefix) {
            definition()
        }
    }
}

suspend fun ApplicationCall.respondJson(obj: Any?, httpStatus: HttpStatusCode = HttpStatusCode.OK) {
    if(obj is String) {
        respondText(obj, status = httpStatus)
        return
    }
    respondText(JSONUtil.toJsonStr(obj), ContentType.Application.Json, httpStatus)
}

fun StatusPagesConfig.status(
    status: HttpStatusCode,
    handler: suspend (ApplicationCall, HttpStatusCode) -> Unit
) = status(status) { call, statusParam -> handler(call, statusParam) }

typealias RoutingDefinition = Routing.() -> Unit