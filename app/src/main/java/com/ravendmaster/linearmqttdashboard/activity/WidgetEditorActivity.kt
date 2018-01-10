package com.ravendmaster.linearmqttdashboard.activity

import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView

import com.ravendmaster.linearmqttdashboard.customview.MyColorPicker
import com.ravendmaster.linearmqttdashboard.service.WidgetData
import com.ravendmaster.linearmqttdashboard.R

class WidgetEditorActivity : AppCompatActivity(), View.OnClickListener {

    internal var mCreateNew: Boolean? = null
    internal var mCreateCopy: Boolean? = null
    internal var widget_index: Int = 0

    internal var widgetType: WidgetData.WidgetTypes = WidgetData.WidgetTypes.VALUE
    internal var widget_mode: Int? = 0

    internal lateinit var sub_topic_group: LinearLayout
    internal lateinit var pub_topic_group: LinearLayout

    internal lateinit var spinner_widget_mode: Spinner

    private var topic_colors = arrayOfNulls<Int>(4)

    internal lateinit var color_topic: ImageView
    internal lateinit var color_topic1: ImageView
    internal lateinit var color_topic2: ImageView
    internal lateinit var color_topic3: ImageView

    internal lateinit var editText_name: EditText
    internal lateinit var editText_name1: EditText
    internal lateinit var editText_name2: EditText
    internal lateinit var editText_name3: EditText

    internal lateinit var editText_sub_topic: EditText

    internal lateinit var editText_pub_topic: EditText

    internal lateinit var extended_topics_group: View

    internal lateinit var editText_topic1: EditText
    internal lateinit var editText_topic2: EditText
    internal lateinit var editText_topic3: EditText


    internal lateinit var textView_publish_value: TextView
    internal lateinit var editText_publish_value: EditText

    internal lateinit var textView_publish_value2: TextView
    internal lateinit var editText_publish_value2: EditText

    internal lateinit var labels_group: View
    internal lateinit var editText_labelOn: EditText
    internal lateinit var editText_labelOff: EditText

    internal lateinit var format_mode_group: View
    internal lateinit var textView_format_mode: TextView
    internal lateinit var editText_format_mode: EditText

    //View new_value_topic_group;
    //EditText editText_new_value_topic; //new value

    internal lateinit var retained_group: View
    internal lateinit var checkBox_retained: CheckBox

    internal lateinit var additional_values_group: View
    internal lateinit var textView_additional_value: TextView
    internal lateinit var editText_additional_value: EditText
    internal lateinit var textView_additional_value2: TextView
    internal lateinit var editText_additional_value2: EditText

    internal lateinit var additional_value_group: View
    internal lateinit var additional_value2_group: View
    internal lateinit var additional_value3_group: View
    internal lateinit var textView_additional_value3: TextView
    internal lateinit var editText_additional_value3: EditText

    //View primary_color_group;
    //MyColorPicker primary_color_picker;

    internal lateinit var checkBox_displayAsDecimal: CheckBox

    internal lateinit var codes_group: View
    internal lateinit var editText_codeOnShow: EditText


    internal lateinit var on_receive_codes_group: View
    internal lateinit var editText_codeOnReceive: EditText

    internal var widgetModes: Array<String>? = null

