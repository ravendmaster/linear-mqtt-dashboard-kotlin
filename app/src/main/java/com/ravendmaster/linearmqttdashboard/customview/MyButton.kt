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
import com.crashlytics.android.Crashlytics

import com.ravendmaster.linearmqttdashboard.Log
import com.ravendmaster.linearmqttdashboard.R

class MyButton(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var mMyButtonEventListener: OnMyButtonEventListener? = null

    var X: Int = 0 // Переменные, доступные снаружи
    var Y: Int = 0

    internal var p = Paint()
    internal var bounds = Rect()

    internal var mPressed = false

    internal var mOn = false
    private var colorLight = -7617718
    private var labelOff: String? = "off"
    private var labelOn: String? = "on"

    var isOn: Boolean
        get() = mOn
        set(on) {
            mOn = on
            invalidate()
            requestLayout()
        }

    fun getColorLight(): Int {
        return colorLight
    }

    fun getLabelOff(): String? {
        return labelOff
    }

    fun getLabelOn(): String? {
        return labelOn
    }

    interface OnMyButtonEventListener {
        fun OnMyButtonDown(button: MyButton)

        fun OnMyButtonUp(button: MyButton)
    }

    fun setOnMyButtonEventListener(l: OnMyButtonEventListener) {
        mMyButtonEventListener = l
    }

    override fun setPressed(pressed: Boolean) {
        mPressed = pressed
    }

    fun setColorLight(colorLight: Int) {
        this.colorLight = colorLight
        invalidate()
        requestLayout()
    }

    fun setLabelOff(text: String?) {
        labelOff = text ?: ""
        invalidate()
        requestLayout()
    }

    fun setLabelOn(text: String?) {
        labelOn = text ?: ""
        invalidate()
        requestLayout()
    }

    init {

        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.RGBLEDView,
                0, 0)

        try {
            mOn = a.getBoolean(R.styleable.RGBLEDView_isOn, false)
            colorLight = a.getInteger(R.styleable.RGBLEDView_colorLight, Color.BLUE)
            labelOff = a.getString(R.styleable.RGBLEDView_labelOff)
            labelOn = a.getString(R.styleable.RGBLEDView_labelOn)
        } finally {
            a.recycle()
        }
        if (labelOff == null) labelOff = ""
        if (labelOn == null) labelOn = "button"

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

        X = event.x.toInt()
        Y = event.y.toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                Log.d(javaClass.name, "DOWN")
                mPressed = true
                if (mMyButtonEventListener != null) mMyButtonEventListener!!.OnMyButtonDown(this)
                invalidate()
                requestLayout()
                playSoundEffect(SoundEffectConstants.CLICK)
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                Log.d(javaClass.name, "UP, " + event.action)
                mPressed = false
                if (mMyButtonEventListener != null) mMyButtonEventListener!!.OnMyButtonUp(this)
                invalidate()
                requestLayout()
            }
        }

        return true
    }

    /*
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

        int mTextWidth = 200;
        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        int minh = MeasureSpec.getSize(w) - (int) mTextWidth + getPaddingBottom() + getPaddingTop();
        int h = resolveSizeAndState(minh, heightMeasureSpec, 1);


        setMeasuredDimension(100, 100);
    }
*/
    override fun onDraw(canvas: Canvas) {
        //Rect rect = canvas.getClipBounds();
        val buttonColor = colorLight

        val left = left
        val right = right
        val top = top
        val bottom = bottom

        //int size = right - left;
        val x_center = (right - left) / 2
        val y_center = (bottom - top) / 2

        val strokeWidth = 4

        //int shadow_displace_y = (int) ((bottom - top) * 0.2f);
        val button_width = right - left - strokeWidth
        val button_height = bottom - top - strokeWidth


        val button_displace_y = strokeWidth / 2

        val alpha: Int
        val button_label: String?
        val displace: Int
        val displace_bound = 6



        if (isEnabled) {
            if (mPressed) {
                alpha = 196
                button_label = if (labelOff!!.isEmpty()) labelOn else labelOff
                displace = displace_bound
            } else {
                alpha = 128
                button_label = labelOn
                displace = 0
            }
        } else {
            alpha = 32
            displace = 0
            button_label = labelOn
        }

        p.style = Paint.Style.FILL
        //primary_paint.setColor(0xFFFFFFFF); // тень
        //canvas.drawRect(strokeWidth, button_displace_y, button_width, button_height + button_displace_y, primary_paint);
        //RectF rect=new RectF(strokeWidth,shadow_displace_y,button_width,button_height + shadow_displace_y);
        //canvas.drawRoundRect(rect, primary_paint);

        p.color = buttonColor // основа кнопки
        p.alpha = alpha
        canvas.drawRect(strokeWidth.toFloat(), button_displace_y.toFloat(), button_width.toFloat(), (button_height + button_displace_y).toFloat(), p)

        //углубление/тень
        canvas.drawRect(strokeWidth.toFloat(), (button_displace_y + displace).toFloat(), (button_width - displace_bound).toFloat(), (button_height + button_displace_y - displace_bound + displace).toFloat(), p)


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
        var text = button_label!!.toUpperCase()
        val measuredWidth = FloatArray(100)
        val cnt = p.breakText(text, true, (button_width - widthOfThreeDots - 4).toFloat(), measuredWidth)

        if (cnt < text.length) {
            text = text.substring(0, cnt)
            text += "..."
        }
        p.getTextBounds(text, 0, text.length, bounds)
        canvas.drawText(text, (button_width / 2 - (bounds.right - bounds.left) / 2).toFloat(), (displace / 2 + button_height / 2 + (bounds.bottom - bounds.top) / 2).toFloat(), p)

    }

    internal fun setBoundsOfThreeDots(): Rect {
        p.getTextBounds("...", 0, 3, bounds)
        return bounds
    }

}
