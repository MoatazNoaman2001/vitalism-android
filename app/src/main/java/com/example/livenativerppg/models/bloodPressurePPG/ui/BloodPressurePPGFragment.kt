package com.example.livenativerppg.models.bloodPressurePPG.ui

import android.Manifest
import android.content.SharedPreferences
import android.hardware.camera2.CameraCharacteristics.LENS_FACING
import android.hardware.camera2.CameraMetadata
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.FLASH_MODE_ON
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.livenativerppg.commons.birthDayDateFromate
import com.example.livenativerppg.commons.ppgDateFormat
import com.example.livenativerppg.commons.ppgTimeDateFormat
import com.example.livenativerppg.component.db.models.PPGResult
import com.example.livenativerppg.component.db.models.PPGType
import com.example.livenativerppg.component.db.models.State
import com.example.livenativerppg.component.utility.Math.Fft
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.FragmentBloodPressurePPGBinding
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.Gender
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.database.DatabaseReference
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Named
import kotlin.collections.ArrayList
import kotlin.math.ceil

private const val TAG = "BloodPressurePPGFragmen"


@AndroidEntryPoint
class BloodPressurePPGFragment : Fragment() {
    lateinit var binding: FragmentBloodPressurePPGBinding
    lateinit var listener: ListenableFuture<ProcessCameraProvider>
    lateinit var permissionRequester: ActivityResultLauncher<Array<String>>
    lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    @Inject
    lateinit var preferences: SharedPreferences

    @Inject
    @Named(Variables.PPG_Bp_PATH)
    lateinit var ppgBpDatabaseReference: DatabaseReference

    val permissions: Array<String> = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentBloodPressurePPGBinding.inflate(layoutInflater)
        return binding.root
    }

    private val mLoaderCallback: BaseLoaderCallback =
        object : BaseLoaderCallback(this@BloodPressurePPGFragment.context) {
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                    startCam()
                }
            }
        permissionRequester.launch(permissions)

        binding.progressHorizontalDet.max = 30
        binding.instructionTextView.text =
            "${binding.instructionTextView.text}\ndefault result 120/80-140/90"
    }

    private fun startCam() {
        listener = ProcessCameraProvider.getInstance(requireContext());
        listener.addListener({
            val provider = listener.get()
            bindProvider(provider)

        }, ContextCompat.getMainExecutor(requireContext()))
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

    private fun bindProvider(provider: ProcessCameraProvider?) {
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.PreView.surfaceProvider)
        }
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        val imageCapture = ImageCapture.Builder()
            .setTargetResolution(Size(6936, 9248))
            .build().also {
                it.flashMode = FLASH_MODE_ON
            }

        val userInfo = Gson().fromJson(preferences.getString(Variables.USER_INFO, "") , UserInfo::class.java)
        val analyser = FrameAnalyser(
            if(userInfo.gender == Gender.male.name) 1f else 2f,
            Period.between(birthDayDateFromate.parse(userInfo.BirthDay).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), LocalDate.now()).years.toDouble(),
            userInfo.height.toDouble(),
            userInfo.weight.toDouble(),
            ppgBpDatabaseReference
        )
        val imageAnalyser = ImageAnalysis.Builder()
