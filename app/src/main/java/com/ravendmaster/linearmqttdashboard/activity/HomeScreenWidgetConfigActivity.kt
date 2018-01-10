package com.ravendmaster.linearmqttdashboard.activity

import android.app.Activity
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup

import com.ravendmaster.linearmqttdashboard.HomeScreenWidget

import com.ravendmaster.linearmqttdashboard.R

class HomeScreenWidgetConfigActivity : Activity() {

    internal var widgetID = AppWidgetManager.INVALID_APPWIDGET_ID
    internal lateinit var resultValue: Intent

    internal val LOG_TAG = "myLogs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(LOG_TAG, "onCreate config")

        // извлекаем ID конфигурируемого виджета
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID)
        }
        // и проверяем его корректность
        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
        }

        // формируем intent ответа
        resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)

        // отрицательный ответ
        setResult(Activity.RESULT_CANCELED, resultValue)

        setContentView(R.layout.config)
    }


    fun onClick(v: View) {
        val selRBColor = (findViewById<View>(R.id.rgColor) as RadioGroup)
                .checkedRadioButtonId
        var color = Color.RED
        when (selRBColor) {
            R.id.radioRed -> color = Color.parseColor("#66ff0000")
            R.id.radioGreen -> color = Color.parseColor("#6600ff00")
            R.id.radioBlue -> color = Color.parseColor("#660000ff")
        }
        val etText = findViewById<View>(R.id.etText) as EditText

        // Записываем значения с экрана в Preferences
        val sp = getSharedPreferences(WIDGET_PREF, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putString(WIDGET_TEXT + widgetID, etText.text
                .toString())
        editor.putInt(WIDGET_COLOR + widgetID, color)
        editor.commit()

        val appWidgetManager = AppWidgetManager.getInstance(this)
        HomeScreenWidget.updateWidget(this, appWidgetManager, sp, widgetID)

        // положительный ответ
        setResult(Activity.RESULT_OK, resultValue)

        Log.d(LOG_TAG, "finish config " + widgetID)
        finish()
    }

    companion object {

        val WIDGET_PREF = "widget_pref"
        val WIDGET_TEXT = "widget_text_"
        val WIDGET_COLOR = "widget_color_"
    }


}