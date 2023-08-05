package com.example.livenativerppg.models.faceDetectYNCreated.ui

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import com.example.livenativerppg.R
import com.example.livenativerppg.databinding.ActivityFeceDetectorYncreatedBinding
import org.opencv.android.*
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.properties.Delegates


private const val TAG = "FeceDetectorYNCreated"

class FeceDetectorYNCreatedActivity : CameraActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private var mOpenCvCameraView: CameraBridgeViewBase? = null
    private var mRgba: Mat? = null
    private var mGray: Mat? = null
    private var fd_file: File? = null
    private var fr_file: File? = null
    var faceDetectorInst by Delegates.notNull<Long>()


    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")
                    mOpenCvCameraView!!.enableView()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun loadCascadeFile(cascadeDir: File, id: Int, filename: String): String? {
        val inp = resources.openRawResource(id)
        val cascadeFile = File(cascadeDir, filename)
        val os = FileOutputStream(cascadeFile)
        val buffer = ByteArray(4096)
        var bytesRead: Int
        while (inp.read(buffer).also { bytesRead = it } != -1) {
            os.write(buffer, 0, bytesRead)
        }
        inp.close()
        os.close()
        return cascadeFile.absolutePath
    }

    lateinit var binding: ActivityFeceDetectorYncreatedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeceDetectorYncreatedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        mOpenCvCameraView = binding.cameraOpencvPreview
        mOpenCvCameraView?.setCameraIndex(1)
        mOpenCvCameraView?.setCvCameraViewListener(this)
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume: open Cv not Found")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback)
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    override fun getCameraViewList(): List<CameraBridgeViewBase?>? {
        return listOf(mOpenCvCameraView!!)
    }

    override fun onPause() {
        super.onPause()
        mOpenCvCameraView?.disableView()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView?.disableView()
        }
    }

    companion object {
        // Used to load the 'livenativerppg' library on application startup.
        init {
            System.loadLibrary("livenativerppg")
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        mRgba = Mat(width, height, CvType.CV_8UC4)
        mGray = Mat(width, height, CvType.CV_8UC4)

        fd_file = getDir("fd_models", MODE_PRIVATE)
        fr_file = getDir("fr_models", MODE_PRIVATE)

        val fd_file_absolute_dir =
            loadCascadeFile(fd_file!!, R.raw.face_detection_yunet_2022mar, "fd_model.onnx")!!
        val fr_file_absolute_dir = loadCascadeFile(fr_file!!,
            R.raw.face_detection_yunet_2022mar_act_int8_wt_int8_quantized,
            "fr_model.onnx")!!

        Log.d(TAG, "onCameraViewStarted: fd: $fd_file_absolute_dir, fr: $fr_file_absolute_dir")

        faceDetectorInst = _init(
            fd_file_absolute_dir, fr_file_absolute_dir,
            width, height
        )
    }

    override fun onCameraViewStopped() {
        mRgba?.release()
        mGray?.release()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        mRgba = inputFrame?.rgba()


        Imgproc.cvtColor(mRgba , mRgba , Imgproc.COLOR_RGBA2BGR)
        FaceDetectAndSelectFrame(faceDetectorInst, mRgba!!.nativeObjAddr)
        Imgproc.cvtColor(mRgba , mRgba , Imgproc.COLOR_BGR2RGBA)

        return mRgba!!;
    }

    external fun _init(fd_model: String, fr_model: String, width: Int, height: Int): Long
    external fun FaceDetectAndSelectFrame(self: Long, framAddr: Long);
}