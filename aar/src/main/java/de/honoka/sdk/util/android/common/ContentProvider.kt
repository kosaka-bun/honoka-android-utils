package de.honoka.sdk.util.android.common

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import cn.hutool.core.exceptions.ExceptionUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.json.JSON
import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil

abstract class BaseContentProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        GlobalComponents.initApplicationField(context)
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int = 0

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle = Bundle().apply {
        val args = if(StrUtil.isNotBlank(arg)) JSONUtil.parse(arg) else null
        val result = try {
            call(method.ifBlank { null }, args)?.let { if(it !is Unit) it else null }
        } catch(t: Throwable) {
            ExceptionUtil.getRootCause(t).also {
                Log.e(javaClass.simpleName, "", it)
            }
        }
        putString("json", JSONObject().also {
            if(result !is Throwable) {
                it["result"] = result
            } else {
                it["error"] = JSONObject().also { jo ->
                    jo["info"] = ExceptionUtil.getMessage(result)
                    jo["stackTrace"] = ExceptionUtil.stacktraceToString(result)
                }
            }
        }.toString())
    }

    abstract fun call(method: String?, args: JSON?): Any?
}

object ContentProviderUtils {

    inline fun <reified T> getTypedResult(result: Any): T = result.let {
        when {
            T::class.java.isAssignableFrom(it.javaClass) -> it as T
            it is JSON -> it.toBean(T::class.java)
            else -> throw ClassCastException("Cannot cast ${it.javaClass.name} to ${T::class.java.name}")
        }
    }
}

@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter", "unused")
class ContentProviderCallException(val info: String, val stackTraceText: String) : Exception(info)

fun ContentResolver.call(authority: String, method: String? = null, args: Any? = null): Any? {
    val uri = Uri.parse("content://$authority")
    val argsStr = args?.let { JSONUtil.toJsonStr(args) }
    val result = call(uri, method ?: "", argsStr, null)?.let {
        it.getString("json").let { jsonStr ->
            val json = JSONUtil.parseObj(jsonStr)
            json.getJSONObject("error")?.let { error ->
                val stackTrace = error.getStr("stackTrace").apply {
                    Log.e(ContentResolver::class.simpleName, this)
                }
                throw ContentProviderCallException(error.getStr("info"), stackTrace)
            }
            json["result"]
        }
    }
    return result
}

/*
 * 需注意，若实化泛型T中含有嵌套泛型，比如调用该方法时表现为：call<List<Entity>>()，则在代码中只能获取到
 * 泛型T的顶级类型，即List的Class对象。
 */
inline fun <reified T> ContentResolver.typedCall(
    authority: String, method: String? = null, args: Any? = null
): T = call(authority, method, args).let {
    ContentProviderUtils.getTypedResult<T>(it!!)
}

fun contentResolverCall(authority: String, method: String? = null, args: Any? = null): Any? = run {
    GlobalComponents.application.contentResolver.call(authority, method, args)
}

inline fun <reified T> contentResolverTypedCall(
    authority: String, method: String? = null, args: Any? = null
): T = contentResolverCall(authority, method, args).let {
    ContentProviderUtils.getTypedResult<T>(it!!)
}