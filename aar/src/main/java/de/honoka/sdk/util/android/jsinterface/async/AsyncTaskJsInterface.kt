package de.honoka.sdk.util.android.jsinterface.async

import android.util.Log
import android.webkit.JavascriptInterface
import cn.hutool.core.exceptions.ExceptionUtil
import cn.hutool.core.thread.BlockPolicy
import cn.hutool.core.util.StrUtil
import cn.hutool.json.JSONUtil
import de.honoka.sdk.util.android.common.evaluateJavascriptOnUiThread
import de.honoka.sdk.util.android.common.toMethodArgs
import de.honoka.sdk.util.android.jsinterface.JavascriptInterfaceContainer
import de.honoka.sdk.util.android.ui.AbstractWebActivity
import java.lang.reflect.Method
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class AsyncTaskJsInterface(
    private val jsInterfaceContainer: JavascriptInterfaceContainer,
    private val webActivity: AbstractWebActivity
) {

    private val threadPool = ThreadPoolExecutor(
        5, 30, 60, TimeUnit.SECONDS,
        LinkedBlockingQueue(), BlockPolicy()
    )

    @JavascriptInterface
    fun invokeAsyncMethod(jsInterfaceName: String, methodName: String, callbackId: String, args: String) {
        threadPool.submit {
            val result = AsyncTaskResult()
            try {
                val jsInterface = jsInterfaceContainer.interfaces[jsInterfaceName].also {
                    it ?: throw Exception("Unknown JavaScript interface name: $jsInterfaceName")
                }
                val method: Method = jsInterface!!.javaClass.declaredMethods.run {
                    forEach {
                        if(it.name == methodName) return@run it
                    }
                    throw Exception("Unknown method name \"$methodName\" of interface name: $jsInterfaceName")
                }
                method.getAnnotation(AsyncJavascriptInterface::class.java).also {
                    it ?: throw Exception("Method \"$methodName\" in interface \"$jsInterfaceName\" is not asynchronous")
                }
                result.run {
                    val rawMethodArgs = JSONUtil.parseArray(args)
                    this.result = method.invoke(jsInterface, *rawMethodArgs.toMethodArgs(method))
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
            val script = "window.jsInterfaceAsyncMethodCallbackUtils.invokeCallback('$callbackId', $resultStr)"
            webActivity.webView.evaluateJavascriptOnUiThread(script)
        }
    }
}