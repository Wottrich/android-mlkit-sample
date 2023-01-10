package github.io.wottrich.mlkit

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import github.io.wottrich.mlkit.handlers.*

class FaceProcessor private constructor(
    private val faceDetector: FaceDetector,
    private val faceHandlers: List<FaceHandler>
) {

    fun process(inputImage: InputImage, listener: FaceStateListener) {
        faceDetector.process(inputImage)
            .addOnSuccessListener { faces ->
                faceHandlers.forEach { handler ->
                    handler.handle(faces, listener)
                    if (!handler.nextCanHandle()) {
                        return@addOnSuccessListener
                    }
                }
            }
            .addOnFailureListener {
                listener.onFaceStateEvent(FaceState.Error, it.message)
            }
    }

    fun close() {
        faceDetector.close()
    }

    companion object {

        private const val MIN_FACE_SIZE = 0.30f

        private val handlers = LinkedHashSet<FaceHandler>()

        private val optionsBuilder = FaceDetectorOptions.Builder()
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setMinFaceSize(MIN_FACE_SIZE)

        //TODO Liveness here

        fun eulerMovement(
            facePointListener: FacePointListener? = null
        ) = apply {
            handlers += EulerMovementFaceHandler(facePointListener)
        }

        fun facesCount(
            stopIfNotHaveOnlyOneFace: Boolean = false,
            facePointListener: FacePointListener? = null
        ) = apply {
            handlers += NumberFaceHandler(stopIfNotHaveOnlyOneFace, facePointListener)
            optionsBuilder.enableTracking()
        }

        fun expectedFaceRegion(
            stopIfFaceIsOutsideRegion: Boolean = true,
            expectedFaceRegion: ExpectedFaceRegion
        ) = apply {
            handlers += ExpectedFaceRegionHandler(stopIfFaceIsOutsideRegion, expectedFaceRegion)
        }

        fun build(): FaceProcessor {
            return FaceProcessor(
                FaceDetection.getClient(optionsBuilder.build()),
                handlers.toList()
            )
        }
    }

}