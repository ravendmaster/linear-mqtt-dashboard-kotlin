package com.ravendmaster.linearmqttdashboard.customview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SoundEffectConstants
import android.view.View

import com.ravendmaster.linearmqttdashboard.Log
import com.ravendmaster.linearmqttdashboard.R

/**
 * Created by Andrey on 15.06.2016.
 */
class Meter(context: Context, attrs: AttributeSet) : View(context, attrs) {

    var X: Int = 0 // Переменные, доступные снаружи
    var Y: Int = 0

    internal var p = Paint()
    internal var bounds = Rect()


    internal var mOn = false
    internal var mColorLight = -7617718
    var value: Float = 0.toFloat()

    internal val MODE_SIMPLE = 0
    internal val MODE_VALUE = 1
    internal val MODE_PERCENTAGE = 2
    internal val MODE_LOOP = 3

    var mode = MODE_SIMPLE

    internal var minimum_value = 0f
    internal var maximum_value = 100f
    internal var full_range = 101f

    internal var alarm_zone_minimum = 0f
    internal var alarm_zone_maximum = 0f

    internal var decimalMode = false

    var min: Float
        get() = minimum_value
        set(min) {
            minimum_value = min
            full_range = maximum_value - minimum_value
            if (full_range == 0f) full_range = 1f
        }

    var max: Float
        get() = maximum_value
        set(max) {
            maximum_value = max
            full_range = maximum_value - minimum_value
            if (full_range == 0f) full_range = 1f
        }

    internal var text = ""

    var colorLight: Int
        get() = mColorLight
        set(colorLight) {
            mColorLight = colorLight
            invalidate()
        }

    private val currentIndicationColor: Int?
        get() = if (value > minimum_value + full_range * alarm_zone_minimum) {

            if (value > maximum_value - full_range * (alarm_zone_maximum / 2)) {
                MyColors.red
            } else if (value > maximum_value - full_range * alarm_zone_maximum) {
                MyColors.yellow
            } else {
                MyColors.green
            }
        } else if (value > minimum_value + full_range * (alarm_zone_minimum / 2)) {
            MyColors.yellow
        } else {
            MyColors.red
        }

    fun setDecimalMode(decimalMode: Boolean) {
        this.decimalMode = decimalMode
    }

    fun setAlarmZones(min: Float, max: Float) {
        alarm_zone_minimum = min / 100
        alarm_zone_maximum = max / 100
    }

    fun setText(text: String) {
        this.text = text
    }

    init {

        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.RGBLEDView,
                0, 0)

        try {
            mColorLight = a.getInteger(R.styleable.RGBLEDView_colorLight, Color.BLUE)
        } finally {
            a.recycle()
        }

        p.isAntiAlias = true

        setLayerToHW(this)
    }


    private fun setLayerToHW(v: View) {
        if (!v.isInEditMode && Build.VERSION.SDK_INT >= 11) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        if (!isEnabled) return true


        //Log.d(getClass().getName(), event.toString());
        X = event.x.toInt()
        Y = event.y.toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

                Log.d(javaClass.name, "DOWN")
                return true
            }
            MotionEvent.ACTION_MOVE -> {
            }

            MotionEvent.ACTION_CANCEL -> {
            }

            MotionEvent.ACTION_UP -> {
                Log.d(javaClass.name, "UP, " + event.action)

                mode++
                if (mode >= MODE_LOOP) {
                    mode = MODE_SIMPLE
                }
                invalidate()
                playSoundEffect(SoundEffectConstants.CLICK)

                return true
            }
        }


        return false
    }

    override fun onDraw(canvas: Canvas) {

        val density_multi = resources.displayMetrics.density

        try {
            val left = left
            val right = right
            val top = top
            val bottom = bottom

            val numberOfMarks = 21
            val dev = (right - left) / numberOfMarks

            val arrowWidth = (dev * 0.9f).toInt()

            val button_width = right - left
            val button_height = bottom - top

            val meter_height = (button_height * 0.66).toInt()
            val meter_vertical_displace = button_height.toInt() * 0.5 - meter_height * 0.5

            val arrow_value: Float = value


            val phys_arrow_value: Float
            phys_arrow_value = numberOfMarks * (arrow_value - minimum_value) / full_range - 1
            p.style = Paint.Style.FILL

            val active_color = currentIndicationColor!!


            for (i in 0 until numberOfMarks) {

                if (phys_arrow_value >= i) {
                    p.color = active_color
                } else {
                    p.color = -0x222223
                }

                val x: Int
                x = i * button_width / (numberOfMarks - 1)

                if (isEnabled) {
                    p.alpha = 255
                } else {
                    p.alpha = 128
                }

                canvas.drawRect(x.toFloat(), meter_vertical_displace.toFloat(), (x + arrowWidth).toFloat(), (meter_height + meter_vertical_displace).toFloat(), p)
            }

            p.textSize = meter_height * 0.9f
            //primary_paint.setFakeBoldText(true);
            //primary_paint.setColor(0xff000000);
            var label: String? = null
            when (mode) {
                MODE_SIMPLE -> {
                }
                MODE_PERCENTAGE -> {
                    val cur_percentage_value = ((value - minimum_value) * 100 / full_range).toInt()
                    label = "" + cur_percentage_value + "%"
                }
                MODE_VALUE -> label = text
                else -> label = "n/a"
            }/*
                    if (decimalMode) {
                        label = String.valueOf(value);
                    } else {
                        label = String.valueOf((int) value);
                    }
                    */

            if (label != null) {

                p.getTextBounds(label, 0, label.length, bounds)

                //подложка
                /*
                if(phys_arrow_value>0 && active_color==MyColors.getRed()) {
                    int pan_x = button_width / 2 - (bounds.right - bounds.left) / 2;
                    int pan_y = button_height / 2 - (bounds.bottom - bounds.top) / 2;
                    int over = meter_height / 12;
                    primary_paint.setColor(0x80ffffff);
                    canvas.drawRect(pan_x - over, pan_y - over, bounds.right - bounds.left + pan_x + over, bounds.bottom - bounds.top + pan_y + over, primary_paint);
                }
                */


                p.color = -0x1000000

                if (isEnabled) {
                    p.alpha = 255
                } else {
                    p.alpha = 128
                }

                val text_x = button_width / 2 - (bounds.right - bounds.left) / 2
                val text_y = button_height / 2 + (bounds.bottom - bounds.top) / 2
                canvas.drawText(label, text_x.toFloat(), text_y.toFloat(), p)
            }

        } catch (e: Exception) {
            p.textSize = 30f
            p.color = -0x120000
            canvas.drawText("Error!", 0f, 30f, p)
            canvas.drawText("Please check", 0f, 60f, p)
            canvas.drawText("the home_screen_widget settings.", 0f, 90f, p)
        }

    }

    internal fun setBoundsOfThreeDots(): Rect {
        p.getTextBounds("...", 0, 3, bounds)
        return bounds
    }

    companion object {

        val modes = arrayOf("Simple", "Value", "Percentage")
    }
}
