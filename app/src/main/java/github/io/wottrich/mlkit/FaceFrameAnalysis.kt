package github.io.wottrich.mlkit

import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import github.io.wottrich.mlkit.handlers.ExpectedFaceRegion
import io.fotoapparat.preview.Frame
import io.fotoapparat.preview.FrameProcessor

open class FaceFrameAnalysis(
    private var listener: FaceStateListener,
    private var expectedFaceRegion: ExpectedFaceRegion = ExpectedFaceRegion()
) : FrameProcessor {

    private val faceProcessor by lazy {
        buildFaceProcessor()
    }

    override fun process(frame: Frame) {
        updateExpectedFaceRegion(frame)

        val image = InputImage.fromByteArray(
            frame.image,
            frame.size.width,
            frame.size.height,
            frame.rotation.getRotationCompensation(),
            InputImage.IMAGE_FORMAT_NV21
        )

        faceProcessor.process(image, listener)
    }

    fun close() {
        faceProcessor.close()
    }

    fun setExpectedFaceRegion(faceRoi: Rect, surfaceWidth: Int, surfaceHeight: Int) {
        expectedFaceRegion.faceRoi = faceRoi
        expectedFaceRegion.surfaceWidth = surfaceWidth
        expectedFaceRegion.surfaceHeight = surfaceHeight
    }

    fun getExpectedFaceRegion(): ExpectedFaceRegion {
        return expectedFaceRegion
    }

    protected open fun buildFaceProcessor(): FaceProcessor {
        return FaceProcessor.build()
    }

    private fun updateExpectedFaceRegion(frame: Frame) {
        var frameSize = frame.size
        if (frameSize.width > frameSize.height) {
            frameSize = frameSize.flipDimensions()
        }

        expectedFaceRegion.frameWidth = frameSize.width
        expectedFaceRegion.frameHeight = frameSize.height
    }
}

fun Int.getRotationCompensation(): Int {
    return (360 - this) % 360
}