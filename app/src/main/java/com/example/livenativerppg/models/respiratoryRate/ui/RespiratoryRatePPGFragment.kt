package com.example.livenativerppg.models.respiratoryRate.ui

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
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
import com.example.livenativerppg.R
import com.example.livenativerppg.commons.makeToast
import com.example.livenativerppg.commons.ppgDateFormat
import com.example.livenativerppg.commons.ppgTimeDateFormat
import com.example.livenativerppg.component.db.models.PPGResult
import com.example.livenativerppg.component.db.models.PPGType
import com.example.livenativerppg.component.db.models.State
import com.example.livenativerppg.component.utility.Math.Fft
import com.example.livenativerppg.component.utility.Math.Fft2
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.FragmentRespiratoryRatePPGBinding
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
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Named
import kotlin.collections.ArrayList
import kotlin.math.ceil


private const val TAG = "RespiratoryRatePPGFragm"
private const val TYPE= "type"
@AndroidEntryPoint
class RespiratoryRatePPGFragment : Fragment() {
    lateinit var binding: FragmentRespiratoryRatePPGBinding
    lateinit var controller: NavController
    lateinit var permissionRequester: ActivityResultLauncher<Array<String>>
    lateinit var listenableFuture: ListenableFuture<ProcessCameraProvider>
    private var type :String? = ""

    private var dialog: AlertDialog? = null
    @Inject
    @Named(Variables.PPG_RF_PATH)
    lateinit var ppgRfDatabaseReference: DatabaseReference


