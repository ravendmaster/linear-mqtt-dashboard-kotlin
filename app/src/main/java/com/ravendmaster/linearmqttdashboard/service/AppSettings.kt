package com.ravendmaster.linearmqttdashboard.service

import android.content.Context
import android.util.JsonReader
import android.util.Log
import com.ravendmaster.linearmqttdashboard.TabData
import com.ravendmaster.linearmqttdashboard.TabsCollection
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.StringReader

class AppSettings private constructor()
{
    var view_compact_mode = false;
    var view_magnify = 0
    var settingsVersion = 1
    var adfree = true
    var keep_alive = ""
    var server = ""
    var port = ""
    var username = ""
    var password = ""
    var server_topic: String = ""
    var server_mode: Boolean = false
    var push_notifications_subscribe_topic = ""
    var connection_in_background = false
    internal lateinit var dashboards: DashboardsConfiguration
    internal var tabs: TabsCollection? = null

    val tabNames: Array<String?>
        get() {
            val result = arrayOfNulls<String>(tabs!!.items.size)
            var index = 0
            for (tabData in tabs!!.items) {
                result[index++] = tabData.name
            }
            return result
        }

    private var settingsLoaded = false

    private var context: Context? = null


    val settingsAsString: String
        get() {

            val resultJson = JSONObject()

            try {
                resultJson.put("server", server)
                resultJson.put("port", port)
                resultJson.put("username", username)
                resultJson.put("server_topic", server_topic)
                resultJson.put("push_notifications_subscribe_topic", push_notifications_subscribe_topic)
                resultJson.put("keep_alive", keep_alive)
                resultJson.put("connection_in_background", connection_in_background)
                resultJson.put("settingsVersion", settingsVersion)

                resultJson.put("tabs", tabs!!.asJSON)

                resultJson.put("dashboards", dashboards.asJSON)

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            return resultJson.toString()
        }

    fun getDashboardIDByTabIndex(tabIndex: Int): Int {
        return tabs!!.getDashboardIdByTabIndex(tabIndex)
    }

    fun removeTabByDashboardID(id: Int) {
        tabs!!.removeByDashboardID(id)
    }

    fun addTab(tabData: TabData) {
        tabs!!.items.add(tabData)
    }

    fun readFromPrefs(con: Context) {
        context = con
        if (settingsLoaded) return
        settingsLoaded = true

        Log.d(javaClass.name, "readFromPrefs()")

        var sprefs = con.getSharedPreferences("mysettings", Context.MODE_PRIVATE)

        adfree = sprefs.getBoolean("adfree", false)

        view_compact_mode = sprefs.getBoolean("view_compact_mode", false)
        view_magnify = sprefs.getInt("view_magnify", 0)

        server = sprefs.getString("connection_server", "")
        port = sprefs.getString("connection_port", "")
        username = sprefs.getString("connection_username", "")
        password = sprefs.getString("connection_password", "")
        server_topic = sprefs.getString("connection_server_topic", "")
        push_notifications_subscribe_topic = sprefs.getString("connection_push_notifications_subscribe_topic", "")
        keep_alive = sprefs.getString("keep_alive", "60")
        connection_in_background = sprefs.getBoolean("connection_in_background", false)
        server_mode = sprefs.getBoolean("server_mode", false)

        settingsVersion = sprefs.getInt("settingsVersion", 0)

        //settingsVersion=0;

        if (server == "") {
            server = "ssl://m21.cloudmqtt.com"
            port = "26796"
            username = "ejoxlycf"
            password = "odhSFqxSDACF"
            //3.0 subscribe_topic = "out/wcs/#";
            push_notifications_subscribe_topic = "out/wcs/push_notifications/#"
            keep_alive = "60"
            connection_in_background = false
        }

        /* 3.0
        if (subscribe_topic.equals("")) {
            subscribe_topic = "#";
        }
        */


        sprefs = con.getSharedPreferences("mytabs", Context.MODE_PRIVATE)

        tabs = TabsCollection()

        dashboards = DashboardsConfiguration()

        //tabs
        if (settingsVersion == 0) {
            for (i in 0..3) {
                val tabData = TabData()
                tabData.id = i
                tabData.name = sprefs.getString("tab" + (i + 1), "tab #" + i)
                tabs!!.items.add(tabData)
            }
            //dashboards

            sprefs = con.getSharedPreferences("dashboard", Context.MODE_PRIVATE)
            for (i in 0..3) {
                dashboards.put(i, sprefs.getString(getDashboardSystemName(i), ""))
            }

        } else if (settingsVersion == 1) {

            val tabsJSON = sprefs.getString("tabs", "")
            Log.d("tabs", tabsJSON)


            tabs!!.setFromJSONString(tabsJSON)

            sprefs = con.getSharedPreferences("dashboard", Context.MODE_PRIVATE)

            for (tabData in tabs!!.items) {
                val dashboardSysName = getDashboardSystemName(tabData.id)
                dashboards.put(tabData.id, sprefs.getString(dashboardSysName, ""))
            }

        }

    }


    private fun getDashboardSystemName(tabIndex: Int): String {
        return "dashboard" + if (tabIndex == 0) "" else Integer.toString(tabIndex)
    }

    fun saveDashboardSettingsToPrefs(tabIndex: Int, con: Context) {
        Log.d(javaClass.name, "saveDashboardSettingsToPrefs()")
        val sprefs = con.getSharedPreferences("dashboard", Context.MODE_PRIVATE)
        val ed = sprefs.edit()
        ed.putString(getDashboardSystemName(tabIndex), dashboards.get(tabIndex))
        ed.commit()
    }


    fun saveConnectionSettingsToPrefs(con: Context) {
        Log.d(javaClass.name, "saveConnectionSettingsToPrefs()")

        val sprefs = con.getSharedPreferences("mysettings", Context.MODE_PRIVATE)
        val ed = sprefs.edit()
        ed.putBoolean("view_compact_mode", view_compact_mode)
        ed.putInt("view_magnify", view_magnify)

        ed.putBoolean("adfree", adfree!!)
        ed.putString("connection_server", server)
        ed.putString("connection_port", port)
        ed.putString("connection_username", username)
        ed.putString("connection_password", password)
        //3.0 ed.putString("connection_subscribe_topic", subscribe_topic);
        ed.putString("connection_server_topic", server_topic)
        ed.putString("connection_push_notifications_subscribe_topic", push_notifications_subscribe_topic)
        ed.putString("keep_alive", keep_alive)
        ed.putBoolean("connection_in_background", connection_in_background)
        ed.putBoolean("server_mode", server_mode)

        ed.putInt("settingsVersion", settingsVersion)

        if (!ed.commit()) {
            Log.d(javaClass.name, "commit failure!!!")
        }
    }

    fun saveTabsSettingsToPrefs(con: Context?) {
        val sprefs = con!!.getSharedPreferences("mytabs", Context.MODE_PRIVATE)
        val ed = sprefs.edit()

        ed.putString("tabs", tabs!!.asJSON.toString())
        if (!ed.commit()) {
            Log.d(javaClass.name, "commit failure!!!")
        }
    }

    fun setSettingsFromString(text: String) {

        tabs!!.items.clear()
        settingsVersion = 0

        val jsonReader = JsonReader(StringReader(text))
        try {
            jsonReader.beginObject()
            while (jsonReader.hasNext()) {
                val name = jsonReader.nextName()
                when (name) {

                    "server" -> server = jsonReader.nextString()
                    "port" -> port = jsonReader.nextString()
                    "username" -> username = jsonReader.nextString()
                    "password" -> password = jsonReader.nextString()
                    "subscribe_topic" -> {
                        //subscribe_topic = jsonReader.nextString();
                        val trash: String = jsonReader.nextString()
                    }
                    "server_topic" -> server_topic = jsonReader.nextString()
                    "push_notifications_subscribe_topic" -> push_notifications_subscribe_topic = jsonReader.nextString()
                    "keep_alive" -> keep_alive = jsonReader.nextString()
                    "connection_in_background" -> connection_in_background = jsonReader.nextBoolean()
                    "settingsVersion" -> settingsVersion = jsonReader.nextInt()

                    "tab0" -> tabs!!.items.add(TabData(0, jsonReader.nextString()))
                    "tab1" -> tabs!!.items.add(TabData(1, jsonReader.nextString()))
                    "tab2" -> tabs!!.items.add(TabData(2, jsonReader.nextString()))
                    "tab3" -> tabs!!.items.add(TabData(3, jsonReader.nextString()))
                    "dashboard0" -> dashboards.put(0, jsonReader.nextString())
                    "dashboard1" -> dashboards.put(1, jsonReader.nextString())
                    "dashboard2" -> dashboards.put(2, jsonReader.nextString())
                    "dashboard3" -> dashboards.put(3, jsonReader.nextString())

                    "tabs" -> tabs!!.setFromJSON(jsonReader)

                    "dashboards" -> {
                        jsonReader.skipValue()
                        dashboards.setFromJSONRAWString(text)
                    }

                    else -> Log.d("not readed param! ", name)
                }
            }
            jsonReader.endObject()
            jsonReader.close()

        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (settingsVersion == 0) {
            saveTabsSettingsToPrefs(context)
        }
    }


    private object Holder { val INSTANCE = AppSettings() }

    companion object {
        val instance: AppSettings by lazy { Holder.INSTANCE }
    }
}
