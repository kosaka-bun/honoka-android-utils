package de.honoka.sdk.util.android.jsinterface

import android.webkit.JavascriptInterface
import de.honoka.sdk.util.android.server.HttpServerVariables
import de.honoka.sdk.util.android.ui.AbstractWebActivity
import de.honoka.sdk.util.android.ui.WebActivityExtras
import de.honoka.sdk.util.android.ui.startActivity

internal class BasicJsInterface(private val webActivity: AbstractWebActivity) {

    @JavascriptInterface
    fun openNewWebActivity(path: String) {
        val extras = WebActivityExtras(
            HttpServerVariables.getUrlByPath(path),
            true
        )
        webActivity.startActivity(webActivity::class, extras)
    }

    @JavascriptInterface
    fun finishCurrentWebActivity() {
        webActivity.finish()
    }
}
