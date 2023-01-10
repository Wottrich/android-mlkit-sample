package github.io.wottrich.mlkit

import androidx.annotation.StringRes

sealed class FaceState(open val name: String) {
    object Error : FaceState("Error")
    sealed class FaceCount(override val name: String) : FaceState(name) {
        object NoFace : FaceState("NoFace")
        object FaceDetected : FaceState("FaceDetected")
        object MoreThatOneFace : FaceState("MoreThatOneFace")
    }
    sealed class Movement(override val name: String) : FaceState(name) {
        object Blinked : FaceState("Blinked")
        object Smile : FaceState("Smile")
        object MouthMoved : FaceState("MouthMoved")
        sealed class EulerMovement(override val name: String) : Movement(name) {
            object RightMovementSuccess : EulerMovement("EulerRightMovement")
            object LeftMovementSuccess : EulerMovement("EulerLeftMovement")
            object UpMovementSuccess : EulerMovement("EulerUpMovement")
            object DownMovementSuccess : EulerMovement("EulerDownMovement")
            object AllMovementSuccess : EulerMovement("AllMovementSuccess")
        }
    }
    sealed class Region(override val name: String) : FaceState(name) {
        object FaceInsideRegion : FaceState("FaceInsideRegion")
        object FaceOutsideRegion : FaceState("FaceOutsideRegion")
    }
    data class Information(@StringRes val message: Int) : FaceState("Information")
}