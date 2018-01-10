package com.ravendmaster.linearmqttdashboard.service

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Message
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView

import com.ravendmaster.linearmqttdashboard.Log
import com.ravendmaster.linearmqttdashboard.R
import com.ravendmaster.linearmqttdashboard.TabData
import com.ravendmaster.linearmqttdashboard.TabsCollection
import com.ravendmaster.linearmqttdashboard.activity.MainActivity
import com.ravendmaster.linearmqttdashboard.customview.ButtonsSet
import com.ravendmaster.linearmqttdashboard.customview.MyButton
import com.ravendmaster.linearmqttdashboard.Utilities

import org.fusesource.hawtbuf.Buffer

import java.util.ArrayList
import java.util.Timer
import java.util.TimerTask

class Presenter(internal var view: IView) {

    internal var mqttService: MQTTService? = null

    fun updateView(view: IView){
        this.view=view;
        mqttService = MQTTService.instance
    }

    val unusedTopics: Array<Any>
        get() {
            val unusedTopics = ArrayList<String>()
            if (mqttService!!.currentSessionTopicList != null) {
                for (topic in mqttService!!.currentSessionTopicList) {
                    if (topic.startsWith(mqttService!!.serverPushNotificationTopicRootPath))
                        continue
                    var topicInUse = false
                    for (dashboard in dashboards!!) {
                        if (dashboard.findWidgetByTopic(topic) != null) {
                            topicInUse = true
                            break
                        }
                    }
                    if (!topicInUse) {
                        unusedTopics.add(topic)
                    }
                }
            }
            return unusedTopics.toTypedArray()
        }

    val freeDashboardId: Int
        get() = mqttService!!.freeDashboardId

    val dashboards: ArrayList<Dashboard>?
        get() = if (mqttService == null) null else mqttService!!.dashboards

    val tabs: TabsCollection?
        get() {
            val appSettings = AppSettings.instance
            return appSettings.tabs
        }


    internal var connectionStatus = CONNECTION_STATUS.DISCONNECTED
    internal var mqttBrokerStatus = CONNECTION_STATUS.DISCONNECTED


    var handlerNeedRefreshDashboard = object : Handler() {
        override fun handleMessage(msg: android.os.Message) {
            view.onRefreshDashboard()
        }
    }

    var handlerNeedRefreshMQTTConnectionStatus = object : Handler() {
        override fun handleMessage(msg: android.os.Message) {
            view.setBrokerStatus(mqttBrokerStatus)
            view.setNetworkStatus(connectionStatus)
        }
    }

    internal var interactiveMode = false


    val screenActiveTabIndex: Int?
        get() = if (mqttService == null) null else mqttService!!.screenActiveTabIndex

    val activeDashboardId: Int
        get() = if (mqttService == null) 0 else mqttService!!.activeTabIndex

    val widgetsList: ArrayList<WidgetData>?
        get() = getWidgetsListOfTabIndex(activeDashboardId)

    val isMQTTBrokerOnline: Boolean
        get() = mqttService!!.isConnected

    internal var mActiveMode: Boolean = false

    internal var mTimerRefreshMQTTConnectionStatus: Timer? = null

    var isEditMode: Boolean
        get() = editMode
        set(editMode2) {
            editMode = editMode2
        }

    internal var lastSendTimestamp: Long = 0

    internal var mDelayedPublishValueHandler = object : Handler() {
        override fun handleMessage(msg: android.os.Message) {
            val sendMsgPack = msg.obj as SendMessagePack
            publishMQTTMessage(sendMsgPack.topic, Buffer(sendMsgPack.value!!.toByteArray()), sendMsgPack.retained!!)
        }
    }
    //switch

    //long click on value
    internal lateinit var widgetDataOfNewValueSender: WidgetData

