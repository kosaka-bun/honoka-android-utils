package de.honoka.sdk.util.android.jsinterface

import android.annotation.SuppressLint
import de.honoka.sdk.util.android.jsinterface.async.AsyncTaskJsInterface
import de.honoka.sdk.util.android.jsinterface.definition.BasicJsInterface
import de.honoka.sdk.util.android.ui.AbstractWebActivity

@SuppressLint("AddJavascriptInterface")
@Suppress("MemberVisibilityCanBePrivate")
class JavascriptInterfaceContainer(
    val definedInterfaceInstances: List<Any>,
    private val webActivity: AbstractWebActivity
) {

    private val predefinedInterfaceInstances: List<Any> = listOf(
        BasicJsInterface(webActivity),
        AsyncTaskJsInterface(this, webActivity)
    )

    private val interfaceInstances: List<Any> = ArrayList<Any>().apply {
        listOf(
            predefinedInterfaceInstances,
            definedInterfaceInstances
        ).forEach { addAll(it) }
    }

    internal val interfaces: Map<String, Any> = HashMap<String, Any>().also { map ->
        interfaceInstances.forEach {
            map[it.javaClass.simpleName] = it
        }
    }

    init {
        registerJsInterfaces()
    }

    private fun registerJsInterfaces() {
        interfaceInstances.forEach {
            webActivity.webView.addJavascriptInterface(it, "android_${it.javaClass.simpleName}")
        }
    }
}