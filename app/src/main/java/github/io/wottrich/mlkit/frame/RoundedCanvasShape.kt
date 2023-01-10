package github.io.wottrich.mlkit.frame

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

interface RoundedCanvasShape {
    /**
     * Draw your final paint using this function.
     * To draw something, you need 4 basic components:
     * A Bitmap to hold the pixels,
     * a Canvas to host the draw calls (writing into the bitmap),
     * a drawing primitive (e.g. Rect, Path, text, Bitmap),
     * and a paint (to describe the colors and styles for the drawing).
     *
     * @param canvas - To host the draw calls
     * @param cx – The x-coordinate of the center of the circle to be drawn
     * @param cy – The y-coordinate of the center of the circle to be drawn
     * @param radius – The radius of the circle to be drawn
     * @param paint – The paint used to draw the circle
     */
    fun drawCanvas(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        paint: Paint
    )

    /**
     * @return Radius ratio of your rounded draw
     */
    fun getRadiusRatio(): Float

    /**
     * @param cx – The x-coordinate of the center of the circle to be drawn
     * @param cy – The y-coordinate of the center of the circle to be drawn
     * @param radius – The radius of the circle to be drawn
     * @return area of your rounded draw
     */
    fun getRectFArea(cx: Float, cy: Float, radius: Float): RectF
}

class CircleShape(
    private val radiusRatio: Float = DEFAULT_RADIUS_RATIO
) : RoundedCanvasShape {
    override fun drawCanvas(canvas: Canvas, cx: Float, cy: Float, radius: Float, paint: Paint) {
        canvas.drawCircle(
            cx, cy, radius, paint
        )
    }

    override fun getRectFArea(cx: Float, cy: Float, radius: Float): RectF {
        return RectF(cx - radius, cy - radius, cx + radius, cy + radius)
    }

    override fun getRadiusRatio(): Float {
        return radiusRatio
    }

    companion object {
        private const val DEFAULT_RADIUS_RATIO = 2.3f
    }
}

class OvalShape(
    private val ovalCircumference: Float = DEFAULT_OVAL_CIRCUMFERENCE,
    private val radiusRatio: Float = DEFAULT_RADIUS_RATIO
) : RoundedCanvasShape {
    override fun drawCanvas(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        paint: Paint
    ) {
        val ovalArea = getRectFArea(cx, cy, radius)
        canvas.drawOval(ovalArea, paint)
    }

    override fun getRectFArea(cx: Float, cy: Float, radius: Float): RectF {
        val ovalRadius = getOvalRadius(radius)
        return RectF(
            cx - ovalRadius,
            cy - radius,
            cx + ovalRadius,
            cy + radius
        )
    }

    private fun getOvalRadius(radius: Float): Float {
        return radius / ovalCircumference
    }

    override fun getRadiusRatio(): Float {
        return radiusRatio
    }

    companion object {
        private const val DEFAULT_OVAL_CIRCUMFERENCE = 1.5f
        private const val DEFAULT_RADIUS_RATIO = 2f
    }
}