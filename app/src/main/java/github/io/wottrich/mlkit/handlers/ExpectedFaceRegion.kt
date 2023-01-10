package github.io.wottrich.mlkit.handlers

import android.graphics.Rect
import com.google.mlkit.vision.face.Face
import github.io.wottrich.mlkit.BaseFaceHandler
import github.io.wottrich.mlkit.FaceState
import github.io.wottrich.mlkit.FaceStateListener

data class ExpectedFaceRegion(
    var faceRoi: Rect = Rect(),
    var surfaceWidth: Int = 0,
    var surfaceHeight: Int = 0,
    var frameWidth: Int = 0,
    var frameHeight: Int = 0
) {
    internal fun isFaceInside(faceRect: Rect): Boolean {
        if (frameWidth == 0 || frameHeight == 0) return false
        if (surfaceWidth == 0 || surfaceHeight == 0) return false

        val roiLeft = faceRoi.left * frameWidth / surfaceWidth
        val roiRight = faceRoi.right * frameWidth / surfaceWidth
        val roiTop = faceRoi.top * frameHeight / surfaceHeight
        val roiBottom = faceRoi.bottom * frameHeight / surfaceHeight

        return faceRect.left >= roiLeft &&
                faceRect.right <= roiRight &&
                faceRect.top >= roiTop &&
                faceRect.bottom <= roiBottom
    }

    internal fun isValid(): Boolean {
        return (
                !faceRoi.isEmpty &&
                        surfaceWidth > 0 &&
                        surfaceHeight > 0 &&
                        frameWidth > 0 &&
                        frameHeight > 0
                )
    }
}

class ExpectedFaceRegionHandler(
    private val canHandleNextIfInsideRegion: Boolean,
    private val expectedFaceRegion: ExpectedFaceRegion
) : BaseFaceHandler() {

    override fun handle(faces: List<Face>, listener: FaceStateListener) {
        faces.forEach {
            val insideRegion = hasFaceExpectedRegion(it)
            if (insideRegion) {
                listener.onFaceStateEvent(FaceState.Region.FaceInsideRegion)
            } else {
                listener.onFaceStateEvent(FaceState.Region.FaceOutsideRegion)
            }

            if (canHandleNextIfInsideRegion) {
                nextCanHandle = insideRegion
            }
        }
    }

    private fun hasFaceExpectedRegion(face: Face): Boolean {
        if (!expectedFaceRegion.isValid()) return true
        return expectedFaceRegion.isFaceInside(face.boundingBox)
    }
}