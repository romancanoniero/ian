package com.iyr.ian.utils.components.clock

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.iyr.ian.R
import com.iyr.ian.utils.px
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin


/**
 * TODO: document your custom view class.
 */
class ClockView : View {

    private var minDimension: Int = 0

    //   private val height = 0
    //   private  var width:Int = 0
    private var padding = 0
    private var fontSize = 0
    private val numeralSpacing = 0
    private var handTruncation = 0
    private var hourHandTruncation: Int = 0
    private var radius = 0
    private var paint: Paint? = null
    private var isInit = false
    private val numbers = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
    private val rect: Rect = Rect()


    private var textRect: Rect = Rect()


    //  private var _exampleString: String? = null // TODO: use a default from R.string...
    //   private var _exampleColor: Int = Color.RED // TODO: use a default from R.color...
    private var _backgroundColor: Int = Color.RED // TODO: use a default from R.color...
    private var _borderColor: Int = Color.WHITE // TODO: use a default from R.color...
    private var _borderWidth: Float = 5f

    //   private var _exampleDimension: Float = 0f // TODO: use a default from R.dimen...
    private var _maxValue: Float = 0f // TODO: use a default from R.dimen...
    private var _value: Float = 0f // TODO: use a default from R.dimen...

    private var _textSize: Float = 12f // TODO: use a default from R.dimen...

    private lateinit var textPaint: TextPaint
    private var _textBottomMargin: Float = 0f
    private var _textTopMargin: Float = 0f
    private var _progressColor: Int = Color.WHITE
/*
    var backgroundColor: Int
        get() = _backgroundColor
        set(value) {
            _backgroundColor = value
            invalidateTextPaintAndMeasurements()
        }
*/


    var textSize: Float
        get() = _textSize
        set(value) {
            _textSize = value
            invalidateTextPaintAndMeasurements()
        }

    private var textBottomMargin: Float
        get() = _textBottomMargin
        set(value) {
            _textBottomMargin = value
            invalidateTextPaintAndMeasurements()
        }

    private var textTopMargin: Float
        get() = _textTopMargin
        set(value) {
            _textTopMargin = value
            invalidateTextPaintAndMeasurements()
        }


    var value: Float
        get() = _value
        set(value) {
            _value = value
            invalidateTextPaintAndMeasurements()
        }

    var maxValue: Float
        get() = _maxValue
        set(value) {
            _maxValue = value
            invalidateTextPaintAndMeasurements()
        }

    private var progressColor: Int
        get() = _progressColor
        set(value) {
            _progressColor = value
            invalidateTextPaintAndMeasurements()
        }


    var borderWidth: Float
        get() = _borderWidth
        set(value) {
            _borderWidth = value
            invalidateTextPaintAndMeasurements()
        }


    /**
     * In the example view, this drawable is drawn above the text.
     */
    var exampleDrawable: Drawable? = null

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes

        //   this.setBackgroundColor(Color.YELLOW);

        val a = context.obtainStyledAttributes(
            attrs, R.styleable.ClockView, defStyle, 0
        )

        _backgroundColor = a.getColor(
            R.styleable.ClockView_backgroundColor,
            Color.WHITE
        )

        _borderColor = a.getColor(
            R.styleable.ClockView_borderColor,
            Color.RED
        )

        _borderWidth = a.getDimension(
            R.styleable.ClockView_borderWidth,
            _borderWidth
        )


        _maxValue = a.getFloat(
            R.styleable.ClockView_maxValue,
            220f
        )

        _value = a.getFloat(
            R.styleable.ClockView_value,
            50f
        )

        _progressColor = a.getColor(
            R.styleable.ClockView_progressColor,
            progressColor
        )


        _textTopMargin = a.getDimension(
            R.styleable.ClockView_textTopMargin,
            0f
        )

        _textBottomMargin = a.getDimension(
            R.styleable.ClockView_textBottomMargin,
            0f
        )

        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        /*
         _exampleDimension = a.getDimension(
             R.styleable.ClockView_exampleDimension,
             exampleDimension
         )
         */
/*
        if (a.hasValue(R.styleable.ClockView_exampleDrawable)) {
            exampleDrawable = a.getDrawable(
                R.styleable.ClockView_exampleDrawable
            )
            exampleDrawable?.callback = this
        }
*/
        _textSize = a.getDimension(
            R.styleable.ClockView_textSize,
            _textSize
        )


        a.recycle()

