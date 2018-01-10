package com.ravendmaster.linearmqttdashboard

import android.util.*

import com.ravendmaster.linearmqttdashboard.activity.MainActivity
import com.ravendmaster.linearmqttdashboard.service.Dashboard

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.StringReader
import java.util.ArrayList

class TabsCollection {

    var items = ArrayList<TabData>()
        internal set


    val asJSON: JSONArray
        get() {
            val ar = JSONArray()
            for (tab in items) {
                val resultJson = JSONObject()
                try {
                    resultJson.put("id", tab.id)
                    resultJson.put("name", tab.name)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                ar.put(resultJson)
            }
            return ar
        }

    fun getDashboardIdByTabIndex(index: Int): Int {
        if (index >= items.size) return 0
        val tabData = items[index] ?: return 0
        return tabData.id
    }

    fun removeByDashboardID(dashboardID: Int) {
        for (tabData in items) {
            if (tabData.id == dashboardID) {
                items.remove(tabData)
                return
            }
        }
    }

    fun setFromJSON(jsonReader: JsonReader) {
        items.clear()

        try {
            /*
            String name2 = jsonReader.nextString();
            TabData tabData2 = new TabData();
            tabData2.setId(1);
            tabData2.setName("esp32");
            items.add(tabData2);
*/

            jsonReader.beginArray()
            while (jsonReader.hasNext()) {
                val tabData = TabData()
                jsonReader.beginObject()
                while (jsonReader.hasNext()) {
                    val name = jsonReader.nextName()
                    when (name) {
                        "id" -> tabData.id = jsonReader.nextInt()
                        "name" -> tabData.name = jsonReader.nextString()
                    }
                }
                jsonReader.endObject()
                items.add(tabData)
            }
            jsonReader.endArray()

        } catch (e: Exception) {
            android.util.Log.d("error", e.toString())
        }

    }

    //using in AppSettings.readFromPrefs
    fun setFromJSONString(tabsJSON: String) {
        items.clear()
        val jsonReader = JsonReader(StringReader(tabsJSON))
        try {
            jsonReader.beginArray()
            while (jsonReader.hasNext()) {
                val tabData = TabData()
                jsonReader.beginObject()
                while (jsonReader.hasNext()) {
                    val name = jsonReader.nextName()
                    when (name) {
                        "id" -> tabData.id = jsonReader.nextInt()
                        "name" -> tabData.name = jsonReader.nextString()
                    }
                }
                jsonReader.endObject()
                items.add(tabData)
            }
            jsonReader.endArray()
        } catch (e: Exception) {
            android.util.Log.d("error", e.toString())
        }

    }


}