package com.ravendmaster.linearmqttdashboard

import java.io.IOException

object Log {
    fun d(tag: String, text: String) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(tag, text)
        }
    }

    fun w(tag: String, text: String, e: IOException) {
        if (BuildConfig.DEBUG) {
            android.util.Log.w(tag, text, e)
        }
    }
}
