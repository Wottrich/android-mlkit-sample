package github.io.wottrich.mlkit

import github.io.wottrich.mlkit.handlers.ExpectedFaceRegion
import github.io.wottrich.mlkit.handlers.FacePointListener

class LivenessFrameAnalysis(
    listener: FaceStateListener,
    val pointListener: FacePointListener? = null,
    expectedFaceRegion: ExpectedFaceRegion = ExpectedFaceRegion()
) : FaceFrameAnalysis(listener, expectedFaceRegion) {

    override fun buildFaceProcessor(): FaceProcessor {
        return FaceProcessor
            .expectedFaceRegion(stopIfFaceIsOutsideRegion = true, getExpectedFaceRegion())
            .facesCount(stopIfNotHaveOnlyOneFace = true, facePointListener = pointListener)
            .eulerMovement(facePointListener = pointListener)
            //.smile()
            //.eyesBlinked()
            //.mouthMoved()
            .build()
    }
}