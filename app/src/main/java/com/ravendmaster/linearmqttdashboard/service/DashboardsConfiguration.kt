package com.ravendmaster.linearmqttdashboard.service

import android.util.SparseArray
import com.ravendmaster.linearmqttdashboard.activity.MainActivity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class DashboardsConfiguration {

    //internal var items = HashMap<Int, String>()
    private var items = SparseArray<String>() //HashMap<Int, String>()

    val asJSON: JSONArray
        get() {
            val dashboards = JSONArray()
            for (tabData in MainActivity.presenter!!.tabs!!.items) {
                val dashboard = JSONObject()
                try {
                    dashboard.put("id", tabData.id.toString())
                    val data = items[tabData.id] ?: continue
                    val o2 = JSONArray(data)
                    dashboard.put("dashboard", o2)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                dashboards.put(dashboard)
            }
            return dashboards
        }

    fun put(name: Int, data: String) {
        items.put(name, data)
    }

    operator fun get(name: Int): String {
        return items[name].toString()
    }

    internal fun setFromJSONRAWString(RawJSON: String) {
        items.clear()

        try {
            val jsonObj = JSONObject(RawJSON)
            val dashboards = jsonObj.getJSONArray("dashboards")
            val dashboardsCount = jsonObj.getJSONArray("dashboards").length()
            for (i in 0 until dashboardsCount) {
                val id: Int?
                var data = ""
                val dashboard = dashboards.getJSONObject(i)
                id = dashboard.getInt("id")
                if (!dashboard.isNull("dashboard"))
                    data = dashboard.getJSONArray("dashboard").toString()
                items.put(id, data)
            }
        } catch (e: Exception) {
            android.util.Log.d("error", e.toString())
        }

    }


}