        // Set up a default TextPaint object
        textPaint = TextPaint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            textAlign = Paint.Align.LEFT
        }

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements()
    }

    private fun initClock() {

        padding = numeralSpacing + 20
        fontSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 13f,
            resources.displayMetrics
        ).toInt()

        paint = Paint()

        isInit = true
    }

    private fun calculations() {


        val counterSectionHeight = _textTopMargin + _textBottomMargin + textRect.height()
        val min = min(height - counterSectionHeight.toInt(), width)

        minDimension = min - padding

        radius = (((min) / 2) - padding)
        handTruncation = min / 20
        hourHandTruncation = min / 7
/*
        Log.d("CLOCK_VIEW ", "HEIGHT = $height")
        Log.d("CLOCK_VIEW ", "WIDTH = $width")
        Log.d("CLOCK_VIEW ", "RADIUS = $radius")
*/
    }

    private fun invalidateTextPaintAndMeasurements() {
        /*
         textPaint.let {
             it.textSize = exampleDimension
             it.color = exampleColor
             textWidth = it.measureText(exampleString)
             textHeight = it.fontMetrics.bottom
         }
         */
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isInit) {
            initClock()
        }
        canvas.drawColor(Color.argb(0, 255, 255, 255))
        drawRemainingTime(canvas)
        calculations()
        drawCircleFullFill(canvas)
        drawCircle(canvas)
        drawCenter(canvas)
        //    drawNumeral(canvas);
        drawHands(canvas)
        drawTicks(canvas)

        postInvalidateDelayed(500)
    }

    private fun drawRemainingTime(canvas: Canvas) {

//        val mScaledDensity = context.resources.displayMetrics.scaledDensity

        var textToShow = ""

        val timeDif = ((_maxValue * 60 * 1000) - (_value * 60 * 1000)).toLong()
//        Log.d("TIME_DIFF", timeDif.toString())
        textToShow = String.format(
            "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(timeDif),
            TimeUnit.MILLISECONDS.toMinutes(timeDif) - TimeUnit.HOURS.toMinutes(
                TimeUnit.MILLISECONDS.toHours(
                    timeDif
                )
            ),
            TimeUnit.MILLISECONDS.toSeconds(timeDif) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(
                    timeDif
                )
            )
        )


        val paint = Paint()
        paint.color = Color.BLACK
        paint.textSize = _textSize
        textRect = Rect()
        paint.getTextBounds(textToShow, 0, textToShow.length, textRect)

        //     Log.d("CLOCK_VIEW", textRect.toString())
        val textWidth = textRect.width()
        var textHeight = textRect.height()
        val leftMargin = (canvas.width - textWidth) / 2
        canvas.drawText(
            textToShow, leftMargin.toFloat(),
            (this.height - _textBottomMargin), paint
        )


    }


    private fun drawTicks(canvas: Canvas) {

        //    var r = (Math.min(width, height) / 2 - padding).toDouble()
        val r = radius.toDouble() + (_borderWidth) //((padding + _borderWidth)/2 )

        val cX = (width / 2).toDouble()
        val cY = ((height - textTopMargin - textBottomMargin - textRect.height()) / 2).toDouble()
        val tickLen = (((radius * .10) * 100) / 50).roundToInt() // short tick

        val medTickLen = (((radius * .15) * 100) / 40).roundToInt() // at 5-minute intervals

        val longTickLen = (((radius * .15) * 100) / 30).roundToInt() // at the quarters

        val tickColor = 0xCCCCCC

        paint!!.reset()
        paint!!.color = Color.BLACK
        paint!!.strokeWidth = 1.px.toFloat()
        paint!!.style = Paint.Style.STROKE
        paint!!.isAntiAlias = true

        for (i in 1..60) {
            // default tick length is short
            var len: Int = tickLen
            if (i % 15 == 0) {
                // Longest tick on quarters (every 15 ticks)
                len = longTickLen
            } else if (i % 5 == 0) {
                // Medium ticks on the '5's (every 5 ticks)
                len = medTickLen
            }
            val di = i.toDouble() // tick num as double for easier math

            // Get the angle from 12 O'Clock to this tick (radians)
            val angleFrom12 = di / 60.0 * 2.0 * Math.PI

            // Get the angle from 3 O'Clock to this tick
            // Note: 3 O'Clock corresponds with zero angle in unit circle
            // Makes it easier to do the math.
            val angleFrom3 = Math.PI / 2.0 - angleFrom12

            // Move to the outer edge of the circle at correct position
            // for this tick.


            canvas.drawLine(
                (cX + cos(angleFrom3) * r).toFloat(),
                (cY - sin(angleFrom3) * r).toFloat(),
                (cX + cos(angleFrom3) * (r - len)).toFloat(),
                (cY - sin(angleFrom3) * (r - len)).toFloat(),
                paint!!
            )
            /*
            ticksPath.moveTo(
                (cX + Math.cos(angleFrom3) * r) as Float,
                (cY - Math.sin(angleFrom3) * r) as Float
            )

            // Draw line inward along radius for length of tick mark
            ticksPath.lineTo(
                (cX + Math.cos(angleFrom3) * (r - len)) as Float,
                (cY - Math.sin(angleFrom3) * (r - len)) as Float
            )

             */
        }

// Draw the full shape onto the graphics context.

// Draw the full shape onto the graphics context.
        //  g.setColor(tickColor)
        //  g.drawShape(ticksPath, tickStroke)
    }

    private fun drawHand(canvas: Canvas, loc: Double, isHour: Boolean) {
        val angle = Math.PI * loc / 30 - Math.PI / 2
        val handRadius =
            if (isHour) radius - handTruncation - hourHandTruncation else radius - handTruncation
        canvas.drawLine(
            (width / 2).toFloat(), (height / 2).toFloat(),
            (width / 2 + cos(angle) * handRadius).toFloat(),
            (height / 2 + sin(angle) * handRadius).toFloat(),
            paint!!
        )
    }

    private fun drawElapsedTime(canvas: Canvas, loc: Double) {
        val angle = Math.PI * loc / 30 - Math.PI / 2
        val handRadius = radius - textTopMargin - textBottomMargin
        canvas.drawLine(
            (width / 2).toFloat(),
            (height / 2).toFloat() - textTopMargin - textBottomMargin,
            (width / 2 + cos(angle) * handRadius).toFloat(),
            (height / 2 + sin(angle) * handRadius).toFloat(),
            paint!!
        )

        //       Log.d("CLOCK_VIEW_textTopMargin", textTopMargin.toString())
        //       Log.d("CLOCK_VIEW_textBottomMargin", textBottomMargin.toString())
    }


    private fun drawHands(canvas: Canvas) {
        val c: Calendar = Calendar.getInstance()
        var hour: Float = c.get(Calendar.HOUR_OF_DAY).toFloat()
        hour = if (hour > 12) hour - 12 else hour
        val gradians = (_value * 360) / _maxValue
        val p = Paint()
        p.color = _progressColor
        /*
        val rectF = RectF(
            0F + padding,
            0F + (padding /2),
            canvas.width.toFloat() - padding,
            canvas.height.toFloat() - padding - textTopMargin
        )
        */

        val leftMargin = (width - minDimension) / 2
        val rectF = RectF(
            0F + leftMargin,
            0F + (padding / 2),
            (width - leftMargin).toFloat(),
            minDimension.toFloat() + (padding / 2)
        )
        canvas.drawArc(rectF, 270F, gradians, true, p)


/*
        for (i in 0..gradians)
{
        drawElapsedTime(canvas, ((i) ).toDouble())
        }

 */
        /*
         drawHand(canvas, ((hour + c.get(Calendar.MINUTE) / 60) * 5f).toDouble(), true)
         drawHand(canvas, c.get(Calendar.MINUTE).toDouble(), false)
         drawHand(canvas, c.get(Calendar.SECOND).toDouble(), false)
         */
    }

    private fun drawNumeral(canvas: Canvas) {
        return
        paint!!.textSize = fontSize.toFloat()
        for (number in numbers) {
            val tmp = number.toString()
            paint!!.getTextBounds(tmp, 0, tmp.length, rect)
            val angle = Math.PI / 6 * (number - 3)
            val x = (width / 2 + cos(angle) * radius - rect.width() / 2).toInt()
            val y =
                ((height - textTopMargin - textBottomMargin - textRect.height()) / 2 + sin(
                    angle
                ) * radius + rect.height() / 2).toInt()
            canvas.drawText(tmp, x.toFloat(), y.toFloat(), paint!!)
        }
    }

    private fun drawCenter(canvas: Canvas) {
        paint!!.style = Paint.Style.FILL
        canvas.drawCircle(
            (width / 2).toFloat(),
            ((height - textTopMargin - textBottomMargin - textRect.height()) / 2),
            12f,
            paint!!
        )
    }

    private fun drawCircle(canvas: Canvas) {
        paint!!.reset()
        paint!!.color = _borderColor
        paint!!.strokeWidth = _borderWidth
        paint!!.style = Paint.Style.STROKE
        paint!!.isAntiAlias = true
        canvas.drawCircle(
            (width / 2).toFloat(),
            ((height - textTopMargin - textBottomMargin - textRect.height()) / 2),
            (radius + padding - 10).toFloat(),
            paint!!
        )
    }

    private fun drawCircleFullFill(canvas: Canvas) {
        paint!!.reset()
        paint!!.color = _backgroundColor
        paint!!.strokeWidth = _borderWidth
        paint!!.style = Paint.Style.FILL
        paint!!.isAntiAlias = true
        canvas.drawCircle(
            (width / 2).toFloat(),
            ((height - textTopMargin - textBottomMargin - textRect.height()) / 2),
            (radius + padding - 10).toFloat(),
            paint!!
        )
    }
}