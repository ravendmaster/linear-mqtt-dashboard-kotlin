package com.ravendmaster.linearmqttdashboard.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.util.JsonReader

import com.ravendmaster.linearmqttdashboard.Utilities
import com.ravendmaster.linearmqttdashboard.customview.Graph
import com.ravendmaster.linearmqttdashboard.database.DbHelper
import com.ravendmaster.linearmqttdashboard.database.HistoryCollector
import com.ravendmaster.linearmqttdashboard.Log
import com.ravendmaster.linearmqttdashboard.activity.MainActivity
import com.ravendmaster.linearmqttdashboard.R
import com.squareup.duktape.Duktape

import org.fusesource.hawtbuf.Buffer
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.StringReader
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Collections
import java.util.Date
import java.util.GregorianCalendar
import java.util.HashMap
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


class MQTTService : Service(), CallbackMQTTClient.IMQTTMessageReceiver {
    private var currentConnectionState: Int = 0

    private var push_topic: String? = null

    private var currentDataVersion = 0

    internal var callbackMQTTClient: CallbackMQTTClient? = null

    var dashboards: ArrayList<Dashboard>? = null

    val freeDashboardId: Int
        get() {
            val result = dashboards!!
                    .map { it.id }
                    .max()
                    ?: -1
            return result + 1
        }

    private val lastReceivedMessagesByTopic: HashMap<String, String>

    var currentMQTTValues: HashMap<String, String> = HashMap()

    var activeTabIndex = 0
    var screenActiveTabIndex = 0

    private val duktape: Duktape

    private var contextWidgetData: WidgetData? = null

    private var imqtt: IMQTT = object : IMQTT {
        override fun read(topic: String): String {
            return getMQTTCurrentValue(topic)
        }

        override fun publish(topic: String, payload: String) {
            publishMQTTMessage(topic, Buffer(payload.toByteArray()), false)
        }

        override fun publishr(topic: String, payload: String) {
            publishMQTTMessage(topic, Buffer(payload.toByteArray()), true)
        }
    }

    private var notifier: INotifier = object : INotifier {
        override fun push(message: String) {
            publishMQTTMessage(getServerPushNotificationTopicForTextMessage(contextWidgetData!!.uid.toString()), Buffer(message.toByteArray()), true)
        }

        override fun stop() {
            publishMQTTMessage(getServerPushNotificationTopicForTextMessage(contextWidgetData!!.uid.toString()), Buffer("".toByteArray()), true)
        }
    }

    //корень топиков от сервера приложения
    val serverPushNotificationTopicRootPath: String
        get() {
            val rootPushTopic = AppSettings.instance.push_notifications_subscribe_topic
            return rootPushTopic.replace("#", "") + "\$server"
        }

    private val topicsForHistoryCollect: HashMap<String, String>
        get() {
            val graph_topics = HashMap<String, String>()
            for (dashboard in dashboards!!) {
                for (widgetData in dashboard.widgetsList) {
                    if (widgetData.type != WidgetData.WidgetTypes.GRAPH) continue
                    for (i in 0..3) {
                        val topic = widgetData.getSubTopic(i)
                        if (!topic.isEmpty() && widgetData.mode >= Graph.PERIOD_TYPE_1_HOUR) {
                            graph_topics.put(topic, topic)
                        }
                    }
                }
            }
            return graph_topics
        }

    private val topicsForLiveCollect: HashMap<String, String>
        get() {
            val graphTopics = HashMap<String, String>()
            for (dashboard in dashboards!!) {
                for (widgetData in dashboard.widgetsList) {
                    if (widgetData.type != WidgetData.WidgetTypes.GRAPH) continue
                    for (i in 0..3) {
                        val topic = widgetData.getSubTopic(i)
                        if (!topic.isEmpty() && widgetData.mode == Graph.LIVE) {
                            graphTopics.put(topic, topic)
                        }
                    }
                }
            }
            return graphTopics
        }

    private var topicsForHistory: HashMap<String, String>? = null
    private var topicsForLive: HashMap<String, String>? = null
    var SERVER_DATAPACK_NAME = ""

    private var historyCollector: HistoryCollector? = null


