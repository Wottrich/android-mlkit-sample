package github.io.wottrich.mlkit

import com.google.mlkit.vision.face.Face

abstract class BaseFaceHandler : FaceHandler {
    protected var nextCanHandle = true

    override fun nextCanHandle(): Boolean {
        return nextCanHandle
    }

    override fun handle(faces: List<Face>, listener: FaceStateListener) {
    }
}