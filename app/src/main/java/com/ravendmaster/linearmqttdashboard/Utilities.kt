package com.ravendmaster.linearmqttdashboard

import android.view.View
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView

import com.ravendmaster.linearmqttdashboard.customview.ButtonsSet
import com.ravendmaster.linearmqttdashboard.customview.Graph
import com.ravendmaster.linearmqttdashboard.customview.Meter
import com.ravendmaster.linearmqttdashboard.customview.MyButton
import com.ravendmaster.linearmqttdashboard.customview.RGBLEDView

import java.math.BigInteger
import java.util.UUID

object Utilities {

    /*
    @Throws(NumberFormatException::class)
    fun isNumeric(s: String): Boolean {
        try {
            java.lang.Double.parseDouble(s)
            return true
        } catch (e: NumberFormatException) {
            return false
        }

    }
    */

    fun onBindDragTabView(clickedView: View, dragView: View) {
        (dragView.findViewById<View>(R.id.tab_name) as TextView).text = (clickedView.findViewById<View>(R.id.tab_name) as TextView).text
    }

    fun onBindDragWidgetView(clickedView: View, dragView: View) {
        dragView.findViewById<View>(R.id.root).layoutParams.height=clickedView.findViewById<View>(R.id.root).layoutParams.height
        dragView.findViewById<View>(R.id.widget_drag_place).layoutParams.width=clickedView.findViewById<View>(R.id.widget_drag_place).layoutParams.width


        dragView.findViewById<View>(R.id.root).visibility = clickedView.findViewById<View>(R.id.root).visibility

        (dragView.findViewById<View>(R.id.widget_name) as TextView).text = (clickedView.findViewById<View>(R.id.widget_name) as TextView).text
        (dragView.findViewById<View>(R.id.widget_topic) as TextView).text = (clickedView.findViewById<View>(R.id.widget_topic) as TextView).text
        (dragView.findViewById<View>(R.id.widget_value) as TextView).text = (clickedView.findViewById<View>(R.id.widget_value) as TextView).text
        dragView.findViewById<View>(R.id.widget_value).visibility = clickedView.findViewById<View>(R.id.widget_value).visibility
        dragView.findViewById<View>(R.id.widget_value1).visibility = clickedView.findViewById<View>(R.id.widget_value1).visibility
        dragView.findViewById<View>(R.id.widget_value2).visibility = clickedView.findViewById<View>(R.id.widget_value2).visibility
        dragView.findViewById<View>(R.id.widget_value3).visibility = clickedView.findViewById<View>(R.id.widget_value3).visibility


        (dragView.findViewById<View>(R.id.widget_value) as TextView).setTextColor((clickedView.findViewById<View>(R.id.widget_value) as TextView).textColors)

        (dragView.findViewById<View>(R.id.widget_meter) as Meter).mode = (clickedView.findViewById<View>(R.id.widget_meter) as Meter).mode
        (dragView.findViewById<View>(R.id.widget_meter) as Meter).visibility = (clickedView.findViewById<View>(R.id.widget_meter) as Meter).visibility
        (dragView.findViewById<View>(R.id.widget_meter) as Meter).value = (clickedView.findViewById<View>(R.id.widget_meter) as Meter).value
        (dragView.findViewById<View>(R.id.widget_meter) as Meter).min = (clickedView.findViewById<View>(R.id.widget_meter) as Meter).min
        (dragView.findViewById<View>(R.id.widget_meter) as Meter).max = (clickedView.findViewById<View>(R.id.widget_meter) as Meter).max

        (dragView.findViewById<View>(R.id.widget_button) as MyButton).visibility = (clickedView.findViewById<View>(R.id.widget_button) as MyButton).visibility
        (dragView.findViewById<View>(R.id.widget_button) as MyButton).setColorLight((clickedView.findViewById<View>(R.id.widget_button) as MyButton).getColorLight())
        (dragView.findViewById<View>(R.id.widget_button) as MyButton).setLabelOff((clickedView.findViewById<View>(R.id.widget_button) as MyButton).getLabelOff())
        (dragView.findViewById<View>(R.id.widget_button) as MyButton).setLabelOn((clickedView.findViewById<View>(R.id.widget_button) as MyButton).getLabelOn())

        (dragView.findViewById<View>(R.id.widget_buttons_set) as ButtonsSet).visibility = (clickedView.findViewById<View>(R.id.widget_buttons_set) as ButtonsSet).visibility
        (dragView.findViewById<View>(R.id.widget_buttons_set) as ButtonsSet).setPublishValues((clickedView.findViewById<View>(R.id.widget_buttons_set) as ButtonsSet).getPublishValues())
        (dragView.findViewById<View>(R.id.widget_buttons_set) as ButtonsSet).setColorLight((clickedView.findViewById<View>(R.id.widget_buttons_set) as ButtonsSet).getColorLight())
        (dragView.findViewById<View>(R.id.widget_buttons_set) as ButtonsSet).setMaxButtonsPerRow((clickedView.findViewById<View>(R.id.widget_buttons_set) as ButtonsSet).getMaxButtonsPerRow())
        (dragView.findViewById<View>(R.id.widget_buttons_set) as ButtonsSet).setSize((clickedView.findViewById<View>(R.id.widget_buttons_set) as ButtonsSet).getSize())

        (dragView.findViewById<View>(R.id.widget_switch) as Switch).visibility = (clickedView.findViewById<View>(R.id.widget_switch) as Switch).visibility
        (dragView.findViewById<View>(R.id.widget_switch) as Switch).isChecked = (clickedView.findViewById<View>(R.id.widget_switch) as Switch).isChecked
        (dragView.findViewById<View>(R.id.widget_switch) as Switch).isEnabled = (clickedView.findViewById<View>(R.id.widget_switch) as Switch).isEnabled

        dragView.findViewById<View>(R.id.seek_bar_group).visibility = clickedView.findViewById<View>(R.id.seek_bar_group).visibility
        (dragView.findViewById<View>(R.id.widget_seekBar) as SeekBar).progress = (clickedView.findViewById<View>(R.id.widget_seekBar) as SeekBar).progress
        (dragView.findViewById<View>(R.id.widget_seekBar) as SeekBar).max = (clickedView.findViewById<View>(R.id.widget_seekBar) as SeekBar).max
        (dragView.findViewById<View>(R.id.widget_RGBLed) as RGBLEDView).visibility = (clickedView.findViewById<View>(R.id.widget_RGBLed) as RGBLEDView).visibility

        dragView.findViewById<View>(R.id.imageView_edit_button).visibility = clickedView.findViewById<View>(R.id.imageView_edit_button).visibility

        dragView.findViewById<View>(R.id.widget_graph).visibility = clickedView.findViewById<View>(R.id.widget_graph).visibility

        for (i in 0..3) {
            (dragView.findViewById<View>(R.id.widget_graph) as Graph).setValue(i, (clickedView.findViewById<View>(R.id.widget_graph) as Graph).getValue(i))
            (dragView.findViewById<View>(R.id.widget_graph) as Graph).setColorLight(i, (clickedView.findViewById<View>(R.id.widget_graph) as Graph).getColorLight(i))
            (dragView.findViewById<View>(R.id.widget_graph) as Graph).setName(i, (clickedView.findViewById<View>(R.id.widget_graph) as Graph).getName(i))
        }

        (dragView.findViewById<View>(R.id.widget_graph) as Graph).mode = (clickedView.findViewById<View>(R.id.widget_graph) as Graph).mode

        dragView.findViewById<View>(R.id.imageView_combo_box_selector).visibility = clickedView.findViewById<View>(R.id.imageView_combo_box_selector).visibility

        dragView.findViewById<View>(R.id.imageView_js).visibility = clickedView.findViewById<View>(R.id.imageView_js).visibility
    }

