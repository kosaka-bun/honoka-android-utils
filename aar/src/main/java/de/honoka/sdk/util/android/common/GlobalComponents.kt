package de.honoka.sdk.util.android.common

import android.app.Application
import android.content.Context

object GlobalComponents {

    lateinit var application: Application

    fun initApplicationField(context: Context?) {
        if(::application.isInitialized) return
        application = context!!.applicationContext as Application
    }
}