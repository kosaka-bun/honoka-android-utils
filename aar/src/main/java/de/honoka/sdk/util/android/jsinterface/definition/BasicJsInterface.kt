package de.honoka.sdk.util.android.jsinterface.definition

import android.content.Intent
import android.webkit.JavascriptInterface
import de.honoka.sdk.util.android.server.HttpServerVariables
import de.honoka.sdk.util.android.ui.AbstractWebActivity

class BasicJsInterface(private val webActivity: AbstractWebActivity) {

    @JavascriptInterface
    fun openNewWebActivity(path: String) {
        webActivity.run {
            startActivity(Intent(this, webActivity.javaClass).apply {
                putExtra("url", HttpServerVariables.getUrlByPath(path))
            })
        }
    }

    @JavascriptInterface
    fun finishCurrentWebActivity() {
        webActivity.finish()
    }
}