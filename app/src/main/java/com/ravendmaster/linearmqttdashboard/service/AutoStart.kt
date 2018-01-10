package com.ravendmaster.linearmqttdashboard.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class AutoStart : BroadcastReceiver() {
    override fun onReceive(context: Context, arg1: Intent) {
        val intent = Intent(context, MQTTService::class.java)
        intent.action = "autostart"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }

        Log.d(javaClass.name, "started")
    }
}