    fun OnClickHelp(activity: AppCompatActivity, helpView: View) {

        var file = ""
        if (helpView === activity.findViewById<View>(R.id.help_onreceive)) {
            file = "help_onreceive.html"
        } else if (helpView === activity.findViewById<View>(R.id.help_onshow)) {
            file = "help_onshow.html"
        } else if (helpView === activity.findViewById<View>(R.id.help_push_topic)) {
            file = "help_push_topic.html"
        } else if (helpView === activity.findViewById<View>(R.id.help_application_server_mode)) {
            file = "help_application_server_mode.html"
        }


        val alert = AlertDialog.Builder(activity)

        val wv = WebView(activity)
        wv.loadUrl("file:///android_asset/web/" + file)
        wv.webViewClient = WebViewClient()
        alert.setView(wv)
        alert.setNegativeButton("Close") { dialog, id -> dialog.dismiss() }
        alert.show()
    }

    fun addNewTab(name: String) {
        val freeId = MainActivity.presenter!!.freeDashboardId
        MainActivity.presenter!!.dashboards!!.add(Dashboard(freeId))

        val tabData = TabData()
        tabData.id = freeId
        tabData.name = name

        val appSettings = AppSettings.instance
        appSettings.addTab(tabData)
    }

    fun saveTabsList(context: Context) {
        val appSettings = AppSettings.instance
        if (appSettings.settingsVersion == 0) {
            //переход на новую версию сохраненных настроек
            //состав табов храниться в tabs в виде JSON миссива id,name
            appSettings.settingsVersion = 1
            appSettings.saveConnectionSettingsToPrefs(context)
        }
        appSettings.saveTabsSettingsToPrefs(context)

        MainActivity.presenter!!.onTabPressed(-1)

    }

    fun resetCurrentSessionTopicList() {
        mqttService!!.currentSessionTopicList.clear()
        //mqttService.setCurrentSessionTopicList(new ArrayList<>());
    }

    fun subscribeToAllTopicsInDashboards(appSettings: AppSettings) {
        mqttService!!.subscribeForInteractiveMode(appSettings)
    }

    fun widgetSettingsChanged(widget: WidgetData) {
        val appSettings = AppSettings.instance
        mqttService!!.subscribeForInteractiveMode(appSettings)//достаточно подписаться только на +1 топик
    }

    interface IView {

        val appCompatActivity: AppCompatActivity

        fun onRefreshDashboard()

        fun notifyPayloadOfWidgetChanged(tabIndex: Int, widgetIndex: Int)

        fun setBrokerStatus(status: CONNECTION_STATUS)

        fun setNetworkStatus(status: CONNECTION_STATUS)

        fun onOpenValueSendMessageDialog(widgetData: WidgetData)

        fun onTabSelected()

        fun showPopUpMessage(title: String, text: String)
    }


    enum class CONNECTION_STATUS {
        DISCONNECTED,
        IN_PROGRESS,
        CONNECTED
    }

    fun connectionSettingsChanged() {
        mqttService!!.connectionSettingsChanged()
    }

    fun moveWidgetTo(context: Context, widgetData: WidgetData, dashboardID: Int) {
        val sourceDashboard = mqttService!!.getDashboardByID(activeDashboardId)
        val destinationDashboard = mqttService!!.getDashboardByID(dashboardID)
        destinationDashboard!!.widgetsList.add(widgetData)

        sourceDashboard!!.widgetsList.remove(widgetData)

        sourceDashboard.saveDashboard(context)
        destinationDashboard.saveDashboard(context)
    }

    fun moveWidget(context: Context, startColumn: Int, startRow: Int, stopColumn: Int, stopRow: Int) {

        Log.d("TAG", "moveWidget: $startColumn $startRow -> $stopColumn $stopRow")

        val srcTab = MainActivity.presenter!!.tabs!!.items[startColumn]
        val destTab = MainActivity.presenter!!.tabs!!.items[stopColumn]

        val sourceDashboard = mqttService!!.getDashboardByID(srcTab.id)//startColumn);
        val destinationDashboard = mqttService!!.getDashboardByID(destTab.id)//stopColumn);

        val widgetData = sourceDashboard!!.widgetsList[startRow]

        sourceDashboard.widgetsList.remove(widgetData)

        destinationDashboard!!.widgetsList.add(stopRow, widgetData)


        sourceDashboard.saveDashboard(context)
        if (startColumn != stopColumn) {
            destinationDashboard.saveDashboard(context)
        }
    }


