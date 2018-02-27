package com.ravendmaster.linearmqttdashboard.service

import android.content.Context

import com.ravendmaster.linearmqttdashboard.R
import com.ravendmaster.linearmqttdashboard.customview.Graph
import com.ravendmaster.linearmqttdashboard.customview.Meter
import com.ravendmaster.linearmqttdashboard.customview.MyColors

import java.util.UUID

class WidgetData {


    var noUpdate = false

    val topicSuffix: String
        get() {

            if (type == WidgetData.WidgetTypes.GRAPH && mode >= Graph.PERIOD_TYPE_1_HOUR) {
                return Graph.HISTORY_TOPIC_SUFFIX
            }

            return if (type == WidgetData.WidgetTypes.GRAPH && mode == Graph.LIVE) {
                Graph.LIVE_TOPIC_SUFFIX
            } else ""

        }

    var type: WidgetTypes
    internal var names = arrayOfNulls<String>(4)

    fun isSystem():Boolean{
        if((names[0]!!.length<1) || (names[0]!!.get(0)!='%'))return false;

        return true;
    }

    var subscribeTopic = ""
    var publishTopic = ""

    //public String newValueTopic = "";

    var label = ""
    var label2 = ""

    internal var subTopics = arrayOfNulls<String>(4)
    internal var pubTopics = arrayOfNulls<String>(4)

    var uid = UUID.randomUUID()


    internal var primaryColors = arrayOfNulls<Int>(4)

    var feedback = true

    var publishValue = ""
    var publishValue2 = ""

    var retained = false

    var additionalValue = ""
    var additionalValue2 = ""
    var additionalValue3 = ""

    var decimalMode = false

    var mode = 0
    var submode = 0

    var formatMode = ""

    var onShowExecute = ""

    var onReceiveExecute = ""

    enum class WidgetTypes {
        VALUE,
        SWITCH,
        BUTTON,
        RGBLed,
        SLIDER,
        HEADER,
        METER,
        GRAPH,
        BUTTONSSET,
        COMBOBOX;


        val asInt: Int
            get() {
                when (this) {
                    VALUE -> return 0
                    SWITCH -> return 1
                    BUTTON -> return 2
                    RGBLed -> return 3
                    SLIDER -> return 4
                    HEADER -> return 5
                    METER -> return 6
                    GRAPH -> return 7
                    BUTTONSSET -> return 8
                    COMBOBOX -> return 9
                }
                return -1
            }

        companion object {

            fun getNames(context: Context): Array<String> {
                return arrayOf(context.getString(R.string.widget_type_value), context.getString(R.string.widget_type_switch), context.getString(R.string.widget_type_button), context.getString(R.string.widget_type_rgb_led), context.getString(R.string.widget_type_slider), context.getString(R.string.widget_type_header), context.getString(R.string.widget_type_meter), "Graph", "Buttons set", "Combo box")
            }

            fun getWidgetTypeByInt(i: Int): WidgetData.WidgetTypes? {
                when (i) {
                    0 -> return WidgetData.WidgetTypes.VALUE
                    1 -> return WidgetData.WidgetTypes.SWITCH
                    2 -> return WidgetData.WidgetTypes.BUTTON
                    3 -> return WidgetData.WidgetTypes.RGBLed
                    4 -> return WidgetData.WidgetTypes.SLIDER
                    5 -> return WidgetTypes.HEADER
                    6 -> return WidgetTypes.METER
                    7 -> return WidgetTypes.GRAPH
                    8 -> return WidgetTypes.BUTTONSSET
                    9 -> return WidgetTypes.COMBOBOX
                    else -> return null
                }
            }
        }
    }

    fun getName(index: Int): String {
        if(names[index]==null) {
            return ""
        }
        return names[index].toString()
    }

    fun setName(index: Int, name: String) {
        names[index] = name
    }

    fun getSubTopic(index: Int): String {
        return if (subTopics[index] == null) "" else subTopics[index].toString()
    }

    fun setSubTopic(index: Int, topic: String) {
        subTopics[index] = topic
    }

    fun getPubTopic(index: Int): String {
        return if (pubTopics[index] == null) "" else pubTopics[index].toString()
    }

    fun setPubTopic(index: Int, topic: String) {
        pubTopics[index] = topic
    }

    fun getPrimaryColor(index: Int): Int {

        return if (primaryColors[index] == null) MyColors.asBlack else primaryColors[index]!!
    }

    fun setPrimaryColor(index: Int, color: Int) {
        primaryColors[index] = color
    }

    constructor() {
        type = WidgetTypes.VALUE
    }

    fun setAdditionalValues(additionalValue: String, additionalValue2: String): WidgetData {
        this.additionalValue = additionalValue
        this.additionalValue2 = additionalValue2
        return this
    }

    constructor(type: WidgetTypes, name: String, topic: String, publishValue: String, publishValue2: String, primaryColor: Int, newValueTopic: String) {
        this.type = type
        this.setName(0, name)
        this.setSubTopic(0, topic)

        this.publishValue = publishValue
        this.publishValue2 = publishValue2
        this.setPrimaryColor(0, primaryColor)

        this.label = ""
        this.label2 = ""

        this.pubTopics[0] = newValueTopic

        this.retained = false

        this.additionalValue = ""
        this.additionalValue2 = ""

        this.additionalValue3 = ""

        this.mode = 0

        this.formatMode = ""
    }

    constructor(type: WidgetTypes, name: String, topic: String, publishValue: String, publishValue2: String, primaryColor: Int, label: String, label2: String, retained: Boolean) {
        this.type = type
        this.setName(0, name)
        this.setSubTopic(0, topic)

        this.publishValue = publishValue
        this.publishValue2 = publishValue2
        this.setPrimaryColor(0, primaryColor)

        this.label = label
        this.label2 = label2

        this.pubTopics[0] = ""

        this.retained = retained

        this.additionalValue = ""
        this.additionalValue = ""

        this.formatMode = ""
    }

    companion object {

        val Value_modes = arrayOf("Any", "Numbers")

        fun getWidgetModes(type: WidgetTypes): Array<String>? {

            when (type) {

                WidgetData.WidgetTypes.VALUE -> return Value_modes

                WidgetData.WidgetTypes.METER -> return Meter.modes

                WidgetData.WidgetTypes.GRAPH -> return Graph.period_names

                else -> return null
            }

        }
    }

}