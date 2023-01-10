package github.io.wottrich.mlkit

import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.graphics.toRect
import github.io.wottrich.mlkit.databinding.ActivityMainBinding
import github.io.wottrich.mlkit.frame.OvalShape
import github.io.wottrich.mlkit.handlers.FacePointListener
import java.lang.Exception
import java.util.jar.Manifest

class MainActivity : AppCompatActivity(), FacePointListener {

    private val viewModel: MainViewModel by viewModels { MainViewModel.factory() }

    private var cameraStrategy: CameraStrategy? = null
    private var livenessFrameAnalysis: LivenessFrameAnalysis? = null

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setupCamera()
        setupFaceRegion()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.liveDataState.observe(this) {
            binding?.labelState?.text = it
        }
        viewModel.eulerLiveData.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
        viewModel.informationLiveData.observe(this) {
            binding?.labelInformation?.text = getString(it)
        }
    }

    private fun setupFaceRegion() {
        val livenessCameraView = binding?.livenessCameraView
        livenessCameraView?.setRoundedCanvasShape(OvalShape())
        livenessCameraView?.post {
            setupExpectedFaceRegion(livenessCameraView?.getRectCircleArea())
        }
    }

    private fun setupExpectedFaceRegion(rect: RectF?) {
        rect?.let {
            val cameraView = requireNotNull(binding?.cameraView)
            livenessFrameAnalysis?.setExpectedFaceRegion(
                it.toRect().increase(FACE_EXPECTED_REGION_GROWTH_VALUE),
                cameraView.width,
                cameraView.height
            )
        }
    }

    private fun getRectF(): RectF {
        val cx = 60
        val cy = 60
        val radius = 2.3f
        return RectF(
            cx - radius, cy - radius, cx + radius, cy + radius
        )
    }

    private fun setupCamera() {
        val cameraView = requireNotNull(binding?.cameraView)
        try {
            livenessFrameAnalysis = LivenessFrameAnalysis(viewModel, this)
            cameraStrategy = FotoapparatCamera(this).apply {
                setupCamera(
                    cameraView,
                    CameraType.FRONT,
                    livenessFrameAnalysis
                )
            }
        } catch (ex: Exception) {
            println("SETUP CAMERA EXCEPTION: $ex")
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            if (hasCameraPermission()) {
                cameraStrategy?.startCamera()
            } else {
                requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.CAMERA),
                    REQUEST_CAMERA_PERMISSION
                )
            }
        } catch (ex: IllegalStateException) {
            ex.printStackTrace()
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.CAMERA
        ) == PermissionChecker.PERMISSION_GRANTED
    }

    override fun onStop() {
        super.onStop()
        try {
            cameraStrategy?.stopCamera()
        } catch (ex: IllegalStateException) {
            ex.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        livenessFrameAnalysis?.close()
        binding = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
            cameraStrategy?.startCamera()
        } else {
            Toast.makeText(this, "We need camera perssion to keep app working", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun updateFacePoints(points: List<PointF>) {
        binding?.livenessCameraView?.updatePoints(points)
    }

    companion object {
        private const val FACE_EXPECTED_REGION_GROWTH_VALUE =
            0.3f // increases the region area in 30% to avoid interrupting the flow if the user moves a little

        private const val REQUEST_CAMERA_PERMISSION = 111

    }
}

fun Rect.increase(percent: Float): Rect {
    val dLeft = (this.left * percent).toInt()
    val dTop = (this.top * percent).toInt()

    val left = this.left - dLeft
    val top = this.top - dTop
    val right = this.right + dLeft
    val bottom = this.bottom + dTop

    return Rect(left, top, right, bottom)
}