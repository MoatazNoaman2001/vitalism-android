package com.example.livenativerppg.models.heartRateProcessing.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.os.FileUtils
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.livenativerppg.R
import com.example.livenativerppg.commons.birthDayDateFromate
import com.example.livenativerppg.commons.makeToast
import com.example.livenativerppg.commons.rppgDateFormat
import com.example.livenativerppg.commons.rppgTimeDateFormat
import com.example.livenativerppg.component.base.BaseFragment
import com.example.livenativerppg.component.natives.*
import com.example.livenativerppg.component.utility.BitmapUtils
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.ActivityHeartRateProcessingRppgactivityBinding
import com.example.livenativerppg.ml.GeneratedModel
import com.example.livenativerppg.models.resultPage.ui.ResultMeasurementActivity
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.Gender
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.common.PointF3D
import com.google.mlkit.vision.facemesh.FaceMeshDetection
import com.google.mlkit.vision.facemesh.FaceMeshDetector
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.newSingleThreadContext
import org.opencv.android.*
import org.opencv.core.*
import org.opencv.core.Core.addWeighted
import org.opencv.core.Core.bitwise_not
import org.opencv.core.CvType.CV_8UC1
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.*
import org.opencv.objdetect.CascadeClassifier
import org.tensorflow.lite.TensorFlowLite
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Named

private const val TAG = "HeartRateProcessingRPPG"

