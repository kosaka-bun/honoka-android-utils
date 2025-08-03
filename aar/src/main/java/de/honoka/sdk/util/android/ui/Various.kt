package de.honoka.sdk.util.android.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.widget.Toast
import de.honoka.sdk.util.android.basic.launchOnUi
import de.honoka.sdk.util.android.server.HttpServerVariables
import de.honoka.sdk.util.kotlin.text.toJsonString
import de.honoka.sdk.util.kotlin.text.toJsonWrapper
import org.intellij.lang.annotations.Language
import kotlin.reflect.KClass

private const val ACTIVITY_DEFAULT_EXTRAS_NAME = "defaultExtras"

fun Context.toast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

fun Activity.startActivity(clazz: KClass<out Activity>, extras: Any? = null) {
    val intent = Intent(this, clazz.java)
    extras?.let {
        intent.putExtra(ACTIVITY_DEFAULT_EXTRAS_NAME, it.toJsonString())
    }
    startActivity(intent)
}

fun Activity.startRootWebActivty(
    webActivityClass: KClass<out AbstractWebActivity> = DefaultWebActivity::class,
    url: String = HttpServerVariables.getUrlByPath("/")
) {
    runOnUiThread {
        startActivity(webActivityClass, WebActivityExtras(url, true))
        finish()
    }
}

fun <T : Any> Activity.getDefaultExtras(clazz: KClass<T>): T? = intent.run {
    getStringExtra(ACTIVITY_DEFAULT_EXTRAS_NAME)?.toJsonWrapper()?.toBean(clazz)
}

/**
 * 全屏化当前Activity
 */
@Suppress("DEPRECATION")
fun Activity.fullScreenToShow() {
    //隐藏状态栏（手机时间、电量等信息显示的地方）
    window.setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN
    )
    //隐藏虚拟按键
    window.decorView.systemUiVisibility = run {
        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
    }
}

fun WebView.evaluateJsOnUi(
    @Language("JavaScript") script: String,
    callback: (String) -> Unit = {}
) {
    launchOnUi {
        evaluateJavascript(script, callback)
    }
}
