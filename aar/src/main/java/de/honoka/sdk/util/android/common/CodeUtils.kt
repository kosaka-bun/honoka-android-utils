package de.honoka.sdk.util.android.common

import android.webkit.WebView
import cn.hutool.core.io.FileUtil
import cn.hutool.core.io.IoUtil
import cn.hutool.core.util.ClassUtil
import cn.hutool.json.JSON
import cn.hutool.json.JSONArray
import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.util.concurrent.TimeUnit

private fun launchCoroutine(block: suspend () -> Unit, dispatcher: CoroutineDispatcher): Job {
    return CoroutineScope(dispatcher).launch { block() }
}

fun launchCoroutineOnUiThread(block: suspend () -> Unit): Job = launchCoroutine(block, Dispatchers.Main)

fun launchCoroutineOnIoThread(block: suspend () -> Unit): Job = launchCoroutine(block, Dispatchers.IO)

fun WebView.evaluateJavascriptOnUiThread(script: String, callback: (String) -> Unit = {}) {
    launchCoroutineOnUiThread {
        evaluateJavascript(script, callback)
    }
}

fun runShellCommandForResult(command: String, waitTimeSecends: Int? = null): String {
    Runtime.getRuntime().exec(command).run {
        val processOut = ByteArrayOutputStream()
        if(waitTimeSecends == null) {
            Thread.sleep(100)
            while(isAlive) {
                processOut.write(inputStream.readBytes())
                TimeUnit.SECONDS.sleep(1)
            }
            processOut.write(inputStream.readBytes())
        } else {
            waitFor(waitTimeSecends.toLong(), TimeUnit.SECONDS)
            processOut.write(inputStream.readBytes())
            if(isAlive) destroyForcibly()
        }
        return String(processOut.toByteArray()).trim()
    }
}

fun copyAssetsFileTo(sourceFilePath: String, targetFilePath: String, abortIfTargetExist: Boolean = false) {
    if(File(targetFilePath).exists() && abortIfTargetExist) return
    GlobalComponents.application.assets.open(sourceFilePath).use {
        val outFile = File(targetFilePath)
        if(outFile.exists()) outFile.delete()
        FileUtil.touch(outFile)
        FileOutputStream(outFile).use { out ->
            IoUtil.copy(it, out)
        }
    }
}

/**
 * 将集合中的每一项，根据method定义中的参数类型，转换成指定类型的数据，并封装为数组（可处理带泛型的参数类型）
 */
fun Collection<*>.toMethodArgs(method: Method): Array<Any?> {
    val result = ArrayList<Any?>()
    forEachIndexed { i, arg ->
        val type = method.genericParameterTypes[i]
        val shouldAddDirectly = (
            arg == null || (
                type is Class<*> && (
                    ClassUtil.isBasicType(type) ||
                    arrayOf(
                        JSONObject::class.java,
                        JSONArray::class.java,
                        String::class.java
                    ).contains(type)
                )
            )
        )
        if(shouldAddDirectly) {
            result.add(arg)
            return@forEachIndexed
        }
        val rawType = if(type is Class<*>) type else (type as ParameterizedType).rawType as Class<*>
        val canBeTransfered = Serializable::class.java.isAssignableFrom(rawType) ||
            Collection::class.java.isAssignableFrom(rawType)
        if(canBeTransfered) {
            result.add(JSONUtil.toBean(arg as JSON, type, false))
            return@forEachIndexed
        }
        throw Exception("Unsupported parameter type \"$type\" of $method")
    }
    return result.toTypedArray()
}