    init {
        mqttService = MQTTService.instance

    }

    fun onTabPressed(screenIndex: Int) {
        var screenIndex = screenIndex
        if (screenIndex == -1) {
            screenIndex = mqttService!!.screenActiveTabIndex
        }
        val appSettings = AppSettings.instance
        mqttService!!.activeTabIndex = appSettings.tabs!!.getDashboardIdByTabIndex(screenIndex)

        //Log.d("dashboard orders", "dash id "+mqttService.activeTabIndex);

        mqttService!!.screenActiveTabIndex = screenIndex
        view.onTabSelected()
    }


    fun onCreate(appCompatActivity: AppCompatActivity) {
        if (mqttService == null) return
        mqttService!!.OnCreate(appCompatActivity)

    }

    fun initDemoDashboard() {
        mqttService!!.getDashboardByID(activeDashboardId)!!.initDemoDashboard()
    }

    fun saveActiveDashboard(context: Context, tabIndex: Int) {
        val activeDashboard = mqttService!!.getDashboardByID(tabIndex)
        activeDashboard?.saveDashboard(context)
    }

    fun saveAllDashboards(context: Context) {
        for (dashboard in mqttService!!.dashboards!!) {
            dashboard.saveDashboard(context)
        }
    }

    fun createDashboardsBySettings(forceReload : Boolean = false) {
        mqttService!!.createDashboardsBySettings(forceReload)
    }


    fun clearDashboard() {
        val activeDashboard = mqttService!!.getDashboardByID(activeDashboardId)
        activeDashboard?.clear()
    }

    fun getWidgetIndex(widgetData: WidgetData): Int {
        var tabIndex = 0
        for (dashboard in dashboards!!) {
            val index = dashboard.widgetsList.indexOf(widgetData)
            if (index != -1) {
                mqttService!!.activeTabIndex = dashboard.id
                mqttService!!.screenActiveTabIndex = tabIndex
                return index
            }
            tabIndex++
        }
        return -1
    }

    fun addWidget(widgetData: WidgetData) {
        mqttService!!.getDashboardByID(activeDashboardId)!!.widgetsList.add(widgetData)
    }

    fun getWidgetByIndex(index: Int): WidgetData {
        return mqttService!!.getDashboardByID(activeDashboardId)!!.widgetsList[index]
    }

    fun removeWidget(widgetData: WidgetData) {

        for (dashboard in dashboards!!) {
            val index = dashboard.widgetsList.indexOf(widgetData)
            if (index != -1) {
                dashboard.widgetsList.remove(widgetData)
                return
            }
        }
    }

    fun getWidgetsListOfTabIndex(tabIndex: Int): ArrayList<WidgetData>? {
        val mqttService = this.mqttService ?: return null
        val dashboard = mqttService.getDashboardByID(tabIndex) ?: return null
        return dashboard.widgetsList
    }

    fun getMQTTCurrentValue(topic: String): String {
        return mqttService!!.getMQTTCurrentValue(topic)
    }