    internal lateinit var alertDialog: AlertDialog
    internal lateinit var myColorPicker: MyColorPicker
    internal var current_topic_index_for_select_color: Int = 0

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_editor, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.Save -> {
                var widget: WidgetData? = null
                if (mCreateNew!! || mCreateCopy!!) {
                    widget = WidgetData()
                    MainActivity.presenter.addWidget(widget)
                } else {
                    widget = MainActivity.presenter.getWidgetByIndex(widget_index)
                }

                widget.type = widgetType

                widget.setName(0, editText_name.text.toString())
                widget.setName(1, editText_name1.text.toString())
                widget.setName(2, editText_name2.text.toString())
                widget.setName(3, editText_name3.text.toString())

                widget.setSubTopic(0, editText_sub_topic.text.toString())
                widget.setSubTopic(1, editText_topic1.text.toString())
                widget.setSubTopic(2, editText_topic2.text.toString())
                widget.setSubTopic(3, editText_topic3.text.toString())

                widget.setPubTopic(0, editText_pub_topic.text.toString())

                widget.publishValue = editText_publish_value.text.toString()
                widget.publishValue2 = editText_publish_value2.text.toString()

                widget.label = editText_labelOn.text.toString()
                widget.label2 = editText_labelOff.text.toString()

                //home_screen_widget.newValueTopic = editText_new_value_topic.getText().toString();

                widget.additionalValue = editText_additional_value.text.toString()
                widget.additionalValue2 = editText_additional_value2.text.toString()

                widget.additionalValue3 = editText_additional_value3.text.toString()

                widget.setPrimaryColor(0, topic_colors[0]!!)
                widget.setPrimaryColor(1, topic_colors[1]!!)
                widget.setPrimaryColor(2, topic_colors[2]!!)
                widget.setPrimaryColor(3, topic_colors[3]!!)

                widget.retained = checkBox_retained.isChecked

                widget.decimalMode = checkBox_displayAsDecimal.isChecked

                widget.mode = spinner_widget_mode.selectedItemPosition

                widget.onShowExecute = editText_codeOnShow.text.toString()
                widget.onReceiveExecute = editText_codeOnReceive.text.toString()

                widget.formatMode = editText_format_mode.text.toString()

                MainActivity.presenter.saveActiveDashboard(applicationContext, MainActivity.presenter.activeDashboardId)

                MainActivity.presenter.widgetSettingsChanged(widget)

                finish()
            }
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget)

        mWidgetEditorActivity = this

        spinner_widget_mode = findViewById<View>(R.id.spinner_widget_mode) as Spinner

        editText_name = findViewById<View>(R.id.editText_name) as EditText
        editText_name1 = findViewById<View>(R.id.editText_name1) as EditText
        editText_name2 = findViewById<View>(R.id.editText_name2) as EditText
        editText_name3 = findViewById<View>(R.id.editText_name3) as EditText

        color_topic = findViewById<View>(R.id.color_topic) as ImageView
        color_topic.tag = 0//индекс топика
        color_topic1 = findViewById<View>(R.id.color_topic1) as ImageView
        color_topic1.tag = 1//индекс топика
        color_topic2 = findViewById<View>(R.id.color_topic2) as ImageView
        color_topic2.tag = 2//индекс топика
        color_topic3 = findViewById<View>(R.id.color_topic3) as ImageView
        color_topic3.tag = 3//индекс топика

        sub_topic_group = findViewById<View>(R.id.sub_topic_group) as LinearLayout
        editText_sub_topic = findViewById<View>(R.id.editText_sub_topic) as EditText

        pub_topic_group = findViewById<View>(R.id.pub_topic_group) as LinearLayout


        editText_pub_topic = findViewById<View>(R.id.editText_pub_topic) as EditText

        extended_topics_group = findViewById(R.id.extended_topics_group)
        editText_topic1 = findViewById<View>(R.id.editText_topic1) as EditText
        editText_topic2 = findViewById<View>(R.id.editText_topic2) as EditText
        editText_topic3 = findViewById<View>(R.id.editText_topic3) as EditText

        textView_publish_value = findViewById<View>(R.id.textView_publish_value) as TextView
        editText_publish_value = findViewById<View>(R.id.editText_publish_value) as EditText

        textView_publish_value2 = findViewById<View>(R.id.textView_publish_value2) as TextView
        textView_publish_value2.visibility = View.GONE
        editText_publish_value2 = findViewById<View>(R.id.editText_publish_value2) as EditText
        editText_publish_value2.visibility = View.GONE

        labels_group = findViewById(R.id.labels_group) as View
        editText_labelOn = findViewById<View>(R.id.editText_OnLabel) as EditText
        editText_labelOff = findViewById<View>(R.id.editText_OffLabel) as EditText

        format_mode_group = findViewById(R.id.format_mode_group) as View
        textView_format_mode = findViewById<View>(R.id.textView_format_mode) as TextView
        editText_format_mode = findViewById<View>(R.id.editText_format_mode) as EditText

        //new_value_topic_group = findViewById(R.id.new_value_topic_group);
        //editText_new_value_topic = (EditText) findViewById(R.id.editText_new_value_topic);

        retained_group = findViewById(R.id.retained_group)
        checkBox_retained = findViewById<View>(R.id.checkBox_retained) as CheckBox

        additional_values_group = findViewById(R.id.addition_values_group)
        textView_additional_value = findViewById<View>(R.id.textView_additionalValue) as TextView
        editText_additional_value = findViewById<View>(R.id.editText_additionalValue) as EditText
        textView_additional_value2 = findViewById<View>(R.id.textView_additionalValue2) as TextView
        editText_additional_value2 = findViewById<View>(R.id.editText_additionalValue2) as EditText

        additional_value_group = findViewById(R.id.addition_value_group)
        additional_value2_group = findViewById(R.id.addition_value2_group)
        additional_value3_group = findViewById(R.id.addition_value3_group)

        textView_additional_value3 = findViewById<View>(R.id.textView_additionalValue3) as TextView
        editText_additional_value3 = findViewById<View>(R.id.editText_additionalValue3) as EditText

        //primary_color_group = ((View) findViewById(R.id.primary_color_group));
        //primary_color_picker = (MyColorPicker) findViewById(R.id.color_picker);

        checkBox_displayAsDecimal = findViewById<View>(R.id.checkBox_decimalMode) as CheckBox


        codes_group = findViewById(R.id.codes_group)
        editText_codeOnShow = findViewById<View>(R.id.editText_codeOnShow) as EditText

        on_receive_codes_group = findViewById(R.id.on_receive_codes_group)
        editText_codeOnReceive = findViewById<View>(R.id.editText_codeOnReceive) as EditText

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, WidgetData.WidgetTypes.getNames(applicationContext))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val widget_type_spinner = findViewById<View>(R.id.spinner_widget_type) as Spinner
        widget_type_spinner.adapter = adapter
        // заголовок
        widget_type_spinner.prompt = "Widget type"
        // выделяем элемент


        // устанавливаем обработчик нажатия
        widget_type_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                widgetType = WidgetData.WidgetTypes.getWidgetTypeByInt(position)!!
                widgetModes = WidgetData.getWidgetModes(widgetType)

                //инициализация списков режимов
                if (widgetModes != null) {
                    val adapter_modes = ArrayAdapter(mWidgetEditorActivity, android.R.layout.simple_spinner_item, widgetModes!!)
                    adapter_modes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner_widget_mode.adapter = adapter_modes
                    if (widget_mode!! + 1 > adapter_modes.count) {
                        widget_mode = 0
                    }
                    spinner_widget_mode.setSelection(widget_mode!!)
                    spinner_widget_mode.prompt = "Widget mode"
                    spinner_widget_mode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            widget_mode = position
                        }

                        override fun onNothingSelected(arg0: AdapterView<*>) {}
                    }
                    //spinner_widget_mode.setSelection(wid);
                }


                var publishValueVisible: Int? = View.GONE
                var valueFieldName = ""

                var publishValue2Visible: Int? = View.GONE
                var value2FieldName = ""

                var primaryColorVisible: Int? = View.GONE

                var modeVisibility: Int? = View.GONE

                var labelsGroupVisible: Int? = View.GONE

                var newValueTopicGroupVisible: Int? = View.GONE

                var retainedVisible: Int? = View.GONE

                var additionalValuesVisible: Int? = View.GONE
                var additionalValueName = ""
                var additionalValue2Name = ""

                var additionalValueVisible: Int? = View.GONE
                var additionalValue2Visible: Int? = View.GONE
                var additionalValue3Visible: Int? = View.GONE

                var additionalValue3Name = ""

                var inputType = EditorInfo.TYPE_CLASS_TEXT
                var additionalInputType = EditorInfo.TYPE_CLASS_TEXT

                var additional3InputType = EditorInfo.TYPE_CLASS_TEXT

                var displayAsDecimalVisibleVisible: Int? = View.GONE

                var codesGroupVisible: Int? = View.GONE

                var formatModeGroupVisible: Int? = View.GONE

                var extendedTopicsGroupVisible: Int? = View.GONE



                when (widgetType) {
                    WidgetData.WidgetTypes.COMBOBOX -> {
                        additionalValueVisible = View.VISIBLE
                        primaryColorVisible = View.VISIBLE
                        retainedVisible = View.VISIBLE
                        publishValueVisible = View.VISIBLE
                        valueFieldName = "Values and labels (example: '0,127,255' or '0|OFF,127|50%,255|MAX')"
                    }
                    WidgetData.WidgetTypes.BUTTONSSET -> {
                        additionalValueVisible = View.VISIBLE
                        primaryColorVisible = View.VISIBLE
                        retainedVisible = View.VISIBLE
                        publishValueVisible = View.VISIBLE
                        valueFieldName = "Values and labels (example: '0,127,255' or '0|OFF,127|50%,255|MAX')"
                        formatModeGroupVisible = View.VISIBLE
                    }
                    WidgetData.WidgetTypes.GRAPH -> {
                        primaryColorVisible = View.VISIBLE
                        extendedTopicsGroupVisible = View.VISIBLE
                        modeVisibility = View.VISIBLE
                    }
                    WidgetData.WidgetTypes.VALUE -> {
                        additionalValueVisible = View.VISIBLE
                        primaryColorVisible = View.VISIBLE
                        newValueTopicGroupVisible = View.VISIBLE
                        codesGroupVisible = View.VISIBLE
                        modeVisibility = View.VISIBLE
                    }
                    WidgetData.WidgetTypes.BUTTON -> {
                        additionalValueVisible = View.VISIBLE
                        additionalValue2Visible = View.VISIBLE
                        primaryColorVisible = View.VISIBLE
                        publishValueVisible = View.VISIBLE
                        valueFieldName = getString(R.string.value_on)
                        publishValue2Visible = View.VISIBLE
                        value2FieldName = getString(R.string.value_off)
                        labelsGroupVisible = View.VISIBLE
                        retainedVisible = View.VISIBLE
                    }
                    WidgetData.WidgetTypes.SLIDER -> {

                        publishValueVisible = View.VISIBLE
                        valueFieldName = getString(R.string.range_from)
                        publishValue2Visible = View.VISIBLE
                        value2FieldName = getString(R.string.range_to)

                        inputType = EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_FLAG_DECIMAL or EditorInfo.TYPE_NUMBER_FLAG_SIGNED
                        additional3InputType = EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_FLAG_DECIMAL or EditorInfo.TYPE_NUMBER_FLAG_SIGNED

                        additionalValueVisible = View.VISIBLE
                        additionalValue2Visible = View.VISIBLE
                        additionalValue3Visible = View.VISIBLE
                        additionalValue3Name = getString(R.string.step)

                        displayAsDecimalVisibleVisible = View.VISIBLE
                        codesGroupVisible = View.VISIBLE
                    }
                    WidgetData.WidgetTypes.SWITCH -> {
                        additionalValueVisible = View.VISIBLE
                        additionalValue2Visible = View.VISIBLE

                        publishValueVisible = View.VISIBLE
                        valueFieldName = getString(R.string.value_on)
                        publishValue2Visible = View.VISIBLE
                        value2FieldName = getString(R.string.value_off)
                    }
                    WidgetData.WidgetTypes.RGBLed -> {
                        primaryColorVisible = View.VISIBLE
                        publishValueVisible = View.VISIBLE

                        additionalValueVisible = View.VISIBLE
                        additionalValue2Visible = View.VISIBLE

                        valueFieldName = getString(R.string.value_on)
                        publishValue2Visible = View.VISIBLE
                        value2FieldName = getString(R.string.value_off)
                    }
                    WidgetData.WidgetTypes.METER -> {
                        modeVisibility = View.VISIBLE

                        additionalValueVisible = View.VISIBLE
                        additionalValue2Visible = View.VISIBLE

                        additionalValuesVisible = View.VISIBLE
                        publishValueVisible = View.VISIBLE
                        valueFieldName = getString(R.string.range_from)
                        publishValue2Visible = View.VISIBLE
                        value2FieldName = getString(R.string.range_to)

                        additionalValueName = getString(R.string.alarm_zone_lower)
                        additionalValue2Name = getString(R.string.alarm_zone_upper)

                        inputType = EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_FLAG_DECIMAL or EditorInfo.TYPE_NUMBER_FLAG_SIGNED
                        additionalInputType = EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_FLAG_DECIMAL or EditorInfo.TYPE_NUMBER_FLAG_SIGNED

                        displayAsDecimalVisibleVisible = View.VISIBLE
                        codesGroupVisible = View.VISIBLE
                    }
                }//formatModeGroupVisible = View.VISIBLE;
                //extendedTopicsGroupVisible = View.VISIBLE;
                //newValueTopicGroupVisible = View.VISIBLE;

                extended_topics_group.visibility = extendedTopicsGroupVisible!!

                format_mode_group.visibility = formatModeGroupVisible!!

                additional_values_group.visibility = additionalValuesVisible!!

                additional_value_group.visibility = additionalValueVisible!!
                additional_value2_group.visibility = additionalValue2Visible!!
                additional_value3_group.visibility = additionalValue3Visible!!


                sub_topic_group.visibility = if (widgetType === WidgetData.WidgetTypes.HEADER) View.GONE else View.VISIBLE
                pub_topic_group.visibility = if (widgetType === WidgetData.WidgetTypes.HEADER || widgetType === WidgetData.WidgetTypes.GRAPH || widgetType === WidgetData.WidgetTypes.RGBLed) View.GONE else View.VISIBLE
                on_receive_codes_group.visibility = if (widgetType === WidgetData.WidgetTypes.HEADER) View.GONE else View.VISIBLE

                spinner_widget_mode.visibility = modeVisibility!!

                textView_publish_value.text = valueFieldName
                textView_publish_value.visibility = publishValueVisible!!
                editText_publish_value.visibility = publishValueVisible
                editText_publish_value.inputType = inputType
                editText_publish_value.setSingleLine(widgetType !== WidgetData.WidgetTypes.BUTTONSSET)


                textView_publish_value2.text = value2FieldName
                textView_publish_value2.visibility = publishValue2Visible!!
                editText_publish_value2.visibility = publishValue2Visible
                editText_publish_value2.inputType = inputType

                labels_group.visibility = labelsGroupVisible!!

                //new_value_topic_group.setVisibility(newValueTopicGroupVisible);

                color_topic.visibility = primaryColorVisible!!
                color_topic1.visibility = primaryColorVisible
                color_topic2.visibility = primaryColorVisible
                color_topic3.visibility = primaryColorVisible

                retained_group.visibility = retainedVisible!!

                textView_additional_value.text = additionalValueName
                textView_additional_value2.text = additionalValue2Name

                editText_additional_value.inputType = additionalInputType
                editText_additional_value2.inputType = additionalInputType

                textView_additional_value3.text = additionalValue3Name
                editText_additional_value3.inputType = additional3InputType

                checkBox_displayAsDecimal.visibility = displayAsDecimalVisibleVisible!!

                codes_group.visibility = codesGroupVisible!!


            }

            override fun onNothingSelected(arg0: AdapterView<*>) {}
        }

        val intent = intent

        lateinit var tempWidgetData: WidgetData
        mCreateNew = intent.getBooleanExtra("createNew", false)
        mCreateCopy = intent.getBooleanExtra("createCopy", false)
        widget_index = intent.getIntExtra("widget_index", 0)

        if (mCreateCopy!!) {
            tempWidgetData = MainActivity.presenter.getWidgetByIndex(widget_index)
        } else if (mCreateNew!!) {
            tempWidgetData = WidgetData()
        } else {
            tempWidgetData = MainActivity.presenter.getWidgetByIndex(widget_index)
        }

        widgetType = tempWidgetData.type

        editText_name.setText(tempWidgetData.getName(0))
        editText_name1.setText(tempWidgetData.getName(1))
        editText_name2.setText(tempWidgetData.getName(2))
        editText_name3.setText(tempWidgetData.getName(3))

        topic_colors[0] = tempWidgetData.getPrimaryColor(0)
        topic_colors[1] = tempWidgetData.getPrimaryColor(1)
        topic_colors[2] = tempWidgetData.getPrimaryColor(2)
        topic_colors[3] = tempWidgetData.getPrimaryColor(3)
        updateScreenColorsOfTopics()

        editText_sub_topic.setText(tempWidgetData.getSubTopic(0))

        editText_pub_topic.setText(tempWidgetData.getPubTopic(0))

        editText_topic1.setText(tempWidgetData.getSubTopic(1))

        editText_topic2.setText(tempWidgetData.getSubTopic(2))

        editText_topic3.setText(tempWidgetData.getSubTopic(3))

        editText_publish_value.setText(tempWidgetData.publishValue)
        editText_publish_value2.setText(tempWidgetData.publishValue2)

        editText_labelOn.setText(tempWidgetData.label)
        editText_labelOff.setText(tempWidgetData.label2)

        //editText_new_value_topic.setText(tempWidgetData.newValueTopic);

        checkBox_retained.isChecked = tempWidgetData.retained

        editText_additional_value.setText(tempWidgetData.additionalValue)
        editText_additional_value2.setText(tempWidgetData.additionalValue2)

        editText_additional_value3.setText(tempWidgetData.additionalValue3)

        checkBox_displayAsDecimal.isChecked = tempWidgetData.decimalMode

        editText_codeOnShow.setText(tempWidgetData.onShowExecute)

        /*
        SpannableStringBuilder sb = new SpannableStringBuilder(tempWidgetData.onReceiveExecute);
        int color = MyColors.getBlue();
        ForegroundColorSpan fcs  =new ForegroundColorSpan(color);
        sb.setSpan(fcs, 0, 2,0);
        editText_codeOnReceive.setText(sb);
        */

        editText_codeOnReceive.setText(tempWidgetData.onReceiveExecute)

        editText_format_mode.setText(tempWidgetData.formatMode)

        widget_type_spinner.setSelection(widgetType!!.asInt)
        widget_mode = tempWidgetData.mode

    }

    internal fun updateScreenColorsOfTopics() {
        color_topic.setColorFilter(topic_colors[0]!!)
        color_topic1.setColorFilter(topic_colors[1]!!)
        color_topic2.setColorFilter(topic_colors[2]!!)
        color_topic3.setColorFilter(topic_colors[3]!!)
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onPause() {
        //primary_color_picker.stopAnimation();
        super.onPause()
    }

    override fun onResume() {
        //primary_color_picker.startAnimation();
        super.onResume()
    }

    fun OnClickSelectTopicColor(view: View) {
        val index = view.tag as Int
        showColorPicker(index)

    }

    internal fun showColorPicker(topic_index: Int) {
        current_topic_index_for_select_color = topic_index
        val li = LayoutInflater.from(this)
        val promptsView = li.inflate(R.layout.clolor_picker, null)
        //TextView nameView = (TextView) promptsView.findViewById(R.id.textView_name);
        //nameView.setText(widgetData.getName(0));
        myColorPicker = promptsView.findViewById<View>(R.id.color_picker) as MyColorPicker
        myColorPicker.setOnClickListener(this)

        myColorPicker.color = topic_colors[topic_index]!!

        val alertDialogBuilder = AlertDialog.Builder(this)

        // set new_value_send_dialogue_send_dialog.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView)

     //   val userInput = promptsView.findViewById<View>(R.id.editTextDialogUserInput) as EditText
        //userInput.setText(presenter.getMQTTCurrentValue(widgetData.getSubTopic(0)).replace("*", ""));
        //userInput.setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL | EditorInfo.TYPE_NUMBER_FLAG_SIGNED);


        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setNegativeButton("Cancel"
                ) { dialog, id -> dialog.cancel() }

        // create alert dialog
        alertDialog = alertDialogBuilder.create()


        // show it
        alertDialog.show()
    }

    override fun onClick(view: View) {
        topic_colors[current_topic_index_for_select_color] = myColorPicker.color
        updateScreenColorsOfTopics()
        alertDialog.cancel()
    }

    fun OnClickHelp(view: View) {
        MainActivity.presenter.OnClickHelp(this, view)
    }

    companion object {


        internal var mWidgetEditorActivity: WidgetEditorActivity? = null
    }
}
