package github.io.wottrich.mlkit

import android.graphics.Bitmap
import android.view.View
import io.fotoapparat.preview.FrameProcessor
import java.io.File
import java.io.Serializable

enum class CameraType : Serializable {
    FRONT, BACK
}

interface CameraStrategy {
    fun setupCamera(renderer: View, type: CameraType, frameProcessor: FrameProcessor? = null)
    fun startCamera()
    fun stopCamera()
    fun takePhoto(listener: CameraListener)
    fun takePhoto(file: File, callback: (Result<Unit>) -> Unit)
    fun flipCamera()
    fun getCurrentCameraOrientation(): CameraType
}

interface CameraListener {
    fun onCameraResult(bitmap: Bitmap?, rotationDegrees: Int)
}