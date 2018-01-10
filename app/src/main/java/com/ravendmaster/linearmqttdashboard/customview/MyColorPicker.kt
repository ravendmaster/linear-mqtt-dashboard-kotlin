package com.ravendmaster.linearmqttdashboard.customview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v7.widget.SearchView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SoundEffectConstants
import android.view.View
import android.view.animation.AnimationUtils

import com.ravendmaster.linearmqttdashboard.Log
import com.ravendmaster.linearmqttdashboard.R

class MyColorPicker(context: Context, attrs: AttributeSet) : View(context, attrs) {

    internal var listener: View.OnClickListener? = null

    internal var stopAnimation: Boolean = false

    var color: Int
        get() = currentColor
        set(selectedColor) {

            var minDif = 0xffff
            var minDifPos = 0
            var pos = 0
            for (color in MyColors.colors) {
                val newDif = (Math.abs(Color.red(color) - Color.red(selectedColor))
                        + Math.abs(Color.green(color) - Color.green(selectedColor))
                        + Math.abs(Color.blue(color) - Color.blue(selectedColor)))
                if (newDif < minDif) {
                    minDif = newDif
                    minDifPos = pos
                }
                pos++
            }

            this.currentColor = MyColors.getColorByIndex(minDifPos)!!
        }

    internal var currentColor = 0

    internal val color_rows = 4

    var X: Int = 0 // Переменные, доступные снаружи
    var Y: Int = 0

    internal var p = Paint()

    private val animator = object : Runnable {
        override fun run() {
            val scheduleNewFrame = false
            val now = AnimationUtils.currentAnimationTimeMillis()

            val newFlashState = now / 500 % 2 == 0L
            if (flashState != newFlashState) {
                flashState = newFlashState
            }

            if (!stopAnimation) {
                postDelayed(this, 500)
            }
            invalidate()
            //Log.d(getClass().getName(), "refresh!!!");
        }
    }

    internal var flashState: Boolean = false

    override fun setOnClickListener(listener: View.OnClickListener?) {
        this.listener = listener
    }

    init {

        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.RGBLEDView,
                0, 0)

        try {
            //colorLight = a.getInteger(R.styleable.RGBLEDView_colorLight, Color.BLUE);
        } finally {
            a.recycle()
        }

    }

    fun startAnimation() {
        stopAnimation = false
        removeCallbacks(animator)
        post(animator)
    }


    fun stopAnimation() {
        stopAnimation = true
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
            MotionEvent.ACTION_DOWN -> Log.d(javaClass.name, "DOWN")

            MotionEvent.ACTION_CANCEL -> {
            }

            MotionEvent.ACTION_UP -> {
                Log.d(javaClass.name, "UP, " + event.action)

                val left = left
                val right = right
                val top = top
                val bottom = bottom

                val strokeWidth = 0
                val button_width = right - left - strokeWidth
                val button_height = bottom - top - strokeWidth
                val oneBlockWidth = button_width / 5
                val oneBlockHeight = button_height / color_rows

                val row = Y / oneBlockHeight
                val col = X / oneBlockWidth
                val colorIndex = row * 5 + col

                Log.d(javaClass.name, "row=$row col=$col")
                if (colorIndex >= 0 && colorIndex < MyColors.colors.size) {
                    currentColor = MyColors.getColorByIndex(colorIndex)!!

                    //if (mMyColorPickerEventListener != null) mMyColorPickerEventListener.OnColorSelected(this, MyColors.getColorByIndex(colorIndex));
                    invalidate()
                    requestLayout()
                    playSoundEffect(SoundEffectConstants.CLICK)
                    if (listener != null) {
                        listener!!.onClick(this)
                    }
                }
            }
        }

        return true
    }

    override fun onDraw(canvas: Canvas) {

        val left = left
        val right = right
        val top = top
        val bottom = bottom

        val strokeWidth = 0
        val button_width = right - left - strokeWidth
        val button_height = bottom - top - strokeWidth
        val oneBlockWidth = button_width / 5
        val oneBlockHeight = button_height / color_rows

        val button_space = oneBlockWidth / 16

        p.strokeWidth = button_space.toFloat()
        for (row in 0 until color_rows) {
            for (col in 0..4) {
                val color = MyColors.getColorByIndex(col + row * 5)!!
                p.color = color

                val ydisp = row * oneBlockHeight

                p.style = Paint.Style.FILL

                canvas.drawRect((col * oneBlockWidth + button_space / 2).toFloat(), (ydisp + button_space / 2).toFloat(), (col * oneBlockWidth + oneBlockWidth - button_space / 2).toFloat(), (ydisp + oneBlockHeight - button_space / 2).toFloat(), p)

                if (color == currentColor) {
                    //primary_paint.setColor(0x80FFFFFF);
                    //int size=50;
                    //canvas.drawCircle(size + col * oneBlockWidth + button_space, size + ydisp + button_space, size, primary_paint);
                    //Rect rect=new Rect(0,0,100,100);

                    p.style = Paint.Style.STROKE

                    if (flashState) {
                        p.color = -0x1000000
                    } else {
                        p.color = -0x1
                    }
                    canvas.drawRect((col * oneBlockWidth + button_space - 1).toFloat(), (ydisp + button_space - 1).toFloat(), (col * oneBlockWidth + oneBlockWidth - button_space).toFloat(), (ydisp + oneBlockHeight - button_space).toFloat(), p)
                }
            }
        }
        return
    }


    fun tick() {}

}