@AndroidEntryPoint
class HeartRateProcessingRPPGFragment :
    BaseFragment<ActivityHeartRateProcessingRppgactivityBinding>(R.layout.activity_heart_rate_processing_rppgactivity) {
    lateinit var permissionRequester: ActivityResultLauncher<Array<String>>
    lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    lateinit var controller: NavController
    lateinit var user: FirebaseUser
    lateinit var analyzer: FrameAnalyserRPPG

    @Inject
    @Named(value = "emr")
    lateinit var myMeasurementRef: DatabaseReference
    @Inject
    @Named(value = "BP_rppg")
    lateinit var myBPMeasurementRPPGRef: DatabaseReference

    @Inject
    @Named(Variables.Activations)
    lateinit var myActivationUtils: DocumentReference

    @Inject
    lateinit var sharedPreferences: SharedPreferences
    lateinit var mainUserInfo: UserInfo

    lateinit var rppg: RPPG;
    private var isBP: Boolean = false


    //ProgressBar
    private val ProgHeart: ProgressBar? = null
    var ProgP = 0
    var inc = 0

    //Freq + timer variable
    private var startTime: Long = 0
    private var SamplingFreq = 0.0
    lateinit var model: GeneratedModel

    val permissions: Array<String> = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    companion object {
        init {
            System.loadLibrary("livenativerppg")
        }

        fun getInstance(bool: Boolean, isBp: Boolean) = HeartRateProcessingRPPGFragment().apply {
            arguments = Bundle().apply {
                putBoolean("facemesh", bool);
                putBoolean("isBP", isBp)
            }
        }
    }

    private val mLoaderCallback: BaseLoaderCallback =
        object : BaseLoaderCallback(this@HeartRateProcessingRPPGFragment.context) {
            override fun onManagerConnected(status: Int) {
                when (status) {
                    SUCCESS -> {
                        Log.i("OpenCV", "OpenCV loaded successfully")
                        rppg = RPPG()
                    }
                    else -> {
                        super.onManagerConnected(status)
                    }
                }
            }
        }

    override fun onInitialized() {
        super.onInitialized()

        controller = Navigation.findNavController(requireView())
        user = FirebaseAuth.getInstance().currentUser!!
        mainUserInfo = Gson().fromJson(
            sharedPreferences.getString(Variables.USER_INFO, ""),
            UserInfo::class.java
        )
        if (arguments != null) {
            isBP = requireArguments().getBoolean("isBP")
        }

        if(isBP){
            binding.previewTextView.text = binding.previewTextView.text.replace(Pattern.compile("hr").toRegex() , "BP")
        }
        permissionRequester =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                if (it.map { it.value }.any { b -> !b }) {
                    Toast.makeText(
                        requireContext(),
                        "must all permission granted $it",
                        Toast.LENGTH_SHORT
                    ).show()
                    permissionRequester.launch(permissions)
                } else {
                    startCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
                }
            }

        permissionRequester.launch(permissions)

        binding.progressHorizontalDet.max = 34

        binding.toolbar.setNavigationOnClickListener {
            controller.popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        startTime = System.currentTimeMillis()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume: open Cv not Found")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, requireContext(), mLoaderCallback)
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }

        myActivationUtils.set(HashMap<String, Boolean>().apply {
            put(Variables.isUserActive, true)
            put(Variables.RPPG_ACTIVE, true)
        })
    }

    override fun onPause() {
        super.onPause()
        myActivationUtils.set(HashMap<String, Boolean>().apply {
            put(Variables.isUserActive, false)
            put(Variables.RPPG_ACTIVE, false)
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        if (analyzer.res_getter.size > 15) {
            if (!isBP)
                myMeasurementRef.child(rppgDateFormat.format(Calendar.getInstance().time))
                    .updateChildren(mapOf(rppgTimeDateFormat.format(Calendar.getInstance().time) to analyzer.res_getter))
                    .addOnSuccessListener {
                        makeToast(requireContext(), "firebase updated")
                    }.addOnFailureListener {
                        Log.d(TAG, "onCameraViewStopped: error: ${it.message}")
                        makeToast(requireContext(), "firebase failed to update")
                    }
            else{
                val date = Calendar.getInstance().time
                myBPMeasurementRPPGRef.child(rppgDateFormat.format(date))
                    .updateChildren(mapOf(rppgTimeDateFormat.format(date) to analyzer.res_getter.map {
                    val Beats = it.mean
                    val Wei = mainUserInfo.weight.toDouble()
                    val Hei = mainUserInfo.height.toDouble()
                    val Agg = Period.between(
                        birthDayDateFromate.parse(mainUserInfo.BirthDay).toInstant().atZone(
                            ZoneId.systemDefault()
                        ).toLocalDate(), LocalDate.now()
                    ).years.toFloat()
                    val Q = if (mainUserInfo.gender == Gender.male.name) 5.0 else 4.5

                    val ROB = 18.5
                    val ET = 364.5 - 1.23 * Beats
                    val BSA = 0.007184 * Math.pow(Wei, 0.425) * Math.pow(Hei, 0.725)
                    val SV = -6.6 + 0.25 * (ET - 35) - 0.62 * Beats + 40.4 * BSA - 0.51 * Agg
                    val PP = SV / (0.013 * Wei - 0.007 * Agg - 0.004 * Beats + 1.307)
                    val MPP = Q * ROB

                    val SP = (MPP + 3 / 2 * PP).toInt()
                    val DP = (MPP - PP / 3).toInt()
                    val date = Calendar.getInstance().time
                    BPRPPGResult(SP, DP, date.time)
                    }))
                    .addOnSuccessListener {
                        makeToast(requireContext(), "firebase updated")
                    }.addOnFailureListener {
                        Log.d(TAG, "onCameraViewStopped: error: ${it.message}")
                        makeToast(requireContext(), "firebase failed to update")
                    }


            }
        }
    }

    private fun startCamera(cameraSelector: CameraSelector) {
//        binding.perviewView.implementationMode = PreviewView.ImplementationMode.PERFORMANCE
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.perviewVpiew.surfaceProvider)
                }

//            val format = MediaFormat.createVideoFormat("video/avc", 1280, 750)
//            format.setInteger(
//                MediaFormat.KEY_COLOR_FORMAT,
//                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
//            )
//            format.setInteger(MediaFormat.KEY_BIT_RATE, 500 * 1024)
//            format.setInteger(MediaFormat.KEY_FRAME_RATE, 25)
//            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 3)
//
//            val encoder = MediaCodec.createEncoderByType("video/avc")
//
//            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

            val imageCapture = ImageCapture.Builder().build()


            val cameraInfo = cameraProvider.availableCameraInfos.filter {
                if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                    return@filter Camera2CameraInfo.from(it)
                        .getCameraCharacteristic(CameraCharacteristics.LENS_FACING) == CameraMetadata.LENS_FACING_BACK
                } else {
                    return@filter Camera2CameraInfo.from(it)
                        .getCameraCharacteristic(CameraCharacteristics.LENS_FACING) == CameraMetadata.LENS_FACING_FRONT
                }
            }
            for (cameraInfo in cameraInfo) {
                Log.d(TAG, "startCamera: $cameraInfo")
            }
            val supportedQuality = QualitySelector.getSupportedQualities(cameraInfo[0])
            val filteredQuality = arrayListOf(Quality.UHD, Quality.FHD, Quality.HD, Quality.SD)
                .filter { quality -> supportedQuality.contains(quality) }
            binding.toolbar.setNavigationOnClickListener {

            }
            val camExecutor = ContextCompat.getMainExecutor(requireActivity())


            analyzer = FrameAnalyserRPPG(
                requireContext(),
                binding,
                requireActivity(),
                rppg,
                mainUserInfo,
                isBP
            )

            val fm = FrameAnalyserFaceMesh(binding, this, rppg)

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
                .setTargetResolution(android.util.Size(720, 960))
