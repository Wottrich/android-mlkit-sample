package github.io.wottrich.mlkit

import android.content.Context
import android.view.View
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.exception.FileSaveException
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.preview.FrameProcessor
import io.fotoapparat.selector.*
import io.fotoapparat.view.CameraRenderer
import java.io.File

class FotoapparatCamera constructor(private val context: Context) :
    CameraStrategy {
    private var fotoapparat: Fotoapparat? = null
    private var currentLens: CameraType = CameraType.BACK

    override fun setupCamera(renderer: View, type: CameraType, frameProcessor: FrameProcessor?) {
        require(renderer is CameraRenderer) { "The renderer view need be a CameraRenderer view" }
        updateCurrentLens(type)
        fotoapparat = Fotoapparat.with(context)
            .into(renderer as CameraRenderer)
            .lensPosition(if (currentLens == CameraType.BACK) back() else front())
            .previewScaleType(ScaleType.CenterCrop)
            .frameProcessor(frameProcessor)
            .focusMode(
                firstAvailable(
                    continuousFocusPicture(),
                    autoFocus(),
                    edof(),
                    fixed(),
                    infinity(),
                    macro(),
                    nothing()
                )
            )
            .flash(off())
            .jpegQuality(manualJpegQuality(80))
            .photoResolution(standardRatio(highestResolution()))
            .sensorSensitivity(highestSensorSensitivity())
            .build()
    }

    private fun updateCurrentLens(cameraType: CameraType) {
        currentLens = cameraType
    }

    override fun startCamera() {
        fotoapparat?.start()
    }

    override fun stopCamera() {
        fotoapparat?.stop()
    }

    override fun takePhoto(listener: CameraListener) {
        fotoapparat?.run {
            takePicture().toBitmap().whenAvailable { bitmapPhoto ->
                bitmapPhoto?.bitmap?.let {
                    listener.onCameraResult(
                        it,
                        bitmapPhoto.run { rotationDegrees }
                    )
                }
            }
        }
    }

    override fun takePhoto(file: File, callback: (Result<Unit>) -> Unit) {
        try {
            fotoapparat?.run {
                takePicture().saveToFile(file).whenAvailable {
                    callback(Result.success(Unit))
                }
            }
        } catch (exception: FileSaveException) {
            callback(Result.failure(exception))
        }
    }

    override fun flipCamera() {
        val lensCamera: LensPositionSelector
        if (currentLens == CameraType.BACK) {
            lensCamera = front()
            updateCurrentLens(CameraType.FRONT)
        } else {
            lensCamera = back()
            updateCurrentLens(CameraType.BACK)
        }
        fotoapparat?.switchTo(lensCamera, CameraConfiguration())
    }

    override fun getCurrentCameraOrientation(): CameraType {
        return currentLens
    }
}