    fun createUUIDByString(uidString: String): UUID {
        val s2 = uidString.replace("-", "")
        return UUID(
                BigInteger(s2.substring(0, 16), 16).toLong(),
                BigInteger(s2.substring(16), 16).toLong())
    }

    fun parseInt(input: String, def: Int): Int {
        try {
            return Integer.parseInt(input)
        } catch (e: Exception) {
        }

        return def
    }

    fun parseFloat(input: String, def: Int): Float {
        try {
            return java.lang.Float.parseFloat(input)
        } catch (e: Exception) {
        }

        return def.toFloat()
    }

    fun round(input: Float): Float {
        return Math.round(input * 1000f) / 1000f
    }

    /*
https://stackoverflow.com/questions/88838/how-to-convert-strings-to-and-from-utf8-byte-arrays-in-java
Convert from String to byte[]:

String s = "some text here";
byte[] b = s.getBytes("UTF-8");

Convert from byte[] to String:

byte[] b = {(byte) 99, (byte)97, (byte)116};
String s = new String(b, "US-ASCII");
 */

    fun stringToBytesUTFCustom(str: String): ByteArray {
        //return str.getBytes(StandardCharsets.UTF_8);
        return str.toByteArray()
    }

    fun bytesToStringUTFCustom(bytes: ByteArray, count: Int): String {
        //return new String(bytes, StandardCharsets.UTF_8);
        return String(bytes)
    }

    /* Old code:
    public static byte[] stringToBytesUTFCustom(String str) {

        char[] buffer = str.toCharArray();

        byte[] b = new byte[buffer.length << 1];

        for(int i = 0; i < buffer.length; i++) {

            int bpos = i << 1;

            b[bpos] = (byte) ((buffer[i]&0xFF00)>>8);

            b[bpos + 1] = (byte) (buffer[i]&0x00FF);

        }

        return b;

    }

    public static String bytesToStringUTFCustom(byte[] bytes, int count) {

        char[] buffer = new char[bytes.length >> 1];

        for(int i = 0; i < count/2; i++) {

            int bpos = i << 1;

            char c = (char)(((bytes[bpos]&0x00FF)<<8) + (bytes[bpos+1]&0x00FF));

            buffer[i] = c;

        }

        return new String(buffer);

    }
    */



}
