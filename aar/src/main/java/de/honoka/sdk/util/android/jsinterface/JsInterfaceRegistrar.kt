package de.honoka.sdk.util.android.jsinterface

import android.annotation.SuppressLint
import de.honoka.sdk.util.android.ui.AbstractWebActivity

@SuppressLint("AddJavascriptInterface")
@Suppress("MemberVisibilityCanBePrivate")
internal class JsInterfaceRegistrar(
    private val webActivity: AbstractWebActivity, definedInterfaceInstances: List<Any>
) {

    private val interfaceInstances = listOf(
        BasicJsInterface(webActivity),
        AsyncTaskJsInterface(this, webActivity),
        *definedInterfaceInstances.toTypedArray()
    )

    internal val interfaces = interfaceInstances.associateBy { it::class.qualifiedName!! }

    init {
        registerJsInterfaces()
    }

    @SuppressLint("JavascriptInterface")
    private fun registerJsInterfaces() {
        interfaceInstances.forEach {
            webActivity.webView.addJavascriptInterface(
                it, "android_${it::class.qualifiedName}"
            )
        }
    }
}
