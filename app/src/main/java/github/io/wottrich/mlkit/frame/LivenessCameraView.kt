package github.io.wottrich.mlkit.frame

import android.content.Context
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import github.io.wottrich.mlkit.R

class LivenessCameraView @kotlin.jvm.JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1
) : RelativeLayout(context, attrs, defStyleAttr) {

    private var progress = 0f
    private lateinit var feedbackImage: ImageView
    private lateinit var circularFrameView: CircularFrameView

    init {
        init()
    }

    fun increaseProgress(value: Float) {
        progress += value
        //circularFrameView.setProgress(progress)
    }

    fun resetState() {
        hideFeedbackImage()

        progress = 0f
        //setProgress(progress, R.color.ifood_red)
    }

//    fun success() {
//        setProgress(PROGRESS_COMPLETED, R.color.positive)
//        showAnimatedImage(R.drawable.ic_check_on_24dp)
//    }
//
//    fun fail() {
//        setProgress(PROGRESS_COMPLETED, R.color.medium_purple)
//        showAnimatedImage(R.drawable.ic_error_purple_16dp)
//    }

    fun getRectCircleArea(): RectF? {
        return circularFrameView.getRectCircleArea()
    }

    fun setRoundedCanvasShape(roundedCanvasShape: RoundedCanvasShape) {
        circularFrameView.setRoundedCanvasShape(roundedCanvasShape)
        postInvalidate()
    }

    fun updatePoints(points: List<PointF>) {
        circularFrameView.updatePoints(points)
        postInvalidate()
    }

    private fun init() {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.content_liveness_camera_view, this)

        feedbackImage = view.findViewById(R.id.feedbackImage)
        circularFrameView = view.findViewById(R.id.circularFrameView)
    }

//    private fun setProgress(progress: Float, @ColorRes color: Int) {
//        circularFrameView.setProgress(progress)
//        circularFrameView.setProgressColor(color)
//    }

    private fun showAnimatedImage(@DrawableRes resId: Int) {
        circularFrameView.let {
            hideFeedbackImage()
            getRectCircleArea()?.let {
                feedbackImage.setImageResource(resId)
                feedbackImage.x = (it.left + it.right) / 2 - (feedbackImage.width / 2)
                feedbackImage.y = (it.top + it.bottom) / 2 - (feedbackImage.height / 2)
                feedbackImage.animate()
                    .scaleXBy(1f)
                    .scaleYBy(1f)
                    .setDuration(FEEDBACK_IMAGE_ANIMATION_DURATION)
            }
        }
    }

    private fun hideFeedbackImage() {
        feedbackImage.animate().cancel()
        feedbackImage.scaleX = 0f
        feedbackImage.scaleY = 0f
    }

    companion object {
        const val PROGRESS_COMPLETED = 100F
        const val FEEDBACK_IMAGE_ANIMATION_DURATION = 250L
    }
}