package de.honoka.sdk.util.android.jsinterface

import android.util.Log
import android.webkit.JavascriptInterface
import cn.hutool.core.exceptions.ExceptionUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.json.JSONUtil
import de.honoka.sdk.util.android.basic.toFunctionArgs
import de.honoka.sdk.util.android.ui.AbstractWebActivity
import de.honoka.sdk.util.android.ui.evaluateJsOnUi
import de.honoka.sdk.util.concurrent.ThreadPoolUtils
import de.honoka.sdk.util.kotlin.basic.exception
import de.honoka.sdk.util.kotlin.text.singleLine
import java.util.concurrent.TimeUnit
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.hasAnnotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class AsyncJavascriptInterface

internal class AsyncTaskJsInterface(
    private val jsInterfaceContainer: JsInterfaceRegistrar,
    private val webActivity: AbstractWebActivity
) {

    private val threadPool = ThreadPoolUtils.newEagerThreadPool(
        5, 30, 60, TimeUnit.SECONDS
    )

    @Suppress("JSUnresolvedReference")
    @JavascriptInterface
    fun invokeAsyncMethod(jsInterfaceName: String, functionName: String, callbackId: String, args: String) {
        threadPool.submit {
            val result = AsyncTaskResult()
            try {
                val jsInterface = jsInterfaceContainer.interfaces[jsInterfaceName].also {
                    it ?: exception("Unknown JavaScript interface name: $jsInterfaceName")
                }
                val function = jsInterface!!::class.declaredMemberFunctions.firstOrNull {
                    it.name == functionName && it.hasAnnotation<AsyncJavascriptInterface>()
                } ?: exception(
                    """
                        The interface [$jsInterfaceName] has no function with name [$functionName] |
                        or the function is not annotated by @AsyncJavascriptInterface.
                    """.singleLine()
                )
                result.run {
                    val rawMethodArgs = JSONUtil.parseArray(args)
                    this.result = function.call(
                        jsInterface, *rawMethodArgs.toFunctionArgs(function)
                    )
                    isResolve = true
                }
            } catch(t: Throwable) {
                val throwable = ExceptionUtil.getRootCause(t)
                Log.e(javaClass.simpleName, "", throwable)
                result.run {
                    isResolve = false
                    message = throwable.message.let {
                        if(StrUtil.isBlank(it)) throwable.javaClass.simpleName else it
                    }
                }
            }
            val resultStr = JSONUtil.toJsonStr(result)
            val script = "window.jsInterfaceAsyncMethodCallbackUtils.invokeCallback(" +
                "'$callbackId', $resultStr)"
            webActivity.webView.evaluateJsOnUi(script)
        }
    }
}

internal data class AsyncTaskResult(

    var isResolve: Boolean? = null,

    var message: String? = null,

    var result: Any? = null
)