//            .setTargetResolution(Size(6936 , 9248))
            .build().also {
                it.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), analyser)
            }

        val cameralist = provider?.availableCameraInfos?.filter {
            return@filter Camera2CameraInfo.from(it)
                .getCameraCharacteristic(LENS_FACING) == CameraMetadata.LENS_FACING_BACK
        }

        cameralist?.forEach {
            Log.d(
                TAG,
                "bindProvider available camera resolution list item : ${it.cameraSelector.cameraFilterSet}"
            )
        }

        val supportedQuality = QualitySelector.getSupportedQualities(cameralist?.get(0)!!)
        val filteredQuality = arrayListOf(Quality.UHD, Quality.FHD, Quality.HD, Quality.SD)
            .filter { quality -> supportedQuality.contains(quality) }


        Log.d(TAG, "bindProvider: ${filteredQuality[0]}")


        val port = binding.PreView.viewPort

        if (port != null) {
            val useCases = UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(imageCapture)
                .addUseCase(imageAnalyser)
                .setViewPort(port)
                .build()

            provider?.unbindAll()
            val camera = provider?.bindToLifecycle(viewLifecycleOwner, cameraSelector, useCases)
            if (camera?.cameraInfo?.hasFlashUnit()!!) {
                camera.cameraControl.enableTorch(true)
            }
//            QualitySelector.getSupportedQualities(camera.cameraInfo)

        }
    }

    private val TAG = "BloodPressurePPGFragmen"
    inner class FrameAnalyser(
        Gen: Float,
        val Agg: Double,
        val Hei: Double,
        val Wei: Double,
        val ppgBpDatabaseReference: DatabaseReference
    ) : ImageAnalysis.Analyzer {

        private val processing = AtomicBoolean(false)


        //Beats variable
        var Beats = 0
        var bufferAvgB = 0.0

        //Freq + timer variable
        private var startTime: Long = 0
        private var SamplingFreq = 0.0

        //BloodPressure variables
        var Q = if (Gen == 1f) 5 else 4.5
        private var SP = 0
        private var DP: Int = 0

        //Arraylist
        var GreenAvgList = ArrayList<Double>()
        var RedAvgList = ArrayList<Double>()
        var counter = 0
        private var init = false;
        var previous: Mat? = null;

        override fun analyze(image: ImageProxy) {
            Log.d(TAG, "analyze: width: ${image.width} , height: ${image.height}")

            if (init) {
                startTime = System.currentTimeMillis()
                init = true;
            }

            //Atomically sets the value to the given updated value if the current value == the expected value.
            if (!processing.compareAndSet(false, true)) {
                image.close()
                return
            }


            val rgb = getRgbMat(image);
            val mask = Mat()
            val res = Core.mean(rgb, mask)

            val RedAvg: Double = res.`val`[0]
            val GreenAvg: Double = res.`val`[1]

            Log.d(TAG, "analyze: formate: ${image.format}")
            Log.d(
                TAG,
                "analyze: width: ${image.width}, height: ${image.height} , area: ${image.height * image.width} , byte array size: ${
                    rgb.size().area()
                },     addtional info: ${image.imageInfo.timestamp} , red avg: $RedAvg , green avg $GreenAvg"
            )

            GreenAvgList.add(GreenAvg)
            RedAvgList.add(RedAvg)

            //To check if we got a good red intensity to process if not return to the condition and set it again until we get a good red intensity
            if (RedAvg < 200) {
                counter = 0
                startTime = System.currentTimeMillis()
                processing.set(false)
                Log.d(TAG, "analyze: failed")
                binding.progressHorizontalDet.setProgressCompat(0, true)
                image.close()
                return
            } else {
                Log.d(TAG, "analyze: red avg greater now: $RedAvg")
            }

            ++counter

            val endTime = System.currentTimeMillis()
            Log.d(TAG, "analyze: calc entime $endTime")
            val totalTimeInSecs: Double =
                (endTime - startTime) / 1000.0 //to convert time to seconds
            Log.d(TAG, "analyze: total time $totalTimeInSecs")
            binding.progressHorizontalDet.setProgressCompat(totalTimeInSecs.toInt(), true)
            if (totalTimeInSecs >= 30) {
                //when 30 seconds of measuring passes do the following " we chose 30 seconds to take half sample since 60 seconds is normally a full sample of the heart beat
                processing.set(true)

                val Green = GreenAvgList.toTypedArray()
                val Red = RedAvgList.toTypedArray()

                SamplingFreq = (counter / totalTimeInSecs)

                val HRFreq = Fft.FFT(Green, counter, SamplingFreq)
                val bpm = ceil(HRFreq * 60)
                val HR2Freq = Fft.FFT(Red, counter, SamplingFreq)
                val bpm2 = ceil(HR2Freq * 60)


                // The following code is to make sure that if the heartrate from red and green intensities are reasonable
                // take the average between them, otherwise take the green or red if one of them is good
                if (bpm > 45 || bpm < 200) {
                    bufferAvgB = if (bpm2 > 45 || bpm2 < 200) {
                        (bpm + bpm2) / 2
                    } else {
                        bpm
                    }
                } else if (bpm2 > 45 || bpm2 < 200) {
                    bufferAvgB = bpm2
                }

                if (bufferAvgB < 45 || bufferAvgB > 200) { //if the heart beat wasn't reasonable after all reset the progresspag and restart measuring
                    Toast.makeText(requireContext(), "Measurement Failed", Toast.LENGTH_SHORT)
                    startTime = System.currentTimeMillis()
                    counter = 0
                    binding.progressHorizontalDet.setProgressCompat(0, true)
                    processing.set(false)
                }
                Beats = bufferAvgB.toInt()
                val ROB = 18.5
                val ET = 364.5 - 1.23 * Beats
                val BSA = 0.007184 * Math.pow(Wei, 0.425) * Math.pow(Hei, 0.725)
                val SV = -6.6 + 0.25 * (ET - 35) - 0.62 * Beats + 40.4 * BSA - 0.51 * Agg
                val PP = SV / (0.013 * Wei - 0.007 * Agg - 0.004 * Beats + 1.307)
                val MPP = Q.toString().toDouble() * ROB

                SP = (MPP + 3 / 2 * PP).toInt()
                DP = (MPP - PP / 3).toInt()
            }
            if (SP != 0 && DP != 0) {
                binding.resultLayout.isVisible = true
                binding.bpmTxtView.text = "$SP/$DP"

                val date = Calendar.getInstance().time
                ppgBpDatabaseReference
                    .child(ppgDateFormat.format(date))
                    .child(ppgTimeDateFormat.format(date))
                    .setValue(PPGResult(Sp = SP , Dp = DP, MeasureDate = date.time , type = PPGType.BP.ordinal, state = if (Beats in 40..104) State.normal.ordinal else State.upnormal.ordinal))
                    .addOnFailureListener {
                        Log.d(TAG, "analyze: error: ${it.message}")
                    }
                binding.progressHorizontalDet.setProgressCompat(0, true)
                startTime = System.currentTimeMillis()
                counter = 0
                processing.set(false)
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
}