//                .let {
//                    val ext  = Camera2Interop.Extender(it)
//                    ext.setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
//                    ext.setCaptureRequestOption(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, android.util.Range<Int>(20, 30))
//                    it
//                }
                .build()
                .also {
                    if (requireArguments().getBoolean("facemesh")) {
                        it.setAnalyzer(
                            camExecutor, fm
                        )
                    } else
                        it.setAnalyzer(
                            camExecutor, analyzer
                        )

                }
            val port: ViewPort? = binding.perviewVpiew.viewPort

            if (port != null) {
                val useCaseGroup = UseCaseGroup.Builder()
                    .addUseCase(preview)
                    .addUseCase(imageCapture)
                    .addUseCase(imageAnalysis)
                    .setViewPort(port)
                    .build()

                try {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, useCaseGroup
                    )
                    if (camera.cameraInfo.hasFlashUnit()) {
                        camera.cameraControl.enableTorch(true)
                    }

                } catch (e: Exception) {
                    Log.d(
                        TAG,
                        "run: " + e.message
                    )
                }
            }
        }, ContextCompat.getMainExecutor(requireContext()))

    }

    override fun setUpClicks() {

    }

    override fun addObservers() {
        super.addObservers()
    }
}

class FrameAnalyserRPPG(
    val context: Context,
    val binding: ActivityHeartRateProcessingRppgactivityBinding,
    val activity: FragmentActivity,
    val rppg: RPPG,
    val mainUserInfo: UserInfo,
    val isBP: Boolean,
) : ImageAnalysis.Analyzer {
    private var initialized = false
    private val TAG = "HeartRateProcessingRPPG"

    /* Settings */
    private val ALGORITHM: RPPG.RPPGAlgorithm = RPPG.RPPGAlgorithm.xminay
    private val SAMPLING_FREQUENCY = 1.0
    private val RESCAN_FREQUENCY = 1.0
    private val TIME_BASE = 0.001
    private val MIN_SIGNAL_SIZE = 2
    private val MAX_SIGNAL_SIZE = 6
    private val LOG = false
    private val VIDEO = false
    private val GUI = true
    private val VIDEO_BITRATE = 100000
    private val FACE_DIR = "facelib"
    private val FACE_MODEL = "haarcascade_frontalface_alt2.xml"
    private val byteSize = 4096 // buffer size
    private var resultsList: RPPGListenerList = RPPGListenerList()

    private var facedetector: CascadeClassifier? = null
    private var fileDir: File? = null

    val res_getter = resultsList.results
        get() {
            return field
        }


    private var mRgba: Mat? = null
    private var mGray: Mat? = null
    private var time: Double = 0.0


    @Throws(IOException::class)
    private fun loadCascadeFile(cascadeDir: File, id: Int, filename: String): String? {
        val inp = context.resources.openRawResource(id)
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

    override fun analyze(image: ImageProxy) {
        val mat = getRgbMat(image)

        val width = mat.width()
        val height = mat.height()
        if (!initialized) {
            mRgba = mat
            mGray = Mat(width, height, CvType.CV_8UC4)
            cvtColor(mGray, mGray, COLOR_RGB2GRAY)
            val cascadeDir = context.getDir("cascade", CameraActivity.MODE_PRIVATE)
            Log.d(TAG, "onCameraViewStarted: start camera, width: $width, height: $height ")
            // Initialise rPPG
            try {
                val Wei = mainUserInfo.weight.toDouble()
                val Hei = mainUserInfo.height.toDouble()
                val Agg = Period.between(
                    birthDayDateFromate.parse(mainUserInfo.BirthDay).toInstant().atZone(
                        ZoneId.systemDefault()
                    ).toLocalDate(), LocalDate.now()
                ).years.toFloat()
                val Q = if (mainUserInfo.gender == Gender.male.name) 5f else 4.5f

                rppg.load(
                    object : RPPGListener {
                        override fun onRPPGResult(result: RPPGResult?) {
                            Log.d(TAG, "onRPPGResult: Result: $result")
                            val fb = DecimalFormat("#.##")
                            fb.roundingMode = RoundingMode.CEILING
                            binding.bpmTxtView.text = fb.format(result?.mean).toString()
                            resultsList.addResult(result!!)
                            val progress: Float = ((resultsList.getSize().toFloat() / 60) * 100)
                            binding.readProgress.setProgressCompat(resultsList.getSize(), true)
                            binding.progressTxtView.text = "${fb.format(progress)}%"
                            if (resultsList.getSize() == 60) {
                                activity.startActivity(
                                    Intent(
                                        binding.root.context,
                                        ResultMeasurementActivity::class.java
                                    ).putExtra("results", resultsList.results).putExtra("isBP" , isBP)
                                )
                                Navigation.findNavController(binding.root).popBackStack()
                            }
                        }

                        override fun onNewPointGenerated(signalPoint: SignalPoint) {
                            Log.d(
                                TAG,
                                "onNewPointGenerated: point generated p1: ${signalPoint.point1} , p2: ${signalPoint.point2} "
                            )
                        }
                    },
                    ALGORITHM,
                    width,
                    height,
                    TIME_BASE,
                    1,
                    SAMPLING_FREQUENCY,
                    RESCAN_FREQUENCY,
                    MIN_SIGNAL_SIZE,
                    MAX_SIGNAL_SIZE,
                    context.getExternalFilesDir(null)!!.absolutePath,
                    loadCascadeFile(
                        cascadeDir,
                        R.raw.haarcascade_frontalface_alt,
                        "haarcascade_frontalface_alt.xml"
                    ).toString(),
                    LOG,
                    GUI,
                    isBP, Wei, Hei, Agg, Q
                )
                Log.i(TAG, "Loaded rPPG")
            } catch (e: IOException) {
                Log.e(
                    TAG,
                    "Failed to load cascade. Exception thrown: $e"
                )
            }

            cascadeDir.delete()
            Log.d(TAG, "analyze: initialized")
            initialized = true
        } else {
            mRgba = mat
            mGray = Mat(width, height, CvType.CV_8UC4)
            cvtColor(mGray, mGray, COLOR_RGB2GRAY)
            time = (Core.getTickCount() * 1000) / Core.getTickFrequency();

            try {
                val NewMat = Mat.zeros(
                    Size(image.width.toDouble(), image.height.toDouble()),
                    mRgba!!.type(),
                )
                resize(
                    mRgba,
                    NewMat,
                    Size(image.width.toDouble(), image.height.toDouble())
                )
                Core.transpose(NewMat, NewMat)
                Core.flip(NewMat, NewMat, -1)
                val bitmap = Bitmap.createBitmap(
                    NewMat?.cols()!!,
                    NewMat.rows(),
                    Bitmap.Config.ARGB_8888
                )
                cvtColor(NewMat, mGray, COLOR_RGBA2GRAY)
                rppg.processFrame(NewMat.nativeObjAddr, mGray?.nativeObjAddr!!, time)

                Utils.matToBitmap(NewMat, bitmap)
                binding.generatedImageView.setImageBitmap(bitmap)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
//        time = (Core.getTickCount() * 1000) / Core.getTickFrequency();
//
//        Log.d(TAG, "analyze: current measured time: $time")
//        Log.d(TAG, "analyze: current getTimeCount (in the mean thread): ${Core.getTickCount()}")
//        Log.d(TAG, "analyze: current getCPUTickCount (in the mean thread): ${Core.getCPUTickCount()}")
//
//        val job = CoroutineScope(Dispatchers.IO).apply {
//            Log.d(TAG, "analyze: current getTimeCount (in the ${Thread.currentThread().name} thread): ${Core.getTickCount()}")
//            Log.d(TAG, "analyze: current getCPUTickCount (in the ${Thread.currentThread().name} thread): ${Core.getCPUTickCount()}")
//        }
        Log.d(
            TAG,
            "analyze: rows: ${mat.rows()} , cols: ${mat.cols()} , size: ${mat.size().width}}"
        )
//        job.cancel()
        image.close()
    }

    private fun getRgbMat(image: ImageProxy): Mat {
        val planes: Array<ImageProxy.PlaneProxy> = image.planes
        val w: Int = image.getWidth()
        val h: Int = image.getHeight()
        val chromaPixelStride = planes[1].pixelStride
        val mRgba = Mat()


        if (chromaPixelStride == 2) { // Chroma channels are interleaved
            assert(planes[0].pixelStride == 1)
            assert(planes[2].pixelStride == 2)
            val y_plane = planes[0].buffer
            val y_plane_step = planes[0].rowStride
            val uv_plane1 = planes[1].buffer
            val uv_plane1_step = planes[1].rowStride
            val uv_plane2 = planes[2].buffer
            val uv_plane2_step = planes[2].rowStride
            val y_mat = Mat(h, w, CvType.CV_8UC1, y_plane, y_plane_step.toLong())
            val uv_mat1 = Mat(h / 2, w / 2, CvType.CV_8UC2, uv_plane1, uv_plane1_step.toLong())
            val uv_mat2 = Mat(h / 2, w / 2, CvType.CV_8UC2, uv_plane2, uv_plane2_step.toLong())
            val addr_diff = uv_mat2.dataAddr() - uv_mat1.dataAddr()
            if (addr_diff > 0) {
                assert(addr_diff == 1L)
                Imgproc.cvtColorTwoPlane(y_mat, uv_mat1, mRgba, Imgproc.COLOR_YUV2RGBA_NV12)
            } else {
                assert(addr_diff == -1L)
                Imgproc.cvtColorTwoPlane(y_mat, uv_mat2, mRgba, Imgproc.COLOR_YUV2RGBA_NV21)
            }
        } else { // Chroma channels are not interleaved
            val yuv_bytes = ByteArray(w * (h + h / 2))
            val y_plane = planes[0].buffer
            val u_plane = planes[1].buffer
            val v_plane = planes[2].buffer
            var yuv_bytes_offset = 0
            val y_plane_step = planes[0].rowStride
            if (y_plane_step == w) {
                y_plane[yuv_bytes, 0, w * h]
                yuv_bytes_offset = w * h
            } else {
                val padding = y_plane_step - w
                for (i in 0 until h) {
                    y_plane[yuv_bytes, yuv_bytes_offset, w]
                    yuv_bytes_offset += w
                    if (i < h - 1) {
                        y_plane.position(y_plane.position() + padding)
                    }
                }
                assert(yuv_bytes_offset == w * h)
            }
            val chromaRowStride = planes[1].rowStride
            val chromaRowPadding = chromaRowStride - w / 2
            if (chromaRowPadding == 0) {
                // When the row stride of the chroma channels equals their width, we can copy
                // the entire channels in one go
                u_plane[yuv_bytes, yuv_bytes_offset, w * h / 4]
                yuv_bytes_offset += w * h / 4
                v_plane[yuv_bytes, yuv_bytes_offset, w * h / 4]
            } else {
                // When not equal, we need to copy the channels row by row
                for (i in 0 until h / 2) {
                    u_plane[yuv_bytes, yuv_bytes_offset, w / 2]
                    yuv_bytes_offset += w / 2
                    if (i < h / 2 - 1) {
                        u_plane.position(u_plane.position() + chromaRowPadding)
                    }
                }
                for (i in 0 until h / 2) {
                    v_plane[yuv_bytes, yuv_bytes_offset, w / 2]
                    yuv_bytes_offset += w / 2
                    if (i < h / 2 - 1) {
                        v_plane.position(v_plane.position() + chromaRowPadding)
                    }
                }
            }
            val yuv_mat = Mat(h + h / 2, w, CvType.CV_8UC1)
            yuv_mat.put(0, 0, yuv_bytes)
            cvtColor(yuv_mat, mRgba, Imgproc.COLOR_YUV2RGBA_I420, 4)
        }
        return mRgba
    }


}


class FrameAnalyserFaceMesh(
    val binding: ActivityHeartRateProcessingRppgactivityBinding,
    val fragment: Fragment, val rppg: RPPG,
) :
    ImageAnalysis.Analyzer {
    private val TAG = "HeartRateProcessingRPPG"
    lateinit var defualtDetector: FaceMeshDetector

    /* Settings */
    private val ALGORITHM: RPPG.RPPGAlgorithm = RPPG.RPPGAlgorithm.g
    private val SAMPLING_FREQUENCY = 1.0
    private val RESCAN_FREQUENCY = 1.0
    private val TIME_BASE = 0.001
    private val MIN_SIGNAL_SIZE = 2
    private val MAX_SIGNAL_SIZE = 6
    private val LOG = false
    private val VIDEO = false
    private val GUI = true
    private val VIDEO_BITRATE = 100000
    private val FACE_DIR = "facelib"
    private val FACE_MODEL = "haarcascade_frontalface_alt2.xml"
    private val byteSize = 4096 // buffer size
    private var resultsList: RPPGListenerList = RPPGListenerList()

    private var facedetector: CascadeClassifier? = null
    private var fileDir: File? = null

    val res_getter = resultsList.results
        get() {
            return field
        }

    private var mRgba: Mat? = null
    private var mGray: Mat? = null
    private var time: Double = 0.0
    private var init = false;

    @Throws(IOException::class)
    private fun loadCascadeFile(cascadeDir: File, id: Int, filename: String): String? {
        val inp = fragment.resources.openRawResource(id)
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

    override fun analyze(image: ImageProxy) {
        val mat = getRgbMat(image)

        val width = mat.width()
        val height = mat.height()

        if (!init) {
            defualtDetector = FaceMeshDetection.getClient(
                FaceMeshDetectorOptions.Builder().build()
            )

            mRgba = mat
            mGray = Mat(width, height, CvType.CV_8UC4)
            cvtColor(mGray, mGray, COLOR_RGB2GRAY)
            val cascadeDir =
                fragment.requireContext().getDir("cascade", CameraActivity.MODE_PRIVATE)
            Log.d(TAG, "onCameraViewStarted: start camera, width: $width, height: $height ")
            // Initialise rPPG
            try {
                rppg.load(
                    object : RPPGListener {
                        override fun onRPPGResult(result: RPPGResult?) {
                            Log.d(TAG, "onRPPGResult: Result: $result")
                            val fb = DecimalFormat("#.##")
                            fb.roundingMode = RoundingMode.CEILING
                            binding.bpmTxtView.text = fb.format(result?.mean).toString()
                            resultsList.addResult(result!!)
                            val progress: Float = ((resultsList.getSize().toFloat() / 60) * 100)
                            binding.readProgress.setProgressCompat(resultsList.getSize(), true)
                            binding.progressTxtView.text = "${fb.format(progress)}%"
                            if (resultsList.getSize() == 60) {
                                fragment.requireActivity().startActivity(
                                    Intent(
                                        binding.root.context,
                                        ResultMeasurementActivity::class.java
                                    ).putExtra("results", resultsList.results)
                                )
                                Navigation.findNavController(binding.root).popBackStack()
                            }
                        }

                        override fun onNewPointGenerated(signalPoint: SignalPoint) {
                            Log.d(
                                TAG,
                                "onNewPointGenerated: point generated p1: ${signalPoint.point1} , p2: ${signalPoint.point2} "
                            )
                        }
                    },
                    ALGORITHM,
                    width,
                    height,
                    TIME_BASE,
                    1,
                    SAMPLING_FREQUENCY,
                    RESCAN_FREQUENCY,
                    MIN_SIGNAL_SIZE,
                    MAX_SIGNAL_SIZE,
                    fragment.requireActivity().getExternalFilesDir(null)!!.absolutePath,
                    loadCascadeFile(
                        cascadeDir,
                        R.raw.haarcascade_frontalface_alt,
                        "haarcascade_frontalface_alt.xml"
                    ).toString(),
                    LOG,
                    GUI,
                    false, 0.0, 0.0, 0f, 0f
                )
                Log.i(TAG, "Loaded rPPG")
            } catch (e: IOException) {
                Log.e(
                    TAG,
                    "Failed to load cascade. Exception thrown: $e"
                )
            }

            cascadeDir.delete()
            Log.d(TAG, "analyze: initialized")
            init = true;
        }

        val mediaImage: Image = image.image!!
        val img: InputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)

        val result = defualtDetector.process(img)
            .addOnSuccessListener {
                binding.generatedImageView.isVisible = it.isNotEmpty()
                val start = System.currentTimeMillis()
                for (faceMesh in it) {
                    time = (Core.getTickCount() * 1000) / Core.getTickFrequency();

                    val boundingBox = faceMesh.boundingBox
                    val faceMeshPoints = faceMesh.allPoints

                    Log.d(
                        TAG,
                        "analyze: time for get rgb mat: ${System.currentTimeMillis() - start}"
                    )


                    var NewMat =
                        Mat.zeros(Size(image.height.toDouble(), image.width.toDouble()), mat.type())

                    resize(mat, NewMat, Size(image.width.toDouble(), image.height.toDouble()))
                    Core.transpose(NewMat, NewMat)

                    Log.d(
                        TAG,
                        "analyze: time after tarnspose: ${System.currentTimeMillis() - start}"
                    )
                    Core.flip(NewMat, NewMat, -1)
                    Core.flip(NewMat, NewMat, 1)
                    Log.d(TAG, "analyze: time after flip: ${System.currentTimeMillis() - start}")


                    val forehead_indexs =
                        Variables.FACEMESH_FOREHEAD_INDEXS.map {
                            Pair(
                                it,
                                PointF3D.from(0f, 0f, 0f)
                            )
                        }
                            .toMutableList()
                    for (faceMeshPoint in faceMeshPoints) {
                        val index = faceMeshPoint.index
                        val pos = faceMeshPoint.position
                        if (forehead_indexs.contains(Pair(index, PointF3D.from(0f, 0f, 0f)))) {
                            val ind =
                                forehead_indexs.indexOf(Pair(index, PointF3D.from(0f, 0f, 0f)))
                            forehead_indexs[ind] = Pair(index, pos)
                        }
                    }

                    Log.d(
                        TAG,
                        "analyze: time after modify forehead landmark : ${System.currentTimeMillis() - start}"
                    )

                    for (i in 0..forehead_indexs.size)
                        if (i < forehead_indexs.size - 1) line(
                            NewMat,
                            Point(
                                forehead_indexs[i].second.x.toDouble(),
                                forehead_indexs[i].second.y.toDouble() + 8
                            ),
                            Point(
                                forehead_indexs[i + 1].second.x.toDouble(),
                                forehead_indexs[i + 1].second.y.toDouble() + 8
                            ),
                            Scalar(
                                0.0, 255.0, 0.0
                            )
                        ) else line(
                            NewMat,
                            Point(
                                forehead_indexs[i - 1].second.x.toDouble(),
                                forehead_indexs[i - 1].second.y.toDouble() + 8
                            ),
                            Point(
                                forehead_indexs[0].second.x.toDouble(),
                                forehead_indexs[0].second.y.toDouble() + 8
                            ),
                            Scalar(
                                0.0, 255.0, 0.0
                            )
                        )


                    val mask: Mat = Mat.zeros(NewMat.rows(), NewMat.cols(), CV_8UC1)

                    val matOfPoints = MatOfPoint()
                    matOfPoints.fromList(forehead_indexs.map {
                        Point(
                            it.second.x.toDouble(),
                            it.second.y.toDouble() + 8
                        )
                    }.toList())
                    drawContours(mask, listOf(matOfPoints), 0, Scalar(255.0), FILLED, 8)
                    NewMat.setTo(Scalar(0.0, 45.0, 78.0, 200.0), mask)

//                rppg.processFrameWithNoFD(f.dataAddr() , mGray?.dataAddr()!! ,mask.dataAddr(), time)

                    Log.d(
                        TAG,
                        "analyze: time after put face mesh: ${System.currentTimeMillis() - start}"
                    )
                    Core.flip(NewMat, NewMat, 1)

                    val returnedBitmap = Bitmap.createBitmap(
                        NewMat?.cols()!!,
                        NewMat.rows(),
                        Bitmap.Config.ARGB_8888
                    )

                    Utils.matToBitmap(NewMat, returnedBitmap)
                    Log.d(
                        TAG,
                        "analyze: time after mat to bitmap: ${System.currentTimeMillis() - start}"
                    )

                    binding.generatedImageView.setImageBitmap(returnedBitmap)

                    Log.d(TAG, "analyze: $boundingBox")

                }

                Log.d(TAG, "analyze: total time: ${System.currentTimeMillis() - start}")
                image.close()
            }
            .addOnFailureListener {
                it.printStackTrace()
                image.close()
            }

    }

    private fun getRgbMat(image: ImageProxy): Mat {
        val planes: Array<ImageProxy.PlaneProxy> = image.planes
        val w: Int = image.getWidth()
        val h: Int = image.getHeight()
        val chromaPixelStride = planes[1].pixelStride
        val mRgba = Mat()


        if (chromaPixelStride == 2) { // Chroma channels are interleaved
            assert(planes[0].pixelStride == 1)
            assert(planes[2].pixelStride == 2)
            val y_plane = planes[0].buffer
            val y_plane_step = planes[0].rowStride
            val uv_plane1 = planes[1].buffer
            val uv_plane1_step = planes[1].rowStride
            val uv_plane2 = planes[2].buffer
            val uv_plane2_step = planes[2].rowStride
            val y_mat = Mat(h, w, CvType.CV_8UC1, y_plane, y_plane_step.toLong())
            val uv_mat1 = Mat(h / 2, w / 2, CvType.CV_8UC2, uv_plane1, uv_plane1_step.toLong())
            val uv_mat2 = Mat(h / 2, w / 2, CvType.CV_8UC2, uv_plane2, uv_plane2_step.toLong())
            val addr_diff = uv_mat2.dataAddr() - uv_mat1.dataAddr()
            if (addr_diff > 0) {
                assert(addr_diff == 1L)
                Imgproc.cvtColorTwoPlane(y_mat, uv_mat1, mRgba, Imgproc.COLOR_YUV2RGBA_NV12)
            } else {
                assert(addr_diff == -1L)
                Imgproc.cvtColorTwoPlane(y_mat, uv_mat2, mRgba, Imgproc.COLOR_YUV2RGBA_NV21)
            }
        } else { // Chroma channels are not interleaved
            val yuv_bytes = ByteArray(w * (h + h / 2))
            val y_plane = planes[0].buffer
            val u_plane = planes[1].buffer
            val v_plane = planes[2].buffer
            var yuv_bytes_offset = 0
            val y_plane_step = planes[0].rowStride
            if (y_plane_step == w) {
                y_plane[yuv_bytes, 0, w * h]
                yuv_bytes_offset = w * h
            } else {
                val padding = y_plane_step - w
                for (i in 0 until h) {
                    y_plane[yuv_bytes, yuv_bytes_offset, w]
                    yuv_bytes_offset += w
                    if (i < h - 1) {
                        y_plane.position(y_plane.position() + padding)
                    }
                }
                assert(yuv_bytes_offset == w * h)
            }
            val chromaRowStride = planes[1].rowStride
            val chromaRowPadding = chromaRowStride - w / 2
            if (chromaRowPadding == 0) {
                // When the row stride of the chroma channels equals their width, we can copy
                // the entire channels in one go
                u_plane[yuv_bytes, yuv_bytes_offset, w * h / 4]
                yuv_bytes_offset += w * h / 4
                v_plane[yuv_bytes, yuv_bytes_offset, w * h / 4]
            } else {
                // When not equal, we need to copy the channels row by row
                for (i in 0 until h / 2) {
                    u_plane[yuv_bytes, yuv_bytes_offset, w / 2]
                    yuv_bytes_offset += w / 2
                    if (i < h / 2 - 1) {
                        u_plane.position(u_plane.position() + chromaRowPadding)
                    }
                }
                for (i in 0 until h / 2) {
                    v_plane[yuv_bytes, yuv_bytes_offset, w / 2]
                    yuv_bytes_offset += w / 2
                    if (i < h / 2 - 1) {
                        v_plane.position(v_plane.position() + chromaRowPadding)
                    }
                }
            }
            val yuv_mat = Mat(h + h / 2, w, CvType.CV_8UC1)
            yuv_mat.put(0, 0, yuv_bytes)
            cvtColor(yuv_mat, mRgba, Imgproc.COLOR_YUV2RGBA_I420, 4)
        }
        return mRgba
    }


}