package github.io.wottrich.mlkit.handlers

import android.graphics.PointF
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceLandmark
import github.io.wottrich.mlkit.BaseFaceHandler
import github.io.wottrich.mlkit.FaceState
import github.io.wottrich.mlkit.FaceStateListener

interface FacePointListener {
    fun updateFacePoints(points: List<PointF>)
}

class NumberFaceHandler(
    private val canHandleNextOnlyWithOneFace: Boolean = false,
    private val facePointListener: FacePointListener? = null
) : BaseFaceHandler() {

    override fun handle(faces: List<Face>, listener: FaceStateListener) {
        val state = when {
            faces.isEmpty() -> {
                FaceState.FaceCount.NoFace
            }
            moreThanOneFace(faces) -> {
                FaceState.FaceCount.MoreThatOneFace
            }
            else -> {
                FaceState.FaceCount.FaceDetected
            }
        }

        listener.onFaceStateEvent(state)

        if (canHandleNextOnlyWithOneFace) {
            nextCanHandle = state == FaceState.FaceCount.FaceDetected
        }
    }

    private fun moreThanOneFace(faces: List<Face>): Boolean {
        return faces.distinctBy { it.trackingId }.size != 1
    }
}