    private val allInteractiveTopics: ArrayList<String>
        get() {
            val result = ArrayList<String>()

            createDashboardsBySettings() //!!!

            for (dashboard in dashboards!!) {
                for (widgetData in dashboard.widgetsList) {

                    for (i in 0..3) {
                        var topic = widgetData.getSubTopic(i)


                        if (!topic.isEmpty()) {


                            if (result.indexOf(topic) == -1 && topic[0]!='%') {
                                result.add(topic)
                            }
                        }
                        /*
                        topic += '$'
                        if (!topic.isEmpty()) {
                            if (result.indexOf(topic) == -1) {
                                result.add(topic)
                            }
                        }
                        */

                    }
                }
            }

            return result
        }

    val isConnected: Boolean
        get() = if (callbackMQTTClient == null) false else callbackMQTTClient!!.isConnected

    private var mPayloadChanged: Handler? = null

    var currentSessionTopicList = ArrayList<String>()

    fun getDashboardByID(id: Int): Dashboard? {
        dashboards!!
                .filter { it.id == id }
                .forEach { return it }
        Exception("can't find dashboard by ID")
        return null
    }

    fun getMQTTCurrentValue(topic: String): String {
        //if (currentMQTTValues == null) return ""

        val value = currentMQTTValues[topic]

        return value ?: ""
    }

    fun OnCreate(appCompatActivity: AppCompatActivity) {
        super.onCreate()

        val context = applicationContext
        val appSettings = AppSettings.instance
        appSettings.readFromPrefs(context)
        createDashboardsBySettings()

        activeTabIndex = appSettings.tabs!!.getDashboardIdByTabIndex(screenActiveTabIndex)



        if (Build.VERSION.SDK_INT >= 26) {
            val foregroundIntent = Intent(this, MainActivity::class.java)
            foregroundIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            foregroundIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val pendingIntent = PendingIntent.getActivity(this, 0, foregroundIntent, 0)
            val builder = Notification.Builder(this)
                    //.setContentTitle("Application server mode is on")
                    //.setSmallIcon(R.drawable.ic_playblack)
                    .setContentIntent(pendingIntent)
                    //.setOngoing(true).setSubText(text1)
                    .setAutoCancel(true)
                startForeground(1, builder.build())
        }

    }

    fun evalJS(contextWidgetData: WidgetData, value: String?, code: String): String? {
        this.contextWidgetData = contextWidgetData
        var result = value
        try {
            result = duktape.evaluate("var value='$value'; $code; String(value);")
            processReceiveSimplyTopicPayloadData("%onJSErrors", "no errors");
        } catch (e: Exception) {
            Log.d("script", "exec: " + e)
            processReceiveSimplyTopicPayloadData("%onJSErrors", e.toString() );
        }

        return result
    }

    internal interface IMQTT {
        fun read(topic: String): String

        fun publish(topic: String, payload: String)

        fun publishr(topic: String, payload: String)
    }

    internal interface INotifier {
        fun push(message: String)

        fun stop()
    }

    fun getServerPushNotificationTopicForTextMessage(id: String): String { //для текстовых сообщений
        return serverPushNotificationTopicRootPath + "/message" + Integer.toHexString(id.hashCode())
    }

    fun createDashboardsBySettings(forceReload : Boolean = false) {

        if(!forceReload && dashboards!=null)return

        Log.d(javaClass.name, "createDashboardsBySettings()")

        dashboards = ArrayList()

        if (AppSettings.instance.settingsVersion == 0) {
            //старый способ
            val tabs = AppSettings.instance.tabs
            for (i in 0..3) {
                if (tabs!!.items.size <= i) break
                val tabData = tabs.items[i]
                if (tabData == null || tabData.name == "") {
                    continue
                }
                val tempDashboard = Dashboard(i)
                tempDashboard.loadDashboard()
                dashboards!!.add(tempDashboard)
            }
        } else {
            for (tabData in AppSettings.instance.tabs!!.items) {
                val tempDashboard = Dashboard(tabData.id)
                tempDashboard.loadDashboard()
                dashboards!!.add(tempDashboard)

            }

        }

    }

