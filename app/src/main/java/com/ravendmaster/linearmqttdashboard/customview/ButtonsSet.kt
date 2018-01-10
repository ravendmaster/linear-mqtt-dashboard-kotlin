package com.ravendmaster.linearmqttdashboard.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SoundEffectConstants
import android.view.View

import com.ravendmaster.linearmqttdashboard.Log
import com.ravendmaster.linearmqttdashboard.R

class ButtonsSet
/*
    public void setLabelOff(String text) {
        labelOff = text == null ? "" : text;
        invalidate();
        //requestLayout();
    }

    public void setLabelOn(String text) {
        labelOn = text == null ? "" : text;
        invalidate();
        requestLayout();
    }
    */
(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var mButtonsSetEventListener: OnButtonsSetEventListener? = null

    var X: Int = 0 // Переменные, доступные снаружи
    var Y: Int = 0

    //int total_button_count = 15;

    internal var button_height: Int = 0

    internal var p = Paint()
    internal var bounds = Rect()

    var pressed = false
        internal set

    private var retained: Boolean = false

    private var Size: Int = 40;

    internal var mOn = false
    private var colorLight = -7617718
    //private String labelOff = "off";
    //private String labelOn = "on";

    private val valueCount: Int
        get() = if (values == null) 0 else values!!.size

    private var maxButtonsPerRow: Int = 0


    private val colCount: Int
        get() = Math.max(1, Math.min(valueCount, maxButtonsPerRow))

    private val rowCount: Int
        get() {
            val colCount = colCount
            return if (colCount == 0) 1 else 1 + (valueCount - 1) / colCount

        }

    internal var publishValues: String? = null

    internal var values: Array<String>? = null

    //requestLayout();
    var isOn: Boolean
        get() = mOn
        set(on) {
            mOn = on
            invalidate()
        }

    internal var pressed_button_index: Int? = null

    internal var delayedButtonOffHandler = Handler(Handler.Callback {
        pressed_button_index = null
        invalidate()
        false
    })


    fun getColorLight(): Int {
        return colorLight
    }

    fun setRetained(retained: Boolean) {
        this.retained = retained
    }

    fun setSize(size: Int) {
        this.Size = size
        updateGraphParams()
    }

    fun getSize():Int{
        return Size
    }

    fun updateGraphParams(){
        val density_multi = resources.displayMetrics.density
        button_height = Size * density_multi.toInt()
    }

    fun getMaxButtonsPerRow(): Int {
        return maxButtonsPerRow
    }

    fun setMaxButtonsPerRow(maxButtonsPerRow: Int) {
        this.maxButtonsPerRow = maxButtonsPerRow
        invalidate()
    }

    //public String getLabelOff() {
    //    return labelOff;
    //}

    //public String getLabelOn() {
    //    return labelOn;
    //}

    interface OnButtonsSetEventListener {
        fun OnButtonsSetPressed(button: ButtonsSet, index: Int)
    }

    fun setOnButtonsSetEventListener(l: OnButtonsSetEventListener) {
        this.mButtonsSetEventListener = l
    }

    //String currentValue;
    fun setCurrentValue(currentValue: String) {
        //Log.d("pressed_button_index", "setCurrentValue:"+currentValue);
        //currentValue=currentValue;
        pressed_button_index = null
        for (index in values!!.indices) {
            if (getPublishValueByButtonIndex(index) == currentValue) {
                //Log.d("pressed_button_index", ""+index);
                if (retained) {
                    pressed_button_index = index
                }
                pressed = true
                invalidate()
                break
            }
        }
    }

    fun setPublishValues(publishValues: String?) {
        this.publishValues = publishValues
        if (publishValues == null) return

        //StringBuffer s = new StringBuffer("asd zxc 123 sdf");
        values = publishValues.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        invalidate()
        requestLayout()
    }

    fun getPublishValues(): String? {
        return this.publishValues
    }

    fun setColorLight(colorLight: Int) {
        this.colorLight = colorLight
        invalidate()
        //requestLayout();
    }

    fun getPublishValueByButtonIndex(index: Int): String {
        if (index >= values!!.size) return ""
        val value = values!![index]
        val val_pres = value.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if(val_pres.size==0)return ""
        return val_pres[0].trim { it <= ' ' }
    }

    fun getPresentationTextByButtonIndex(index: Int): String {
        if (index >= values!!.size) return ""
        val value = values!![index]
        val val_pres = value.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return if (val_pres.size >= 2) {
            val_pres[1]
        } else val_pres[0].trim { it <= ' ' }

    }



    init {



        setSize(Size)


        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.RGBLEDView,
                0, 0)

        try {
            mOn = a.getBoolean(R.styleable.RGBLEDView_isOn, false)
            colorLight = a.getInteger(R.styleable.RGBLEDView_colorLight, Color.BLUE)
            //labelOff = a.getString(R.styleable.RGBLEDView_labelOff);
            //labelOn = a.getString(R.styleable.RGBLEDView_labelOn);
        } finally {
            a.recycle()
        }
        //if (labelOff == null) labelOff = "";
        //if (labelOn == null) labelOn = "button";

        p.isAntiAlias = true

        maxButtonsPerRow = 4
        //requestLayout();

        if (isInEditMode) {
            setPublishValues("1,2,3,4,5,6")
        }
    }

    internal fun getButtonIndexByXY(x: Int, y: Int): Int? {
        val button_by_x = x / (measuredWidth / colCount)
        val button_by_y = y / button_height
        val button_index = button_by_x + button_by_y * colCount
        return if (button_index < valueCount) {

            if (getPublishValueByButtonIndex(button_index).isEmpty()) null else button_index//нечего отправлять, кнопка нет

        } else null
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        if (!isEnabled) return true

        X = event.x.toInt()
        Y = event.y.toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> Log.d(javaClass.name, "DOWN")
            MotionEvent.ACTION_CANCEL -> {
                pressed = false
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {

                Log.d(javaClass.name, "UP, " + event.action)
                //mPressed = false;
                //if (mButtonsSetEventListener != null) mButtonsSetEventListener.OnMyButtonUp(this);

                val new_button_index = getButtonIndexByXY(X, Y)
                if (new_button_index != null) {
                    //if(retained) {
                    pressed_button_index = new_button_index

                    if (!retained) {
                        delayedButtonOffHandler.removeMessages(0)
                        delayedButtonOffHandler.sendEmptyMessageDelayed(0, 100)
                    }
                    //mPressed = true;
                    if (mButtonsSetEventListener != null) mButtonsSetEventListener!!.OnButtonsSetPressed(this, new_button_index)
                    invalidate()
                    playSoundEffect(SoundEffectConstants.CLICK)
                }

                invalidate()
            }
        }/*
                Integer button_index = getButtonIndexByXY(X, Y);
                if(button_index!=null) {
                    pressed_button_index=button_index;
                    mPressed = true;
                    //if (mButtonsSetEventListener != null) mButtonsSetEventListener.OnButtonsSetPressed(this, button_index);
                    invalidate();
                    //playSoundEffect(SoundEffectConstants.CLICK);
                }
*///requestLayout();

        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        // Try for a width based on our minimum
        val minw = paddingLeft + paddingRight + suggestedMinimumWidth
        val w = View.resolveSizeAndState(minw, widthMeasureSpec, 1)

        val mTextWidth = 200
        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        val minh = View.MeasureSpec.getSize(w) - mTextWidth.toInt() + paddingBottom + paddingTop
        val h = View.resolveSizeAndState(minh, heightMeasureSpec, 1)

        setMeasuredDimension(widthMeasureSpec, button_height * rowCount)
    }


    override fun onDraw(canvas: Canvas) {


        //Rect rect = canvas.getClipBounds();
        val buttonColor = colorLight

        val left = left
        val right = right

        val top = top
        val bottom = bottom


        val totalWidth = right - left
        val buttonWidth = totalWidth / colCount


        var button_count = 0
        for (row in 0 until rowCount) {
            val y = button_height * row
            for (col in 0 until colCount) {

                //if (button_count == getValueCount()) break;

                val x = col * buttonWidth

                if (!getPublishValueByButtonIndex(button_count).isEmpty()) {
                    DrawButton(canvas, buttonColor, x, y, x + buttonWidth, y + button_height, button_count)
                }

                button_count++
            }
        }

    }

    private fun DrawButton(canvas: Canvas, buttonColor: Int, left: Int, top: Int, right: Int, bottom: Int, current_button_index: Int) {


        val strokeWidth = 4

        val button_width = right - left - strokeWidth
        val button_height = bottom - top - strokeWidth

        val button_displace_y = strokeWidth / 2

        val alpha: Int
        val button_label: String
        val displace: Int
        val displace_bound = 6

        var label: String? = null
        if (current_button_index >= valueCount) {
            label = ""
        } else {
            label = getPresentationTextByButtonIndex(current_button_index)
        }


        if (isEnabled) {
            if (pressed_button_index != null && pressed_button_index == current_button_index) {
                alpha = 196
                //button_label = labelOff.isEmpty() ? labelOn : labelOff;
                button_label = label
                displace = displace_bound
            } else {
                alpha = 128
                //button_label = labelOn;
                button_label = label
                displace = 0
            }
        } else {
            alpha = 32
            displace = 0
            //button_label = labelOn;
            button_label = label
        }

        p.style = Paint.Style.FILL

        p.color = buttonColor // основа кнопки
        p.alpha = alpha
        canvas.drawRect((strokeWidth + left).toFloat(), (button_displace_y + top).toFloat(), (button_width + left).toFloat(), (button_height + button_displace_y + top).toFloat(), p)

        //углубление/тень
        canvas.drawRect((strokeWidth + left).toFloat(), (button_displace_y + displace + top).toFloat(), (button_width - displace_bound + left).toFloat(), (button_height + button_displace_y - displace_bound + displace + top).toFloat(), p)


        p.style = Paint.Style.STROKE
        p.strokeWidth = strokeWidth.toFloat()
        p.color = colorLight // обводка кнопки
        p.alpha = 128
        //canvas.drawRect(strokeWidth, button_displace_y, button_width, button_height + button_displace_y, primary_paint);

        p.style = Paint.Style.FILL
        p.strokeWidth = 1f

        p.color = -0x1
        p.alpha = 255

        p.textSize = button_height * 0.3f

        setBoundsOfThreeDots()
        val widthOfThreeDots = bounds.right - bounds.left
        var text = button_label.toUpperCase()
        val measuredWidth = FloatArray(100)
        val cnt = p.breakText(text, true, (button_width - widthOfThreeDots - 4).toFloat(), measuredWidth)

        if (cnt < text.length) {
            text = text.substring(0, cnt)
            text += "..."
        }
        p.getTextBounds(text, 0, text.length, bounds)




        canvas.drawText(text, (button_width / 2 - (bounds.right - bounds.left) / 2 + left).toFloat(), (displace / 2 + button_height / 2 + (bounds.bottom - bounds.top) / 2 + top).toFloat(), p)
    }

    internal fun setBoundsOfThreeDots(): Rect {
        p.getTextBounds("...", 0, 3, bounds)
        return bounds
    }


}
