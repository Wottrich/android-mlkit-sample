package github.io.wottrich.mlkit.frame

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import github.io.wottrich.mlkit.R

class CircularFrameView constructor(
    context: Context,
    attrs: AttributeSet
) : View(context, attrs) {
    private var radius = 0f
    private var positionX = 0f
    private var positionY = 0f
    private var progressAngle = 0f

    private var radiusRatio = 2f
    private var circularBackground = Color.TRANSPARENT
    private var circularStrokeColor = Color.GRAY
    private var circularStroke = 4f
    private var progressStartAngle = 270f
    private var progressColor = Color.RED
    private var progressClockwise = true
    private var gradientEnabled = false

    private var circularPaint: Paint = Paint()
    private var borderPaint: Paint = Paint()
    private var progressPaint: Paint = Paint()
    private var facePaint: Paint = Paint()
    private var rectCircularArea: RectF? = null

    private var frameViewShape: RoundedCanvasShape = CircleShape()

    private var currentFacePoints: List<PointF> = listOf()

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CircularFrameView)
            .run {
                loadAttrs(this)
                recycle()
                init()
            }
    }

    private fun loadAttrs(typedArray: TypedArray) {
        circularBackground = typedArray.getColor(
            R.styleable.CircularFrameView_cfv_circleBackground,
            circularBackground
        )
        circularStroke =
            typedArray.getDimension(R.styleable.CircularFrameView_cfv_circleStroke, circularStroke)
        circularStrokeColor = typedArray.getColor(
            R.styleable.CircularFrameView_cfv_circleStrokeColor,
            circularStrokeColor
        )
        progressColor =
            typedArray.getColor(R.styleable.CircularFrameView_cfv_progressColor, progressColor)

        progressStartAngle = typedArray.getFloat(
            R.styleable.CircularFrameView_cfv_progressStartAngle,
            progressStartAngle
        )

        progressClockwise = typedArray.getBoolean(
            R.styleable.CircularFrameView_cfv_progressClockwise,
            progressClockwise
        )

        radiusRatio =
            typedArray.getFloat(R.styleable.CircularFrameView_cfv_radiusRatio, radiusRatio)

        gradientEnabled = typedArray.getBoolean(
            R.styleable.CircularFrameView_cfv_gradientEnabled,
            gradientEnabled
        )
    }

    private fun init() {
        circularPaint.apply {
            color = circularBackground
            if (circularBackground == Color.TRANSPARENT) {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            }
        }

        borderPaint.apply {
            color = circularStrokeColor
            strokeWidth = circularStroke
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        progressPaint.apply {
            color = progressColor
            strokeCap = Paint.Cap.ROUND
            strokeWidth = circularStroke
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateAttributes(w, h)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            updateAttributes(width, height)
            createFrame(it)
            createFacePoints(it)
        }
    }

    private fun createFacePoints(canvas: Canvas) {
        facePaint.apply {
            color = Color.RED
            strokeWidth = 10f
            style = Paint.Style.STROKE
        }

        canvas.drawPoint(positionX, positionY, facePaint)

        currentFacePoints.forEach {
            canvas.drawPoint(
                positionX + it.x,
                positionY + it.y,
                facePaint
            )
        }
    }

    private fun translationX(x: Float): Float {
        val width = getRectAreaWidth() ?: return x
        val height = getRectAreaHeight() ?: return x
        val aspectRadio = width / height
        val offset = (this.width / aspectRadio - this.height) / 2
        return this.width - (scale(x) - offset)
    }

    private fun translationY(y: Float): Float {
        val width = getRectAreaWidth() ?: return y
        val height = getRectAreaHeight() ?: return y
        val aspectRadio = width / height
        val offset = (this.height / aspectRadio - this.width) / 2
        return scale(y) - offset
    }

    private fun scale(imagePixel: Float): Float {
        val width = getRectAreaWidth() ?: return imagePixel
        val height = getRectAreaHeight() ?: return imagePixel
        return imagePixel * (width / height)
    }

    private fun getRectAreaWidth(): Float? {
        return getRectCircleArea()?.width().takeIf { it != null && it != 0f }
    }

    private fun getRectAreaHeight(): Float? {
        return getRectCircleArea()?.height().takeIf { it != null && it != 0f }
    }

    private fun createFrame(canvas: Canvas) {
        canvas.apply {
            rectCircularArea?.let {
                frameViewShape.drawCanvas(canvas, positionX, positionY, radius, circularPaint)
                frameViewShape.drawCanvas(canvas, positionX, positionY, radius, borderPaint)
                drawProgressArc(canvas, it)
            }
        }
    }

    fun setRoundedCanvasShape(roundedCanvasShape: RoundedCanvasShape) {
        frameViewShape = roundedCanvasShape
        frameViewShape.getRadiusRatio().let {
            radiusRatio = it
        }
        postInvalidate()
    }

//    fun setProgressColor(@ColorRes color: Int) {
//        progressPaint.color = context.getColorCompat(color)
//        invalidate()
//    }
//
//    fun setProgress(progress: Float) {
//        progressAngle = progress * MULTIPLIER_PROGRESS
//        invalidate()
//    }

    fun setCircleStroke(stroke: Float, @ColorInt strokeColor: Int) {
        borderPaint.apply {
            strokeWidth = stroke
            color = strokeColor
        }
        invalidate()
    }

    fun getRectCircleArea(): RectF? {
        return rectCircularArea
    }

    private fun updateAttributes(width: Int, height: Int) {
        radius = (width.coerceAtMost(height) / radiusRatio) - circularStroke

        positionY = paddingTop + height / POSITION_Y
        positionX = paddingStart + width / POSITION_X

        rectCircularArea = frameViewShape.getRectFArea(positionX, positionY, radius)
    }

    private fun drawProgressArc(canvas: Canvas, rectCircleArea: RectF) {
        val endAngle = getEndAngle()
        if (gradientEnabled) setGradient(endAngle)
        canvas.drawArc(
            rectCircleArea,
            progressStartAngle,
            endAngle,
            false,
            progressPaint
        )
    }

    private fun getEndAngle() = if (progressClockwise) progressAngle else -progressAngle

    private fun setGradient(endAngle: Float) {
        val colors = intArrayOf(Color.WHITE, progressPaint.color, progressPaint.color)
        val endPosition = endAngle / MAX_ANGLE
        val positions = floatArrayOf(0f, endPosition / MID_GRADIENT_FACTOR, endPosition)

        progressPaint.shader = SweepGradient(positionX, positionY, colors, positions).apply {
            Matrix().apply {
                postRotate(ROTATE, positionX, positionY)
                setLocalMatrix(this)
            }
        }
    }

    fun updatePoints(points: List<PointF>) {
        currentFacePoints = points
        invalidate()
    }

    companion object {
        const val POSITION_X = 2f
        const val POSITION_Y = 2f

        const val MULTIPLIER_PROGRESS = 3.6f
        const val MAX_ANGLE = 360f
        const val ROTATE = 267.7f
        const val MID_GRADIENT_FACTOR = 1.3f
    }
}