    init {
        Log.d(javaClass.name, "constructor MQTTService()")

        instance = this

        duktape = Duktape.create()
        duktape.bind("MQTT", IMQTT::class.java, imqtt)
        duktape.bind("Notifier", INotifier::class.java, notifier)
        Log.d(javaClass.name, "duktape start")


        lastReceivedMessagesByTopic = HashMap()

        currentMQTTValues = HashMap()

        currentConnectionState = STATE_DISCONNECTED
        Thread(object : Runnable {
            override fun run() {
                while (true) {
                    if (connectionInUnActualMode) {
                        connectionInUnActualMode = false
                        if (isConnected) callbackMQTTClient!!.disconnect()
                        while (isConnected) {
                            try {
                                Thread.sleep(100)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }

                        }

                        val appSettings = AppSettings.instance
                        appSettings.readFromPrefs(applicationContext)
                        callbackMQTTClient!!.disconnect()//!!!!! предыдущие соединения тоже соединяться с новыми параметры, поэтому отключаем их силой
                        callbackMQTTClient!!.connect(appSettings)
                        subscribeForState(STATE_FULL_CONNECTED)
                    }


                    if (!inRealForegroundMode && MQTTService.clientCountsInForeground > 0) {

                        val appSettings = AppSettings.instance

                        appSettings.readFromPrefs(applicationContext)
                        if (isConnected) {
                            subscribeForState(STATE_FULL_CONNECTED)
                        } else {
                            callbackMQTTClient!!.disconnect()//предыдущие соединения тоже соединятся с новыми параметры, поэтому отключаем их принудительно
                            callbackMQTTClient!!.connect(appSettings)

                            while (!isConnected) {
                                try {
                                    Thread.sleep(100)
                                } catch (e: InterruptedException) {
                                    e.printStackTrace()
                                }

                                if (connectionInUnActualMode) {
                                    break
                                }
                            }
                            if (connectionInUnActualMode) continue

                            subscribeForState(STATE_FULL_CONNECTED)

                        }

                        inRealForegroundMode = true

                    }

                    if (MQTTService.clientCountsInForeground == 0) {
                        idleTime += 1
                    } else {
                        idleTime = 0
                    }

                    try {
                        Thread.sleep(100)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    if (inRealForegroundMode and (idleTime > 100)) { //100*100 = 10 sec
                        val appSettings = AppSettings.instance
                        appSettings.readFromPrefs(applicationContext)

                        if (!appSettings.server_mode) {
                            Log.d(javaClass.name, "Go to the background.")
                            if (appSettings.connection_in_background && !appSettings.push_notifications_subscribe_topic.isEmpty()) {
                                if (!isConnected) {
                                    callbackMQTTClient!!.connect(appSettings)
                                } else {
                                    subscribeForState(STATE_HALF_CONNECTED)
                                }
                            } else {
                                callbackMQTTClient!!.disconnect()
                            }
                            inRealForegroundMode = false

                        } else {

                        }
                    }

                }

            }
        }).start()


        Thread(Runnable {
            //история данных

            try {
                Thread.sleep(5000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            val appSettings = AppSettings.instance

            while (true) {

                if (appSettings.server_mode && db != null && dashboards != null) {

                    topicsForHistory = topicsForHistoryCollect

                    //аггрегация данных для графиков
                    val universalPackJson = JSONObject()
                    val topicsData = JSONArray()
                    try {
                        val strEnum = Collections.enumeration(topicsForHistory!!.keys)
                        while (strEnum.hasMoreElements()) {
                            val topicForHistoryData = strEnum.nextElement()//widgetData.getSubTopic(0).substring(0, widgetData.getSubTopic(0).length() - 4);
                            val historyData = prepareHistoryGraphicData(topicForHistoryData, intArrayOf(Graph.PERIOD_TYPE_1_HOUR, Graph.PERIOD_TYPE_4_HOUR, Graph.PERIOD_TYPE_1_DAY, Graph.PERIOD_TYPE_1_WEEK, Graph.PERIOD_TYPE_1_MOUNT))
                            val oneTopicData = JSONObject()
                            oneTopicData.put("topic", topicForHistoryData + Graph.HISTORY_TOPIC_SUFFIX)
                            oneTopicData.put("payload", historyData)
                            topicsData.put(oneTopicData)
                            Log.d("servermode", "source len:" + historyData.length)
                        }

                        universalPackJson.put("ver", 1)
                        universalPackJson.put("type", TOPICS_DATA)
                        universalPackJson.put("data", topicsData.toString())

                        val universalPackJsonResult = universalPackJson.toString()

                        //сжимаем
                        val bo = ByteArrayOutputStream()
                        val os = ZipOutputStream(BufferedOutputStream(bo))
                        try {
                            os.putNextEntry(ZipEntry("data"))
                            val buff = Utilities.stringToBytesUTFCustom(universalPackJsonResult)
                            os.flush()
                            os.write(buff)
                            os.close()
                            //os.closeEntry();
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                        publishMQTTMessage(SERVER_DATAPACK_NAME, Buffer(bo.toByteArray()), true)

                        Log.d("servermode", "universal data source len:" + universalPackJsonResult.length + " zipped len:" + bo.toByteArray().size)


                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }

                try {
                    Thread.sleep((60 * 1000).toLong())

                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
        }).start()


        Thread(Runnable {
            //живые данные

            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            val appSettings = AppSettings.instance


            while (true) {

                if ((clientCountsInForeground > 0 || appSettings.server_mode) && db != null && dashboards != null && historyCollector!=null) {

                    topicsForLive = topicsForLiveCollect

                    val strEnum = Collections.enumeration(topicsForLive!!.keys)
                    while (strEnum.hasMoreElements()) {
                        val topicForHistoryData = strEnum.nextElement()//widgetData.getSubTopic(0).substring(0, widgetData.getSubTopic(0).length() - 4);
                        val historyData = prepareHistoryGraphicData(topicForHistoryData, intArrayOf(Graph.LIVE))
                        processReceiveSimplyTopicPayloadData(topicForHistoryData + Graph.LIVE_TOPIC_SUFFIX, historyData)
                    }
                }

                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
        }).start()

        //$timer_1m
        Thread(Runnable {
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            while (true) {
                val cal=Calendar.getInstance();
                val dateFormat = SimpleDateFormat("HH:mm")
                processReceiveSimplyTopicPayloadData("%onTimer1m()", dateFormat.format(cal.getTime()));
                try {
                    Thread.sleep(60000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }).start()


        //$timer_1s
        Thread(Runnable {
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            while (true) {
                val cal=Calendar.getInstance();
                val dateFormat = SimpleDateFormat("HH:mm:ss")
                processReceiveSimplyTopicPayloadData("%onTimer1s()", dateFormat.format(cal.getTime()));
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }).start()

    }


    private fun prepareHistoryGraphicData(sourceTopic: String, period_types: IntArray): String {
        //добыча id топика
        val topicId = historyCollector!!.getTopicIDByName(sourceTopic)

        //аггрегация
        val resultJson = JSONObject()
        val graphics = JSONArray()

        for (period_type in period_types) {

            val aggregationPeriod = Graph.aggregationPeriod[period_type].toLong()
            val periodsCount = Graph.getPeriodCount(period_type)

            val period = aggregationPeriod * periodsCount
            val mass = arrayOfNulls<Float>(periodsCount + 1)

            val now = Date()
            var timeNow = now.time

            val c = GregorianCalendar()
            when (period_type) {
                Graph.PERIOD_TYPE_4_HOUR -> {
                    c.add(Calendar.MINUTE, 30)

                    c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), if (c.get(Calendar.MINUTE) < 30) 0 else 30, 0)
                    timeNow += c.time.time - timeNow
                }
                Graph.PERIOD_TYPE_1_DAY -> {
                    c.add(Calendar.HOUR_OF_DAY, 1)
                    c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), 0, 0)
                    timeNow += c.time.time - timeNow
                }
                Graph.PERIOD_TYPE_1_WEEK, Graph.PERIOD_TYPE_1_MOUNT -> {
                    c.add(Calendar.DAY_OF_YEAR, 1)
                    c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                    timeNow += c.time.time - timeNow
                }
            }

            val nowRaw = (timeNow / aggregationPeriod) * aggregationPeriod
            val timeLine = nowRaw - period

            //данные за период
            var selectQuery = "SELECT  MAX(timestamp/?), COUNT(timestamp), ROUND(AVG(value),2) FROM HISTORY WHERE detail_level=0 AND topic_id=? AND timestamp>=? GROUP BY timestamp/? ORDER BY timestamp DESC"
            var cursor = db!!.rawQuery(selectQuery, arrayOf(aggregationPeriod.toString(), topicId.toString(), timeLine.toString(), aggregationPeriod.toString()))
            if (cursor.moveToFirst()) {
                var i = 1
                do {
                    val res = cursor.getFloat(2)
                    val index = (nowRaw / aggregationPeriod - cursor.getLong(0)).toInt()
                    if (index in 0..periodsCount) {
                        mass[index] = res
                    }
                    i++
                } while (cursor.moveToNext())
            }
            cursor.close()


            //актуальное значение
            selectQuery = "SELECT ROUND(value,2) FROM HISTORY WHERE detail_level=0 AND topic_id=? ORDER BY timestamp DESC LIMIT 1"
            cursor = db!!.rawQuery(selectQuery, arrayOf(topicId.toString()))
            var actualValue: Float? = null
            if (cursor.moveToFirst()) {
                actualValue = cursor.getFloat(0)
            }
            cursor.close()

            if (period_type <= Graph.PERIOD_TYPE_1_HOUR && actualValue != null) { //нужно только для живого и часового графиков, более крупным - нет

                (0 until periodsCount)
                        .takeWhile { mass[it] == null }
                        .forEach { mass[it] = actualValue }
                //заполняем пропуски
                var lastVal: Float? = null
                for (i in periodsCount - 1 downTo 0) {
                    if (mass[i] == null) {
                        mass[i] = lastVal
                    } else {
                        lastVal = mass[i]
                    }
                }


            }


            val dots = JSONArray()
            for (i in 0 until periodsCount) {
                dots.put(if (mass[i] == null) "" else mass[i])
            }

            val graphLineJSON = JSONObject()
            try {
                graphLineJSON.put("period_type", period_type)
                graphLineJSON.put("actual_timestamp", timeNow)
                graphLineJSON.put("aggregation_period", aggregationPeriod)
                graphLineJSON.put("dots", dots)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            graphics.put(graphLineJSON)
        }

        try {
            resultJson.put("type", "graph_history")
            resultJson.put("graphics", graphics)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return resultJson.toString()

    }

    fun connectionSettingsChanged() {
        connectionInUnActualMode = true
    }

    fun subscribeForInteractiveMode(appSettings: AppSettings) {
        callbackMQTTClient!!.subscribeMass(allInteractiveTopics)
        callbackMQTTClient!!.subscribe(appSettings.server_topic)
        callbackMQTTClient!!.subscribe(appSettings.push_notifications_subscribe_topic)
    }

    private fun subscribeForBackgroundMode(appSettings: AppSettings) {
        callbackMQTTClient!!.unsubscribeMass(allInteractiveTopics)
        callbackMQTTClient!!.unsubscribe(appSettings.server_topic)
        callbackMQTTClient!!.subscribe(appSettings.push_notifications_subscribe_topic)
    }


    fun subscribeForState(newState: Int) {
        val appSettings = AppSettings.instance
        appSettings.readFromPrefs(applicationContext)

        when (newState) {
            STATE_FULL_CONNECTED ->
                //3.0 callbackMQTTClient.subscribe(appSettings.subscribe_topic);
                subscribeForInteractiveMode(appSettings)
            STATE_HALF_CONNECTED -> if (appSettings.connection_in_background) {
                //3.0 callbackMQTTClient.subscribe(appSettings.push_notifications_subscribe_topic);
                subscribeForBackgroundMode(appSettings)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(javaClass.name, "onCreate()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(javaClass.name, "onDestroy()")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(javaClass.name, "onStartCommand()")

        val action = if (intent == null) "autostart" else intent.action
        Log.d(javaClass.name, "onStartCommand: " + action!!)

        when (action) {
            "autostart" -> {
            }
            "interactive" -> clientCountsInForeground++
            "pause" -> clientCountsInForeground--
        }//Log.d("test", "clientCountsInForeground++ ="+clientCountsInForeground);
        //Log.d("test", "clientCountsInForeground-- ="+clientCountsInForeground);

        val appSettings = AppSettings.instance
        appSettings.readFromPrefs(applicationContext)

        if (callbackMQTTClient == null) {
            Log.d(javaClass.name, "new CallbackMQTTClient()")
            callbackMQTTClient = CallbackMQTTClient(this)
        }

        //callbackMQTTClient.reConnect(appSettings);

        Log.d(javaClass.name, "clientCountsInForeground=" + clientCountsInForeground)

        push_topic = appSettings.push_notifications_subscribe_topic

        //showNotifyStatus(appSettings.push_notifications_subscribe_topic, false);

        showNotifyStatus("High energy consumption.", !appSettings.server_mode)


        //val rootSubscribeTopic = ""//3.0 appSettings.subscribe_topic.endsWith("#") ? appSettings.subscribe_topic.substring(0, appSettings.subscribe_topic.length() - 1) : appSettings.subscribe_topic;

        SERVER_DATAPACK_NAME = appSettings.server_topic


        if (mDbHelper == null) {
            mDbHelper = DbHelper(applicationContext)
            db = mDbHelper!!.writableDatabase
            historyCollector = HistoryCollector(db)
        }
        if(historyCollector!=null) {
            historyCollector!!.needCollectData = appSettings.server_mode || clientCountsInForeground > 0
        }

        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (wl == null) {
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, javaClass.name)
        }
        if (appSettings.server_mode) {
            wl!!.acquire()
        } else {
            if (wl!!.isHeld) {
                wl!!.release()
            }
            wl = null
        }

        return Service.START_STICKY
    }

    internal fun showNotifyStatus(text1: String, cancel: Boolean?) {
        val foreground_intent = Intent(this, MainActivity::class.java)

        foreground_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        foreground_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val pendingIntent = PendingIntent.getActivity(this, 0, foreground_intent, 0)
        val builder = Notification.Builder(this)
                .setContentTitle("Application server mode is on")
                //.setContentTitle("Linear MQTT Dashboard")
                //.setContentText("Application server mode is on")
                .setSmallIcon(R.drawable.ic_playblack)
                .setContentIntent(pendingIntent)
                .setOngoing(true).setSubText(text1)
                .setAutoCancel(true)

        if (cancel!!) {
            stopForeground(true)
        } else {
            startForeground(1, builder.build())
        }
    }


    internal fun showPushNotification(topic: String, message: String) {

        val intent = Intent(this, MainActivity::class.java)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)


        val builder = Notification.Builder(this)
                .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
                .setLights(Color.RED, 100, 100)

                .setContentTitle(message)
                //.setContentTitle("Linear MQTT Dashboard")
                //.setContentText("You have a new notification")

                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                //.setSubText(message)//"This is subtext...");   //API level 16
                .setAutoCancel(true)

        val manager: NotificationManager
        manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(getStringHash(topic), builder.build())// myNotication);

    }

    internal fun getStringHash(text: String): Int {
        var hash = 7
        for (i in 0 until text.length) {
            hash = hash * 31 + text[i].toInt()
        }
        return hash
    }

    fun publishMQTTMessage(topic: String, payload: Buffer, retained: Boolean) {
        if (topic == "") return
        if (callbackMQTTClient == null) return
        callbackMQTTClient!!.publish(topic, payload, retained)
    }

    fun setPayLoadChangeHandler(payLoadChanged: Handler) {
        mPayloadChanged = payLoadChanged
    }

    internal fun notifyDataInTopicChanged(topic: String?, payload: String?) {
        if (mPayloadChanged != null) {
            if (payload != currentMQTTValues[topic]) {
                val msg = Message()
                msg.obj = topic
                mPayloadChanged!!.sendMessage(msg)
            }
        }
    }

    override fun onReceiveMQTTMessage(topic: String, payload: Buffer) {

        //Log.d(javaClass.name, "onReceiveMQTTMessage() topic:" + topic+" payload:"+String(payload.toByteArray(), Charset.forName("UTF-8")))

        if (topic == SERVER_DATAPACK_NAME) {
            processUniversalPack(payload)
        } else {

            if (currentSessionTopicList.indexOf(topic) == -1) {
                currentSessionTopicList.add(topic)
                Log.d("currentSessionTopicList", "add:" + topic)
            }

            var payloadAsString = ""
            try {
                payloadAsString = String(payload.toByteArray(), Charset.forName("UTF-8"))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

            if (historyCollector != null && topic != SERVER_DATAPACK_NAME) {
                if (topicsForHistory != null && topicsForHistory!![topic] != null || topicsForLive != null && topicsForLive!![topic] != null) {
                    historyCollector!!.collect(topic, payloadAsString)
                    //Log.d("collect", ""+payloadAsString);
                }
            }
            processReceiveSimplyTopicPayloadData(topic, payloadAsString)
        }
    }

    internal fun processUniversalPack(payload: Buffer) {

        var payloadAsString: String? = null
        try {
            //payloadAsString = new String(payload.toByteArray(), "UTF-8");
            //разжимае
            val is_ = ByteArrayInputStream(payload.toByteArray())
            val istream = ZipInputStream(BufferedInputStream(is_))

            //int version=is.read();
            //var entry: ZipEntry?
            while (true){
                if(istream.nextEntry==null)break
                //entry = istream.nextEntry

                val os = ByteArrayOutputStream()

                val buff = ByteArray(1024)
                var count: Int
                while (true){
                    count = istream.read(buff, 0, 1024)
                    if(count==-1)break

                    os.write(buff, 0, count)
                }
                os.flush()
                os.close()

                payloadAsString = Utilities.bytesToStringUTFCustom(os.toByteArray(), os.toByteArray().size)
            }
            istream.close()
            is_.close()


        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e1: IOException) {
            e1.printStackTrace()
        }

        if(payloadAsString==null)return

        var jsonReader = JsonReader(StringReader(payloadAsString))
        try {
            var ver: Int? = 0
            var type: String? = null
            var data: String? = null

            jsonReader.beginObject()
            while (jsonReader.hasNext()) {
                val paramName = jsonReader.nextName()
                when (paramName) {
                    "ver" -> ver = jsonReader.nextInt()
                    "type" -> type = jsonReader.nextString()
                    "data" -> data = jsonReader.nextString()
                }
            }
            jsonReader.endObject()
            jsonReader.close()

            if (type == TOPICS_DATA) {

                jsonReader = JsonReader(StringReader(data!!))
                jsonReader.beginArray()
                while (jsonReader.hasNext()) {
                    var topicName = ""
                    var payloadData = ""

                    jsonReader.beginObject()
                    while (jsonReader.hasNext()) {
                        val paramName = jsonReader.nextName()
                        when (paramName) {
                            "topic" -> topicName = jsonReader.nextString()
                            "payload" -> payloadData = jsonReader.nextString()
                        }
                    }
                    jsonReader.endObject()

                    //Log.d("servermode", "topicName="+topicName+"  payload"+payloadData);
                    processReceiveSimplyTopicPayloadData(topicName, payloadData)
                }
                jsonReader.endArray()

            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    //OnReceive()
    internal fun processOnReceiveEvent(topic: String?, payload: String?) {
        if(dashboards==null)return;
        //if (!AppSettings.instance.server_mode) return

        for (dashboard in dashboards!!) {
            for (widgetData in dashboard.widgetsList) {
                if (widgetData.getSubTopic(0) != topic) continue

                val code = widgetData.onReceiveExecute
                if (code.isEmpty()) continue

                evalJS(widgetData, payload, code)
            }
        }
    }

    private fun processReceiveSimplyTopicPayloadData(topic: String, payload: String) {

        processOnReceiveEvent(topic, payload)

        notifyDataInTopicChanged(topic, payload)

        currentMQTTValues.put(topic, payload)
        currentDataVersion++

        if (push_topic != null && !push_topic!!.isEmpty()) {
            val pushTopicTemplate = push_topic!!.replace("/#".toRegex(), "")
            val templateSize = pushTopicTemplate.length
            if (topic.length >= templateSize && topic.substring(0, templateSize) == pushTopicTemplate) {
                val lastPush = lastReceivedMessagesByTopic[topic]
                if (lastPush == null || lastPush != payload) {
                    lastReceivedMessagesByTopic.put(topic, payload)

                    if (topic.startsWith(serverPushNotificationTopicRootPath)) {
                        //расширенное сообщение с сервера приложения, нужно интерпретировать
                        if (payload != "") {
                            showPushNotification(topic, payload)
                        }

                    } else {
                        //обычный кусок текста, нужно показать
                        if (payload != "") {
                            showPushNotification(topic, payload)
                        }
                    }
                }
            }
        }
    }

    companion object {

        internal val STATE_DISCONNECTED = 0
        internal val STATE_HALF_CONNECTED = 1
        internal val STATE_FULL_CONNECTED = 2

        internal var clientCountsInForeground = 0

        var instance: MQTTService? = null

        /*
        private val FULL_VERSION_FOR_ALL = "full_version_for_all"
        private val MESSAGE_TITLE = "message_title"
        private val MESSAGE_TEXT = "message_text"
        private val AD_FREQUENCY = "ad_frequency"
        private val REBUILD_HISTORY_DATA_FREQUENCY = "rebuild_history_data_frequency"
        */

        internal val TOPICS_DATA = "topics_data"

        internal var mDbHelper: DbHelper? = null
        internal var db: SQLiteDatabase? = null


        internal var connectionInUnActualMode = false

        internal var wl: PowerManager.WakeLock? = null

        internal var inRealForegroundMode = false
        internal var idleTime = 0
    }


}
