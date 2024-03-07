package de.honoka.sdk.util.android.ui

import android.app.Activity
import android.view.View
import android.view.WindowManager

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
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
}