    fun publishMQTTMessage(topic: String?, payload: Buffer, retained: Boolean) {

        mqttService!!.publishMQTTMessage(topic!!, payload, retained)
    }

    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return if (netInfo != null && netInfo.isConnectedOrConnecting) {
            true
        } else false
    }

    fun setCurrentMQTTValue(topic: String, value: String) {
        mqttService!!.currentMQTTValues.put(topic, value)
    }

    fun onPause() {
        Log.d(javaClass.name, "onPause()")

        val service_intent = Intent(view.appCompatActivity, MQTTService::class.java)
        service_intent.action = "pause"
        view.appCompatActivity.startService(service_intent)

        mTimerRefreshMQTTConnectionStatus!!.cancel()
        mTimerRefreshMQTTConnectionStatus = null

        mActiveMode = false
    }

    fun onDestroy(appCompatActivity: AppCompatActivity) {}

    fun onResume(appCompatActivity: AppCompatActivity) {

        mActiveMode = true

        val service_intent = Intent(appCompatActivity, MQTTService::class.java)
        service_intent.action = "interactive"
        appCompatActivity.startService(service_intent)
        mqttService = MQTTService.instance
        Log.d(javaClass.name, "mqttService=MQTTService.getInstance()=" + mqttService!!)



        mTimerRefreshMQTTConnectionStatus = Timer()
        mTimerRefreshMQTTConnectionStatus!!.schedule(object : TimerTask() {
            override fun run() {
                if (mqttService == null) return
                if (!mActiveMode) return
                mqttBrokerStatus = if (isMQTTBrokerOnline) CONNECTION_STATUS.CONNECTED else CONNECTION_STATUS.DISCONNECTED
                connectionStatus = if (isOnline(appCompatActivity)) CONNECTION_STATUS.CONNECTED else CONNECTION_STATUS.DISCONNECTED
                if (mqttService != null) {
                    handlerNeedRefreshMQTTConnectionStatus.sendEmptyMessage(0)
                }
            }
        }, 0, 500)

        if(dashboards==null){
            createDashboardsBySettings()
        }

        view.onTabSelected()

        if (mqttService != null) {

            val handlerPayloadChanged = object : Handler() {
                override fun handleMessage(msg: android.os.Message) {
                    val topic = msg.obj as String
                    startPayloadChangedNotification(topic)
                }
            }
            mqttService!!.setPayLoadChangeHandler(handlerPayloadChanged)
        }

    }

    //обход виджетов в поисках подписки на изменения для оповещения
    internal fun startPayloadChangedNotification(topic: String) {

        var tabIndex = 0
        for (tabData in tabs!!.items) {

            val dashboard = mqttService!!.getDashboardByID(tabData.id) ?: continue

            val widgetsList = dashboard.widgetsList//tabIndex).getWidgetsList();
            var index = 0

            for (widgetData in widgetsList) {
                var needNotify = false
                for (i in 0..3) {
                    var topic_widget: String? = widgetData.getSubTopic(i)
                    topic_widget += widgetData.topicSuffix//Graph.HISTORY_TOPIC_SUFFIX;
                    if (topic_widget != null && topic_widget == topic && !widgetData.noUpdate) {
                        if (widgetData.type == WidgetData.WidgetTypes.BUTTONSSET && !widgetData.retained) {
                            //не обновляем
                        } else {
                            needNotify = true
                            break
                        }
                    }
                }
                if (needNotify) view.notifyPayloadOfWidgetChanged(tabIndex, index)
                index++
            }
            tabIndex++
        }
    }

    //seek bar
    fun onStartTrackingTouch(seekBar: SeekBar) {
        handlerNeedRefreshDashboard.removeMessages(0)
        interactiveMode = true
    }

    internal inner class SendMessagePack {
        var topic: String? = null
        var value: String? = null
        var retained: Boolean? = null
    }

    fun onProgressChanged(seekBar: SeekBar) {

        val tagData = seekBar.tag as Array<Any>
        val widget = tagData[0] as WidgetData
        widget.noUpdate = true

        val textViewValue = tagData[1] as TextView

        val currentInteractiveValue = getSeekDisplayValue(widget, seekBar)

        mDelayedPublishValueHandler.removeMessages(0)

        val pack = SendMessagePack()
        pack.topic = getTopicForPublishValue(widget)//.getSubTopic(0);
        pack.value = currentInteractiveValue
        pack.retained = true


        val msg = Message()
        msg.obj = pack
        msg.what = 0

        val delay: Int
        if (System.currentTimeMillis() - lastSendTimestamp > 500) { //anti flood
            delay = 0
            lastSendTimestamp = System.currentTimeMillis()
        } else {
            delay = 500
        }

        mDelayedPublishValueHandler.sendMessageDelayed(msg, delay.toLong())

        var showValue: String? = currentInteractiveValue
        if (!widget.onShowExecute.isEmpty()) {
            showValue = evalJS(widget, currentInteractiveValue, widget.onShowExecute)
        }

        textViewValue.text = showValue
    }

    fun evalJS(contextWidgetData: WidgetData, value: String, code: String): String? {
        return mqttService!!.evalJS(contextWidgetData, value, code)
    }

    private fun getSeekDisplayValue(widget: WidgetData, seekBar: SeekBar): String {
        val main_step = Utilities.parseFloat(widget.additionalValue3, 1)
        val min_value = Utilities.parseFloat(widget.publishValue, 0)
        val valueInDecimal = Utilities.round(min_value + seekBar.progress * main_step)
        return if (widget.decimalMode) {
            valueInDecimal.toString()
        } else {
            valueInDecimal.toInt().toString()
        }
    }

    fun onStopTrackingTouch(seekBar: SeekBar) {
        Log.d(javaClass.name, "onStopTrackingTouch()")
        val tagData = (seekBar as View).tag as Array<Any>
        val widget = tagData[0] as WidgetData
        widget.noUpdate = false

        interactiveMode = false
        view.onRefreshDashboard()
    }
    //seek bar

    internal fun getTopicForPublishValue(widget: WidgetData): String {
        return if (widget.getPubTopic(0).isEmpty()) widget.getSubTopic(0) else widget.getPubTopic(0)
    }

    //my button
    fun onMyButtonDown(button: MyButton) {
        handlerNeedRefreshDashboard.removeMessages(0)
        interactiveMode = true
        val widget = button.tag as WidgetData

        if (widget.publishValue != "") {
            widget.noUpdate = true
            publishMQTTMessage(getTopicForPublishValue(widget), Buffer(widget.publishValue.toByteArray()), widget.retained)
        }
    }

    fun onMyButtonUp(button: MyButton) {
        val widget = button.tag as WidgetData
        widget.noUpdate = false
        if (widget.publishValue2 != "") {
            publishMQTTMessage(getTopicForPublishValue(widget), Buffer(widget.publishValue2.toByteArray()), widget.retained)
        }
        interactiveMode = false
        view.onRefreshDashboard()
    }
    //my button

    //buttonsset
    fun OnButtonsSetPressed(buttonsSet: ButtonsSet, index: Int) {
        handlerNeedRefreshDashboard.removeMessages(0)
        val widget = buttonsSet.tag as WidgetData
        if (widget.publishValue != "") {
            publishMQTTMessage(getTopicForPublishValue(widget), Buffer(buttonsSet.getPublishValueByButtonIndex(index).toByteArray()), widget.retained)
        }
        view.onRefreshDashboard()
    }

    //switch
    fun onClickWidgetSwitch(view: View) {
        val widget = view.tag as WidgetData
        val widget_switch = view as Switch
        val newValue = if (widget_switch.isChecked) widget.publishValue else widget.publishValue2

        publishMQTTMessage(getTopicForPublishValue(widget), Buffer(newValue.toByteArray()), true)
    }

    fun onLongClick(v: View): Boolean {
        widgetDataOfNewValueSender = v.tag as WidgetData
        if (!widgetDataOfNewValueSender.getPubTopic(0).isEmpty()) {
            view.onOpenValueSendMessageDialog(widgetDataOfNewValueSender)
            return true
        } else {
            return false
        }
    }

    //new value for value home_screen_widget
    fun sendMessageNewValue(newValue: String) {
        publishMQTTMessage(widgetDataOfNewValueSender.getPubTopic(0), Buffer(newValue.toByteArray()), false)
    }

    //combo box
    fun onComboBoxSelector(v: View) {
        widgetDataOfNewValueSender = v.tag as WidgetData
    }

    fun sendComboBoxNewValue(newValue: String) {
        publishMQTTMessage(getTopicForPublishValue(widgetDataOfNewValueSender), Buffer(newValue.toByteArray()), widgetDataOfNewValueSender.retained)
    }

    fun onMainMenuItemSelected() {}

    companion object {


        internal var editMode = false
    }
}
