package github.io.wottrich.mlkit.handlers

import android.graphics.PointF
import androidx.annotation.StringRes
import com.google.mlkit.vision.face.Face
import github.io.wottrich.mlkit.BaseFaceHandler
import github.io.wottrich.mlkit.FaceState
import github.io.wottrich.mlkit.FaceStateListener
import github.io.wottrich.mlkit.R
import java.util.*

enum class HeadMovement {
    MOVE_RIGHT,
    MOVE_LEFT,
    MOVE_UP,
    MOVE_DOWN;

    @StringRes
    fun getMovementMessage(): Int {
        return when (this) {
            MOVE_RIGHT -> R.string.euler_movement_move_to_right
            MOVE_LEFT -> R.string.euler_movement_move_to_left
            MOVE_UP -> R.string.euler_movement_move_to_top
            MOVE_DOWN -> R.string.euler_movement_move_to_bottom
        }
    }

    fun toFaceState(): FaceState {
        return when (this) {
            MOVE_RIGHT -> FaceState.Movement.EulerMovement.RightMovementSuccess
            MOVE_LEFT -> FaceState.Movement.EulerMovement.LeftMovementSuccess
            MOVE_UP -> FaceState.Movement.EulerMovement.UpMovementSuccess
            MOVE_DOWN -> FaceState.Movement.EulerMovement.DownMovementSuccess
        }
    }

    fun getSupportPoint(): PointF {
        return when (this) {
            MOVE_RIGHT -> PointF(150f, 0f)
            MOVE_LEFT -> PointF(-150f, 0f)
            MOVE_UP -> PointF(0f, -150f)
            MOVE_DOWN -> PointF(0f, 150f)
        }
    }


    companion object {
        val headMovements = listOf(
            MOVE_RIGHT,
            MOVE_LEFT,
            MOVE_UP,
            MOVE_DOWN
        )
    }
}

class EulerMovementIterator : Iterator<HeadMovement> {
    private val expectedHeadMovements = LinkedList<HeadMovement>()

    private var currentHeadMovement: HeadMovement
    val current get() = currentHeadMovement

    init {
        loadExpectedHeadMovements()
        currentHeadMovement = expectedHeadMovements.first
    }

    override fun hasNext(): Boolean {
        return expectedHeadMovements.any()
    }

    override fun next(): HeadMovement {
        currentHeadMovement = expectedHeadMovements.removeFirst()
        return currentHeadMovement
    }

    private fun loadExpectedHeadMovements() {
        expectedHeadMovements.clear()
        expectedHeadMovements.addAll(
            HeadMovement.headMovements.shuffled()
        )
    }

}



class EulerMovementFaceHandler(
    private val supportPointListener: FacePointListener? = null
): BaseFaceHandler() {

    private var state: EulerMovementState? = EulerMovementState.CentralizeFace
    private val headMovement: EulerMovementIterator = EulerMovementIterator()

    override fun handle(faces: List<Face>, listener: FaceStateListener) {
        if (faces.size > 1) {
            listener.onFaceStateEvent(FaceState.FaceCount.MoreThatOneFace)
        } else {
            val face = faces.firstOrNull() ?: return
            val headEulerX = face.headEulerAngleX
            val headEulerY = face.headEulerAngleY
            println("=====> X: $headEulerX")
            println("=====> Y: $headEulerY")
            val isCentralizedFace = headEulerX in X_CENTRALIZE_FACE_RANGE && headEulerY in Y_CENTRALIZE_FACE_RANGE

            if (state == EulerMovementState.CentralizeFace) {
                nextCanHandle = false
                if (isCentralizedFace) {
                    supportPointListener?.updateFacePoints(
                        listOf(headMovement.current.getSupportPoint())
                    )
                    listener.onFaceStateEvent(getMovementMessage())
                } else {
                    listener.onFaceStateEvent(getCentralizeFaceMessage())
                }
            }

            when (state) {
                EulerMovementState.CentralizeFace -> {
                    if (isCentralizedFace) state = EulerMovementState.MovementFace
                }
                EulerMovementState.MovementFace -> {
                    if (isExpectedMovementHandled(headEulerX, headEulerY)) {
                        listener.onFaceStateEvent(headMovement.current.toFaceState())
                        state = EulerMovementState.MovementSuccess
                    }
                }
                EulerMovementState.MovementSuccess -> {
                    if (headMovement.hasNext()) {
                        headMovement.next()
                        state = EulerMovementState.CentralizeFace
                    } else {
                        listener.onFaceStateEvent(FaceState.Movement.EulerMovement.AllMovementSuccess)
                        listener.onFaceStateEvent(getAllMovementsSuccessMessage())
                        nextCanHandle = true
                        state = null
                    }
                }
                null -> Unit
            }

        }
    }

    private fun isExpectedMovementHandled(
        headEulerX: Float,
        headEulerY: Float
    ): Boolean {
        return when (headMovement.current) {
            HeadMovement.MOVE_RIGHT -> isHeadMovedToRight(headEulerY)
            HeadMovement.MOVE_LEFT -> isHeadMovedToLeft(headEulerY)
            HeadMovement.MOVE_UP -> isHeadMovedToUp(headEulerX)
            HeadMovement.MOVE_DOWN -> isHeadMovedToDown(headEulerX)
        }
    }

    private fun isHeadMovedToRight(headEulerY: Float): Boolean {
        return headEulerY < Y_MOVEMENT_RANGE.start
    }

    private fun isHeadMovedToLeft(headEulerY: Float): Boolean {
        return headEulerY > Y_MOVEMENT_RANGE.endInclusive
    }

    private fun isHeadMovedToUp(headEulerX: Float): Boolean {
        return headEulerX > X_MOVEMENT_RANGE.endInclusive
    }

    private fun isHeadMovedToDown(headEulerX: Float): Boolean {
        return headEulerX < X_MOVEMENT_RANGE.start
    }

    private fun getMovementMessage(): FaceState.Information {
        return FaceState.Information(headMovement.current.getMovementMessage())
    }

    private fun getAllMovementsSuccessMessage(): FaceState.Information {
        return FaceState.Information(R.string.euler_movement_all_movements_done)
    }

    private fun getCentralizeFaceMessage(): FaceState.Information {
        return FaceState.Information(R.string.euler_movement_centralize_face)
    }

    companion object {
        private val X_MOVEMENT_RANGE = -10f..20F
        private val Y_MOVEMENT_RANGE = -10f..10f
        private val X_CENTRALIZE_FACE_RANGE = -10f..10f
        private val Y_CENTRALIZE_FACE_RANGE = -10f..10f
    }

}

sealed class EulerMovementState {
    object CentralizeFace : EulerMovementState()
    object MovementFace : EulerMovementState()
    object MovementSuccess : EulerMovementState()
}