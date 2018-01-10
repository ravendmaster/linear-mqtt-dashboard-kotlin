package com.ravendmaster.linearmqttdashboard.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast

import com.ravendmaster.linearmqttdashboard.service.AppSettings
import com.ravendmaster.linearmqttdashboard.R
import android.widget.SeekBar
import com.google.firebase.analytics.FirebaseAnalytics


class AppSettingsActivity : AppCompatActivity() {

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    internal lateinit var view_compact_mode: CheckBox
    internal lateinit var view_magnify : SeekBar//  = findViewById(R.id.seekBar1) as SeekBar

    internal lateinit var server: EditText
    internal lateinit var port: EditText
    internal lateinit var username: EditText
    internal lateinit var password: EditText
    internal lateinit var server_topic: EditText
    internal lateinit var push_notifications_subscribe_topic: EditText
    //CheckBox notifications_service;
    internal lateinit var connection_in_background: CheckBox
    internal lateinit var server_mode: CheckBox

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_editor, menu)
        return super.onCreateOptionsMenu(menu)
    }

    fun validateUrl(adress: String): Boolean {
        return if (adress.endsWith(".xyz")) true else Patterns.DOMAIN_NAME.matcher(adress).matches()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.Save -> {
                if(server.text.toString().isBlank()){
                //if (!validateUrl(server.text.toString())) {
                    Toast.makeText(getApplicationContext(), "Server address is incorrect!", Toast.LENGTH_SHORT).show();
                    return false;
                }
                val settings = AppSettings.instance

                if(settings.view_compact_mode != view_compact_mode.isChecked
                        || settings.view_magnify != view_magnify.progress){
                    val params = Bundle()
                    params.putString("view_compact_mode", view_compact_mode.isChecked.toString());
                    params.putString("view_magnify", view_magnify.progress.toString());
                    mFirebaseAnalytics.logEvent("view_mode_changed", params);
                }

                settings.view_compact_mode = view_compact_mode.isChecked
                settings.view_magnify = view_magnify.progress

                settings.server = server.text.toString()
                settings.port = port.text.toString()
                settings.username = username.text.toString()
                settings.password = password.text.toString()
                settings.server_topic = server_topic.text.toString()
                settings.push_notifications_subscribe_topic = push_notifications_subscribe_topic.text.toString()
                settings.connection_in_background = connection_in_background.isChecked
                settings.server_mode = server_mode.isChecked

                settings.saveConnectionSettingsToPrefs(this)

                //MainActivity.presenter.restartService(this);
                if (MainActivity.presenter != null) {
                    MainActivity.presenter!!.connectionSettingsChanged()
                }

                finish()
                //MainActivity.connectToMQTTServer(getApplicationContext());
                MainActivity.presenter!!.resetCurrentSessionTopicList()

                MainActivity.presenter!!.subscribeToAllTopicsInDashboards(settings)




                MainActivity.instance.restartMainActivity();


            }
        }
        return true

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_settings)

        view_magnify = findViewById<View>(R.id.seekBarMagnify) as SeekBar
        view_compact_mode = findViewById<View>(R.id.checkBox_compact_mode) as CheckBox
        server = findViewById<View>(R.id.editText_server) as EditText
        port = findViewById<View>(R.id.editText_port) as EditText
        username = findViewById<View>(R.id.editText_username) as EditText
        password = findViewById<View>(R.id.editText_password) as EditText
        server_topic = findViewById<View>(R.id.editText_server_topic) as EditText
        push_notifications_subscribe_topic = findViewById<View>(R.id.editText_push_notifications_subscribe_topic) as EditText
        //notifications_service = (CheckBox) findViewById(R.id.checkBox_start_notifications_service);
        connection_in_background = findViewById<View>(R.id.checkBox_connection_in_background) as CheckBox
        server_mode = findViewById<View>(R.id.checkBox_server_mode) as CheckBox


        val settings = AppSettings.instance
        view_compact_mode.isChecked = settings.view_compact_mode
        view_magnify.progress = settings.view_magnify

        server.setText(settings.server)
        port.setText(settings.port)
        username.setText(settings.username)
        password.setText(settings.password)
        server_topic.setText(settings.server_topic)
        push_notifications_subscribe_topic.setText(settings.push_notifications_subscribe_topic)
        connection_in_background.isChecked = settings.connection_in_background
        server_mode.isChecked = settings.server_mode

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

    }

    fun OnClickHelp(view: View) {
        MainActivity.presenter!!.OnClickHelp(this, view)
    }

}
