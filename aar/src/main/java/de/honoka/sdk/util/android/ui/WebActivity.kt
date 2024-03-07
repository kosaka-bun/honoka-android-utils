package de.honoka.sdk.util.android.ui

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.webkit.WebChromeClient
import android.webkit.WebChromeClient.CustomViewCallback
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import de.honoka.sdk.util.android.R
import de.honoka.sdk.util.android.common.launchCoroutineOnUiThread
import de.honoka.sdk.util.android.jsinterface.JavascriptInterfaceContainer
import de.honoka.sdk.util.android.server.HttpServer
import de.honoka.sdk.util.android.server.HttpServerVariables
import kotlinx.coroutines.delay
import kotlin.system.exitProcess

@SuppressLint("SetJavaScriptEnabled")
@Suppress("MemberVisibilityCanBePrivate")
abstract class AbstractWebActivity : AppCompatActivity() {

    protected lateinit var url: String

    /**
     * 是否是该应用当中第一个被开启的WebActivity
     */
    protected var firstWebActivity: Boolean = false

    lateinit var webView: WebView

    protected val webViewClient = object : WebViewClient() {

        //重写此方法，解决WebView在重定向时打开系统浏览器的问题
        override fun shouldOverrideUrlLoading(
            view: WebView,
            request: WebResourceRequest
        ): Boolean {
            val url = request.url.toString()
            //禁止WebView加载未知协议的URL
            if(!url.startsWith("http")) return true
            view.loadUrl(url)
            return true
        }
    }

    protected val webChromeClient = object : WebChromeClient() {

        override fun onShowCustomView(view: View, callback: CustomViewCallback) {
            fullScreenView?.run {
                callback.onCustomViewHidden()
                return
            }
            setFullScreen(true)
            val decorView = window.decorView as FrameLayout
            decorView.addView(view, fullScreenViewParams)
            fullScreenView = view
            fullScreenViewCallBack = callback
        }

        override fun onHideCustomView() {
            fullScreenView ?: return
            setFullScreen(false)
            val decorView = window.decorView as FrameLayout
            decorView.removeView(fullScreenView)
            fullScreenView = null
            fullScreenViewCallBack?.onCustomViewHidden()
            webView.visibility = View.VISIBLE
        }
    }

    protected lateinit var jsInterfaceContainer: JavascriptInterfaceContainer

    protected var fullScreenView: View? = null

    protected val fullScreenViewParams = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

    protected var fullScreenViewCallBack: CustomViewCallback? = null

    /**
     * 横屏时屏幕的旋转方向（正向或反向）
     */
    protected var screenOrientation: Int = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

    protected val onBackPressedCallback = object : OnBackPressedCallback(true) {

        private var lastTimePressBack = 0L

        override fun handleOnBackPressed() {
            launchCoroutineOnUiThread {
                try {
                    val result = dispatchEventToListenersInWebView("onBackButtonPressedListeners")
                    if(!result) doBack()
                } catch(t: Throwable) {
                    doBack()
                }
            }
        }

        private fun doBack() {
            if(webView.canGoBack()) {
                webView.goBack()
                return
            }
            if(!firstWebActivity) {
                finish()
                return
            }
            if(System.currentTimeMillis() - lastTimePressBack > 2500) {
                Toast.makeText(this@AbstractWebActivity, "再进行一次返回退出应用", Toast.LENGTH_SHORT).show()
                lastTimePressBack = System.currentTimeMillis()
            } else {
                finish()
                if(firstWebActivity) exitProcess(0)
            }
        }
    }

    protected val orientationEventListener by lazy {
        object : OrientationEventListener(this) {

            override fun onOrientationChanged(orientation: Int) {
                val originalOrientation = screenOrientation
                val nowOrientation = when(orientation) {
                    in 45..135 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    in 225..315 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    else -> null
                }
                //屏幕旋转角度未处于应当切换方向的范围，保持原有方向不变
                nowOrientation ?: return
                //同步现有旋转方向到变量中
                screenOrientation = nowOrientation
                //判断全屏View是否存在（即是否处于全屏状态）
                fullScreenView ?: return
                if(originalOrientation == nowOrientation) return
                requestedOrientation = screenOrientation
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //解决状态栏白底白字的问题
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        setContentView(R.layout.activity_web)
        initActivityParams()
        initWebView()
    }

    override fun onPause() {
        dispatchEventToListenersInWebViewDirectly("onActivityPauseListeners")
        super.onPause()
    }

    override fun onResume() {
        HttpServer.checkOrRestartInstance()
        extendedOnResume()
        dispatchEventToListenersInWebViewDirectly("onActivityResumeListeners")
        super.onResume()
    }

    abstract fun extendedOnResume()

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }

    protected fun initActivityParams() {
        url = intent.getStringExtra("url") ?: HttpServerVariables.getUrlByPath("")
        firstWebActivity = intent.getBooleanExtra("firstWebActivity", false)
    }

    protected fun initWebView() {
        webView = findViewById(R.id.web_view)
        webView.run {
            webViewClient = this@AbstractWebActivity.webViewClient
            webChromeClient = this@AbstractWebActivity.webChromeClient
            settings.run {
                //必须打开，否则网页可能显示为空白
                javaScriptEnabled = true
            }
            isVerticalScrollBarEnabled = false
            scrollBarStyle = View.SCROLLBARS_OUTSIDE_OVERLAY
            jsInterfaceContainer = newJavascriptInterfaceContainer()
            loadUrl(this@AbstractWebActivity.url)
        }
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
        orientationEventListener.enable()
    }

    abstract fun newJavascriptInterfaceContainer(): JavascriptInterfaceContainer

    protected fun setFullScreen(fullScreen: Boolean) {
        if(fullScreen) {
            requestedOrientation = screenOrientation
            showStatusBar(false)
            return
        }
        showStatusBar()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    @Suppress("DEPRECATION")
    protected fun showStatusBar(show: Boolean = true) {
        val flag = if(show) 0 else WindowManager.LayoutParams.FLAG_FULLSCREEN
        window.setFlags(flag, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    //返回true表示有监听器的预定义行为被触发
    protected suspend fun dispatchEventToListenersInWebView(listenerName: String): Boolean {
        val script = "window.androidEventListenerUtils.invokeListeners('$listenerName')"
        var result: String? = null
        webView.evaluateJavascript(script) {
            result = it
        }
        while(true) {
            if(result != null) break
            delay(1)
        }
        return result.toBoolean()
    }

    protected fun dispatchEventToListenersInWebViewDirectly(listenerName: String) {
        launchCoroutineOnUiThread { dispatchEventToListenersInWebView(listenerName) }
    }

    fun simulateClick(positionX: Float, positionY: Float) {
        fun obtainEvent(action: Int) = MotionEvent.obtain(
            SystemClock.uptimeMillis(),
            SystemClock.uptimeMillis(),
            action,
            positionX,
            positionY,
            0
        )
        obtainEvent(MotionEvent.ACTION_DOWN).run {
            webView.dispatchTouchEvent(this)
            recycle()
        }
        obtainEvent(MotionEvent.ACTION_UP).run {
            webView.dispatchTouchEvent(this)
            recycle()
        }
    }
}

class DefaultWebActivity : AbstractWebActivity() {

    override fun extendedOnResume() {}

    override fun newJavascriptInterfaceContainer(): JavascriptInterfaceContainer = run {
        JavascriptInterfaceContainer(listOf(), webView)
    }
}