    val permissions: Array<String> = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this@RespiratoryRatePPGFragment.context) {
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

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume: open Cv not Found")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, requireContext(), mLoaderCallback)
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentRespiratoryRatePPGBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller = Navigation.findNavController(requireView())
        if (arguments != null){
            type = requireArguments().getString(TYPE).toString()
            binding.unitTxtView.text = getString(R.string.pulse_respiration_quotient)
        }

        permissionRequester =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                if (it.map { it.value }.any { !it }) {
                    Toast.makeText(
                        requireContext(),
                        "must all permission granted $it",
                        Toast.LENGTH_SHORT
                    ).show()
                    permissionRequester.launch(permissions)
                } else {
                    startCamera()
                }
            }
        permissionRequester.launch(permissions)
        binding.progressHorizontalDet.max = 30
    }

    private fun startCamera() {
        listenableFuture = ProcessCameraProvider.getInstance(requireContext())
        listenableFuture.addListener({
            val provider = listenableFuture.get()
            bindViewer(provider)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindViewer(provider: ProcessCameraProvider?) {
        val PreView = Preview.Builder().build()
            .also {
                it.setSurfaceProvider(
                    ContextCompat.getMainExecutor(requireContext()),
                    binding.PreView.surfaceProvider
                )
            }

        val capture = ImageCapture.Builder().build()
        val analyser = ImageAnalysis.Builder().build().also {
            (if (type.isNullOrEmpty()) "" else type)?.let { it1 ->
                FrameAnalyzer(binding ,
                    it1,
                    ppgRfDatabaseReference
                )
            }?.let { it2 ->
                it.setAnalyzer(ContextCompat.getMainExecutor(requireContext()),
                    it2
                )
            }
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

    class FrameAnalyzer(
        val binding: FragmentRespiratoryRatePPGBinding,
        val type: String,
        val ppgRfDatabaseReference: DatabaseReference
    ) : ImageAnalysis.Analyzer {

        // Variables Initialization
        private val processing = AtomicBoolean(false)

        //RR variable
        var Breath = 0
        var bufferAvgBr = 0.0

        //Beats variable
        var Beats = 0
        var bufferAvgB = 0.0


        //ProgressBar
        private val ProgHeart: ProgressBar? = null
        var ProgP = 0
        var inc = 0

        //Freq + timer variable
        private var startTime: Long = 0
        private var SamplingFreq = 0.0

        //Arraylist
        var GreenAvgList = ArrayList<Double>()
        var RedAvgList = ArrayList<Double>()
        var counter = 1
        private var init = false


        override fun analyze(image: ImageProxy) {
            val data = ByteArrayOutputStream()
            if (!init){
                startTime = System.currentTimeMillis()
                init = !init
            }
            val rgb = getRgbMat(image)
            val mean = Core.mean(rgb)

            //Atomically sets the value to the given updated value if the current value == the expected value.
            if (!processing.compareAndSet(false, true)) {
                image.close()
                return
            }

            val RedAvg: Double = mean.`val`[0]
            val GreenAvg: Double = mean.`val`[1]

            GreenAvgList.add(GreenAvg)
            RedAvgList.add(RedAvg)

            ++counter
            //To check if we got a good red intensity to process if not return to the condition and set it again until we get a good red intensity
            if (RedAvg < 200) {
                inc = 0
                ProgP = inc
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

            if (totalTimeInSecs >= 30) {
                --counter
                //when 30 seconds of measuring passes do the following " we chose 30 seconds to take half sample since 60 seconds is normally a full sample of the heart beat
                Log.d(TAG, "analyze: Process begin")
                processing.set(true)
                val Green = GreenAvgList.toArray(Array(GreenAvgList.size) {0.0})
                val Red = RedAvgList.toArray(Array(RedAvgList.size) { 0.0 })
                Log.d(TAG, "analyze: Green: ${Green.joinToString { it.toString() } } , size : ${Green.size}, \nred: ${Red.joinToString { it.toString() } } , size : ${Red.size}")
                Log.d(TAG, "analyze: $counter")
                SamplingFreq = (counter / totalTimeInSecs)

                Log.d(TAG, "analyze: $SamplingFreq")
                val RRFreq = Fft2.FFT(Green, counter, SamplingFreq)
                val bpm = ceil(RRFreq * 60)
                val RR1Freq = Fft2.FFT(Red, counter, SamplingFreq)
                val breath1 = ceil(RR1Freq * 60)


                // The following code is to make sure that if the respiration rate from red and green intensities are reasonable
                // take the average between them, otherwise take the green or red if one of them is good
                if ((bpm > 10 || bpm < 24)) {
                    bufferAvgBr = if ((breath1 > 10 || breath1 < 24)) {
                        (bpm + breath1) / 2;
                    } else {
                        bpm;
                    }
                } else if ((breath1 > 10 || breath1 < 24)) {

                    bufferAvgBr = breath1;
                }


                if (bufferAvgBr < 10 || bufferAvgBr > 24) {
                    inc = 0
                    ProgP = inc
                    binding.progressHorizontalDet.setProgressCompat(ProgP, true)
                    makeToast(binding.root.context ,  "Measurement Failed")
                    Log.d(TAG, "analyze: bufferAvgBr value: $bufferAvgBr")
                    startTime = System.currentTimeMillis()
                    counter = 0
                    processing.set(false)
                    image.close()
                    return
                }
                Breath = bufferAvgBr.toInt()

                if (type.isNotEmpty()) {
                    val HRFreq = Fft.FFT(Green, counter, SamplingFreq)
                    val bpm = ceil(HRFreq * 60)
                    val HR2Freq = Fft.FFT(Red, counter, SamplingFreq)
                    val bpm2 = ceil(HR2Freq * 60)


                    // The following code is to make sure that if the heartrate from red and green intensities are reasonable
                    // take the average between them, otherwise take the green or red if one of them is good
                    if (bpm > 45 && bpm < 200) {
                        bufferAvgB = if (bpm2 > 45 || bpm2 < 200) {
                            (bpm + bpm2) / 2
                        } else {
                            bpm
                        }
                    } else if (bpm2 > 45 && bpm2 < 200) {
                        bufferAvgB = bpm2
                    }

                    if (bufferAvgB < 45 || bufferAvgB > 200) { //if the heart beat wasn't reasonable after all reset the progresspag and restart measuring
                        inc = 0
                        ProgP = inc
                        binding.progressHorizontalDet.setProgressCompat(ProgP, true)
                        makeToast(binding.root.context ,  "Measurement Failed")
                        Log.d(TAG, "analyze:bufferAvgB value: $bufferAvgB")
                        startTime = System.currentTimeMillis()
                        counter = 0
                        processing.set(false)
                    } else
                        Beats = bufferAvgB.toInt()
                }
            }

            if (type.isNotEmpty()) {
                if (Breath != 0 && Beats != 0) {
                    Log.d(TAG, "analyze: Beats not equal 0")
                    inc = 0
                    ProgP = inc
                    binding.progressHorizontalDet.setProgressCompat(ProgP, true)
                    startTime = System.currentTimeMillis()
                    counter = 0
                    binding.resultLayout.isVisible = true
                    binding.bpmTxtView.text = (Beats / Breath).toString()
                    val date = Calendar.getInstance().time
                    ppgRfDatabaseReference
                        .child(ppgDateFormat.format(date))
                        .child(ppgTimeDateFormat.format(date))
                        .setValue(PPGResult(hrTorr = (Beats / Breath).toFloat(), MeasureDate = date.time , type = PPGType.HR_RF.ordinal, state = if (Beats in 40..104 && Breath in 12..20) State.normal.ordinal else State.upnormal.ordinal))
                        .addOnFailureListener {
                            Log.d(TAG, "analyze: error: ${it.message}")
                        }
                }
            }else{
                if (Breath != 0) {
                    Log.d(TAG, "analyze: Beats not equal 0")
                    inc = 0
                    ProgP = inc
                    binding.progressHorizontalDet.setProgressCompat(ProgP, true)
                    startTime = System.currentTimeMillis()
                    counter = 0
                    binding.resultLayout.isVisible = true
                    binding.bpmTxtView.text = Breath.toString()

                    val date = Calendar.getInstance().time
                    ppgRfDatabaseReference
                        .child(ppgDateFormat.format(date))
                        .child(ppgTimeDateFormat.format(date))
                        .setValue(PPGResult(RF = Breath, MeasureDate = date.time , type = PPGType.RF.ordinal, state = if (Breath in 12..20) State.normal.ordinal else State.upnormal.ordinal))
                        .addOnFailureListener {
                            Log.d(TAG, "analyze: error: ${it.message}")
                        }
                }
            }
            processing.set(false)
            image.close()
            return

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

    companion object {
        fun getInstance(type: String)  = RespiratoryRatePPGFragment().apply{
            arguments = Bundle().apply {
                putString(TYPE, type)
            }
        }
    }
}