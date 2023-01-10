package github.io.wottrich.mlkit

import com.google.mlkit.vision.face.Face

interface FaceHandler {
    fun nextCanHandle(): Boolean
    fun handle(faces: List<Face>, listener: FaceStateListener)
}