package github.io.wottrich.mlkit

interface FaceStateListener {
    fun onFaceStateEvent(faceState: FaceState, errorMessage: String? = null)
}