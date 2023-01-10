package github.io.wottrich.mlkit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainViewModel : ViewModel(), FaceStateListener {

    private val _liveDataState = MutableLiveData<String>()
    val liveDataState: LiveData<String> = _liveDataState

    private val _eulerLiveData = MutableLiveData<String>()
    val eulerLiveData: LiveData<String> = _eulerLiveData

    private val _informationLiveData = MutableLiveData<Int>()
    val informationLiveData: LiveData<Int> = _informationLiveData

    override fun onFaceStateEvent(faceState: FaceState, errorMessage: String?) {
        _liveDataState.postValue(faceState.name)
        handleFaceState(faceState)
        println("========> FaceState: $faceState")
        println("========> Error msg: $errorMessage")
    }

    private fun handleFaceState(faceState: FaceState) {
        when (faceState) {
            is FaceState.Movement.EulerMovement -> handleEulerMovement(faceState)
            is FaceState.Information -> _informationLiveData.postValue(faceState.message)
            else -> Unit
        }
    }

    private fun handleEulerMovement(eulerMovement: FaceState.Movement.EulerMovement) {
        when (eulerMovement) {
            FaceState.Movement.EulerMovement.DownMovementSuccess -> _eulerLiveData.postValue("Down Movement Success")
            FaceState.Movement.EulerMovement.LeftMovementSuccess -> _eulerLiveData.postValue("Left Movement Success")
            FaceState.Movement.EulerMovement.RightMovementSuccess -> _eulerLiveData.postValue("Right Movement Success")
            FaceState.Movement.EulerMovement.UpMovementSuccess -> _eulerLiveData.postValue("Up Movement Success")
            FaceState.Movement.EulerMovement.AllMovementSuccess -> _eulerLiveData.postValue("All Movements success")
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return modelClass.newInstance()
                }
            }
        }
    }

}