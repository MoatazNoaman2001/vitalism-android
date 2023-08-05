package com.example.livenativerppg.models.oxygenSaturationProcessing.ui

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.livenativerppg.commons.ppgDateFormat
import com.example.livenativerppg.commons.ppgTimeDateFormat
import com.example.livenativerppg.component.db.models.PPGResult
import com.example.livenativerppg.component.db.models.PPGType
import com.example.livenativerppg.component.db.models.State
import com.example.livenativerppg.component.utility.Math.Fft
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.FragmentOxygenSaturationPPGBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.database.DatabaseReference
import dagger.hilt.android.AndroidEntryPoint
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Named
import kotlin.collections.ArrayList
import kotlin.math.sqrt


private const val TAG = "OxygenSaturationPPGFrag"

@AndroidEntryPoint
class OxygenSaturationPPGFragment : Fragment() {
    lateinit var binding: FragmentOxygenSaturationPPGBinding
    lateinit var controller: NavController
    lateinit var permissionsRequester: ActivityResultLauncher<Array<String>>
    lateinit var listenableFuture: ListenableFuture<ProcessCameraProvider>

    @Inject
    @Named(Variables.PPG_o2_PATH)
    lateinit var ppgO2DatabaseReference: DatabaseReference


    val permissions: Array<String> = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    private val mLoaderCallback: BaseLoaderCallback =
        object : BaseLoaderCallback(this@OxygenSaturationPPGFragment.context) {
            override fun onManagerConnected(status: Int) {
                when (status) {
                    SUCCESS -> {
                        Log.i("OpenCV", "OpenCV loaded successfully")
                    }
                    else -> {
                        super.onManagerConnected(status)
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentOxygenSaturationPPGBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller = Navigation.findNavController(requireView())
        permissionsRequester =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                if (it.map { it.value }.any { !it }) {
                    Toast.makeText(requireContext(),
                        "must all permission granted $it",
                        Toast.LENGTH_SHORT).show()
                    permissionsRequester.launch(permissions)
                } else {
                    StartCamera()
                }
            }
        permissionsRequester.launch(permissions)

        binding.progressHorizontalDet.max = 30
    }

    override fun onResume() {
        super.onResume()

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume: open Cv not Found")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, requireContext(), mLoaderCallback)
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }


    fun StartCamera() {
        listenableFuture = ProcessCameraProvider.getInstance(requireContext())
        listenableFuture.addListener({
            val provider = listenableFuture.get()
            bindCamera(provider)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindCamera(provider: ProcessCameraProvider?) {
        val PreView = Preview.Builder().build()
            .also {
                it.setSurfaceProvider(ContextCompat.getMainExecutor(requireContext()),
                    binding.PreView.surfaceProvider)
            }

        val capture = ImageCapture.Builder().build()
        val analyser_core = FrameAnalyzer(binding , ppgO2DatabaseReference)

        val analyser = ImageAnalysis.Builder().build().also {
            it.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), analyser_core)
        }
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        val port = binding.PreView.viewPort

        if (port != null) {
            val usecases = UseCaseGroup.Builder()
                .addUseCase(PreView)
                .addUseCase(capture)
                .addUseCase(analyser)
                .setViewPort(port)
                .build()
            try {
                provider?.unbindAll()
                val camera = provider?.bindToLifecycle(this, cameraSelector, usecases)
                if (camera?.cameraInfo!!.hasFlashUnit()) {
                    camera.cameraControl.enableTorch(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}

class FrameAnalyzer(
    val binding: FragmentOxygenSaturationPPGBinding,
    val ppgO2DatabaseReference: DatabaseReference
) : ImageAnalysis.Analyzer {
    // Variables Initialization
    private val processing = AtomicBoolean(false)

    //Freq + timer variable
    private var startTime: Long = 0
    private var SamplingFreq = 0.0

    // SPO2 variables
    private val RedBlueRatio = 0.0
    var Stdr = 0.0
    var Stdb = 0.0
    var sumred = 0.0
    var sumblue = 0.0
    var o2 = 0

    //Arraylist
    var RedAvgList = ArrayList<Double>()
    var BlueAvgList = ArrayList<Double>()
    var counter = 0
    private var dialog: AlertDialog? = null

    private var init= false

    override fun analyze(image: ImageProxy) {

        if (!init){
            startTime = System.currentTimeMillis()
            init = !init
        }

        val rgb = getRgbMat(image)
        //Atomically sets the value to the given updated value if the current value == the expected value.
        if (!processing.compareAndSet(false, true) || o2 != 0) {
            image.close()
            return
        }

        val means = Core.mean(rgb)

        val RedAvg: Double = means.`val`[0]
        val BlueAvg: Double = means.`val`[1]

        sumred += RedAvg
        sumblue += BlueAvg

        Log.d(TAG, "analyze: formate: ${image.format}")
        Log.d(TAG,
            "analyze: width: ${image.width}, height: ${image.height} , area: ${image.height * image.width} , byte array size: ${
                rgb.size()
            },     addtional info: ${image.imageInfo.timestamp} , red avg: $RedAvg , Blue avg $BlueAvg")

        BlueAvgList.add(BlueAvg)
        RedAvgList.add(RedAvg)

        ++counter
        //To check if we got a good red intensity to process if not return to the condition and set it again until we get a good red intensity
        if (RedAvg < 200) {
            counter = 0
            startTime = System.currentTimeMillis()
            processing.set(false)
            Log.d(TAG, "analyze: failed")
            image.close()
            return
        } else {
            Log.d(TAG, "analyze: red avg greater now: $RedAvg")
        }

        val endTime = System.currentTimeMillis()
        val totalTimeInSecs: Double = (endTime - startTime) / 1000.0 //to convert time to seconds
        Log.d(TAG, "analyze: total time $totalTimeInSecs")
        binding.progressHorizontalDet.setProgressCompat(totalTimeInSecs.toInt(), true)

        if (totalTimeInSecs >= 30) { //when 30 seconds of measuring passes do the following " we chose 30 seconds to take half sample since 60 seconds is normally a full sample of the heart beat
            startTime = System.currentTimeMillis()
            SamplingFreq = counter / totalTimeInSecs
            val Red = RedAvgList.toTypedArray()
            val Blue = BlueAvgList.toTypedArray()
            val HRFreq: Double = Fft.FFT(Red, counter, SamplingFreq)
            val bpm = Math.ceil(HRFreq * 60).toInt().toDouble()
            val meanr = sumred / counter
            val meanb = sumblue / counter
            for (i in 0 until counter - 1) {
                val bufferb = Blue[i]
                Stdb += (bufferb - meanb) * (bufferb - meanb)
                val bufferr = Red[i]
                Stdr += (bufferr - meanr) * (bufferr - meanr)
            }
            val varr = sqrt(Stdr / (counter - 1))
            val varb = sqrt(Stdb / (counter - 1))
            val R = varr / meanr / (varb / meanb)
            val spo2 = 100 - 5 * R
            o2 = spo2.toInt()
            if (o2 < 80 || o2 > 99 || bpm < 45 || bpm > 200) {
                binding.progressHorizontalDet.setProgressCompat(0, true)
                Toast.makeText(binding.root.context, "Measurement Failed", Toast.LENGTH_SHORT).show()
                startTime = System.currentTimeMillis()
                counter = 0
            }
        }
        if (o2 != 0) {
            binding.progressHorizontalDet.setProgressCompat(0, true)
            startTime = System.currentTimeMillis()
            counter = 0
            processing.set(false)
            binding.resultLayout.isVisible = true
            binding.bpmTxtView.text = o2.toString()

            val date = Calendar.getInstance().time
            ppgO2DatabaseReference
                .child(ppgDateFormat.format(date))
                .child(ppgTimeDateFormat.format(date))
                .setValue(PPGResult(o2 = o2, MeasureDate = date.time , type = PPGType.O2.ordinal, state = if (o2 in 92..100) State.normal.ordinal else State.upnormal.ordinal))
                .addOnFailureListener {
                    Log.d(TAG, "analyze: error: ${it.message}")
                }
        }

        processing.set(false)
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
            Imgproc.cvtColor(yuv_mat, mRgba, Imgproc.COLOR_YUV2RGBA_I420, 4)
        }
        return mRgba
    }

}
