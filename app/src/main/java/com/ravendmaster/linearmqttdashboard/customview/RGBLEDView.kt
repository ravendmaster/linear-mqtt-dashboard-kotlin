package com.ravendmaster.linearmqttdashboard.customview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

import com.ravendmaster.linearmqttdashboard.R

class RGBLEDView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    var X: Int = 0 // Переменные, доступные снаружи
    var Y: Int = 0

    var isSize: Int = 0
        internal set
    internal var mOn: Boolean = false
    internal var mColorLight = -7617718

    var isColorLight: Int
        get() = mColorLight
        set(colorLight) {
            mColorLight = colorLight
            invalidate()
            requestLayout()
        }

    var isOn: Boolean
        get() = mOn
        set(on) {
            mOn = on
            invalidate()
            requestLayout()
        }

    fun setmSize(size: Int) {
        isSize = size
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
            mColorLight = a.getInteger(R.styleable.RGBLEDView_colorLight, Color.RED)
            isSize = a.getInteger(R.styleable.RGBLEDView_size, 96)
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
        X = event.x.toInt()
        Y = event.y.toInt()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        //Rect rect = canvas.getClipBounds();

        val left = left
        val right = right
        val top = top
        val bottom = bottom

        val size = Math.min(right - left, bottom - top)
        val x_center = (right - left) / 2
        val y_center = (bottom - top) / 2

        var alpha_k = 1f
        if (!isEnabled) {
            alpha_k = 0.3f
        }


        if (mOn) {

            val a = Color.alpha(mColorLight)
            val r = Color.red(mColorLight)
            val g = Color.green(mColorLight)
            val b = Color.blue(mColorLight)

            //primary_paint.setARGB( (int)(32*alpha_k), (int) (r * 0.9f), (int) (g * 0.9f), (int) (b * 0.9f));
            //canvas.drawCircle(x_center, y_center, size / 2, primary_paint); //ореол

            p.setARGB((255 * alpha_k).toInt(), (r * 0.9f).toInt(), (g * 0.9f).toInt(), (b * 0.9f).toInt())
            canvas.drawCircle(x_center.toFloat(), y_center.toFloat(), size / 3.3f, p)

            p.setARGB((255 * alpha_k).toInt(), r, g, b)
            canvas.drawCircle(x_center.toFloat(), y_center.toFloat(), size / 4f, p)

            p.setARGB((192 / 2 * alpha_k).toInt(), 255, 255, 255)
            canvas.drawCircle((x_center - size / 12).toFloat(), (y_center - size / 12).toFloat(), size / 12f, p)


        } else {

            //primary_paint.setARGB(32, 0, 0, 0);
            //canvas.drawCircle(x_center, y_center+ size*0.05f, size / 3.3f, primary_paint); //тень

            p.setARGB(50, 50, 50, 50)
            canvas.drawCircle(x_center.toFloat(), y_center.toFloat(), size / 3.3f, p)

            p.setARGB(50, 255, 255, 255)
            canvas.drawCircle(x_center.toFloat(), y_center.toFloat(), size / 4f, p)

            p.setARGB(255, 255, 255, 255)
            canvas.drawCircle((x_center - size / 12).toFloat(), (y_center - size / 12).toFloat(), size / 12f, p)
        }


    }

    companion object {

        internal val p = Paint()
    }
}