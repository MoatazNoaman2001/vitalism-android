package com.example.livenativerppg.models.heartRateProcessing.ui

import android.Manifest
import android.app.Activity
import android.graphics.*
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.livenativerppg.commons.makeToast
import com.example.livenativerppg.commons.ppgDateFormat
import com.example.livenativerppg.commons.ppgTimeDateFormat
import com.example.livenativerppg.component.db.models.PPGResult
import com.example.livenativerppg.component.db.models.PPGType
import com.example.livenativerppg.component.db.models.State
import com.example.livenativerppg.component.utility.Math.Fft
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.ActivityHeartRateProcessingBinding
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
import java.util.Calendar
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.ceil


private const val TAG = "HeartRateProcessing"

@AndroidEntryPoint
class HeartRateProcessingPPGActivity : AppCompatActivity() {

    lateinit var binding: ActivityHeartRateProcessingBinding
    lateinit var permissionRequester: ActivityResultLauncher<Array<String>>
    lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    @Inject
    @Named(Variables.PPG_HR_PATH)
    lateinit var ppgHrDatabaseReference: DatabaseReference

    val permissions: Array<String> = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHeartRateProcessingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionRequester =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                if (it.map { it.value }.any { b -> !b }) {
                    Toast.makeText(
                        this,
                        "must all permission granted $it",
                        Toast.LENGTH_SHORT
                    ).show()
                    permissionRequester.launch(permissions)
                } else {
                    startCamera(CameraSelector.DEFAULT_BACK_CAMERA)
                }
            }

        permissionRequester.launch(permissions)

        binding.progressHorizontalDet.max = 30
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

    private fun startCamera(cameraSelector: CameraSelector) {
//        binding.perviewView.implementationMode = PreviewView.ImplementationMode.PERFORMANCE
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

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
                if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
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
            val Recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.fromOrderedList(filteredQuality))
                .build()

            val camExecutor = ContextCompat.getMainExecutor(this)


            val analyser =
                FrameAnalyserPPG(binding = binding, activity = this@HeartRateProcessingPPGActivity , ppgHrDatabaseReference)
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                .let {
//                    val ext  = Camera2Interop.Extender(it)
//                    ext.setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
//                    ext.setCaptureRequestOption(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, android.util.Range<Int>(10, 30))
//                    it
//                }
                .build()
                .also {
                    it.setAnalyzer(camExecutor, analyser)
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
        }, ContextCompat.getMainExecutor(this))

    }

}

class FrameAnalyserPPG(
    val binding: ActivityHeartRateProcessingBinding,
    val activity: Activity,
    val ppgHrDatabaseReference: DatabaseReference
) :
    ImageAnalysis.Analyzer {

    // Variables Initialization
    private val processing = AtomicBoolean(false)

    //Beats variable
    var Beats = 0
    var bufferAvgB = 0.0

    //DataBase
    var user: String? = null

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

    private var counter_progress: Int = 0
    private var dialog: AlertDialog? = null

    private var init = false;

    override fun analyze(image: ImageProxy) {
        val rotationDegree = image.imageInfo.rotationDegrees
        val alpha = image.planes[0].buffer[0]
        val red = image.planes[0].buffer[1]
        val green = image.planes[0].buffer[2]
        val blue = image.planes[0].buffer[3]

        if (!init) {
            startTime = System.currentTimeMillis()
            init = true;
        }

        val rgb = getRgbMat(image);
        val mask = Mat()
        val res = Core.mean(rgb, mask)


        Log.d(TAG, "analyze: ${rgb.type()}")
        Log.d(TAG, "analyze: mask rows: ${mask.rows()} , num of cols: ${mask.cols()}")
        Log.d(TAG, "analyze: res scaler: ${res.`val`.joinToString { "$it" }}}")


        //Atomically sets the value to the given updated value if the current value == the expected value.
        if (!processing.compareAndSet(false, true)) {
            image.close()
            return
        }

        //green channel avg at 1
        val GreenAvg: Double = res.`val`.get(1)

        //red channel avg at 0
        val RedAvg: Double = res.`val`.get(0)

        Log.d(TAG, "analyze: formate: ${image.format}")
        Log.d(TAG, "analyze: width: ${image.width}, height: ${image.height} , area: ${image.height * image.width} , byte array size: ${rgb.size().area()},     addtional info: ${image.imageInfo.timestamp} , red avg: $RedAvg , green avg $GreenAvg")

        GreenAvgList.add(GreenAvg)
        RedAvgList.add(RedAvg)

        //To check if we got a good red intensity to process if not return to the condition and set it again until we get a good red intensity
        if (RedAvg < 200) {
            inc = 0
            ProgP = inc
            counter = 0
            counter_progress = 0
            RedAvgList.clear()
            GreenAvgList.clear()
            startTime = System.currentTimeMillis()
            processing.set(false)
            Log.d(TAG, "analyze: failed")
            image.close()
            return
        } else {
            Log.d(TAG, "analyze: red avg greater now: $RedAvg")
        }

        ++counter
        ++counter_progress

        val endTime = System.currentTimeMillis()
        Log.d(TAG, "analyze: calc entime $endTime")
        val totalTimeInSecs: Double = (endTime - startTime) / 1000.0 //to convert time to seconds
        Log.d(TAG, "analyze: total time $totalTimeInSecs")
        binding.progressHorizontalDet.setProgressCompat(totalTimeInSecs.toInt(), true)

        if (totalTimeInSecs >= 30) {
            //when 30 seconds of measuring passes do the following " we chose 30 seconds to take half sample since 60 seconds is normally a full sample of the heart beat
            Log.d(TAG, "analyze: Process begin")
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
                makeToast(binding.root.context , "Measurement Failed")
                startTime = System.currentTimeMillis()
                counter = 0
                counter_progress = 0
                processing.set(false)
            } else
                Beats = bufferAvgB.toInt()
        }

        if (Beats != 0) {
            Log.d(TAG, "analyze: Beats not equal 0")
            binding.resultLayout.isVisible = true
            binding.bpmTxtView.text = Beats.toString()

            val date = Calendar.getInstance().time
            ppgHrDatabaseReference.child(ppgDateFormat.format(date))
                .child(ppgTimeDateFormat.format(date))
                .setValue(PPGResult(Beats , MeasureDate = date.time , type = PPGType.HR.ordinal, state = if (Beats in 40..104) State.normal.ordinal else State.upnormal.ordinal))
                .addOnFailureListener {
                    Log.d(TAG, "analyze: error: ${it.message}")
                }
            inc = 0
            ProgP = inc
            binding.progressHorizontalDet.setProgressCompat(ProgP, true)
            startTime = System.currentTimeMillis()
            counter = 0
            counter_progress = 0
            processing.set(false)
        }

        //keeps taking frames tell 30 seconds
        processing.set(false)

        Log.d(TAG, "analyze: close current image")
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

    private fun ImageProxy.convertImageProxyToBitmap(): Bitmap {
        val buffer = planes[0].buffer
        buffer.rewind()
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun toByteArray(planeProxy: ImageProxy.PlaneProxy): ByteArray? {
        planeProxy.buffer.rewind()
        val data = ByteArray(planeProxy.buffer.remaining())
        planeProxy.buffer[data]
        return data
    }

    // Ported from opencv private class JavaCamera2Frame
    private fun ImageProxy.yuvToRgba(): Mat {
        val rgbaMat = Mat()

        if (format == ImageFormat.YUV_420_888
            && planes.size == 3
        ) {

            val chromaPixelStride = planes[1].pixelStride

            if (chromaPixelStride == 2) { // Chroma channels are interleaved
                assert(planes[0].pixelStride == 1)
                assert(planes[2].pixelStride == 2)
                val yPlane = planes[0].buffer
                val uvPlane1 = planes[1].buffer
                val uvPlane2 = planes[2].buffer
                val yMat = Mat(height, width, CvType.CV_8UC1, yPlane)
                val uvMat1 = Mat(height / 2, width / 2, CvType.CV_8UC2, uvPlane1)
                val uvMat2 = Mat(height / 2, width / 2, CvType.CV_8UC2, uvPlane2)
                val addrDiff = uvMat2.dataAddr() - uvMat1.dataAddr()
                if (addrDiff > 0) {
                    assert(addrDiff == 1L)
                    Imgproc.cvtColorTwoPlane(yMat, uvMat1, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV12)
                } else {
                    assert(addrDiff == -1L)
                    Imgproc.cvtColorTwoPlane(yMat, uvMat2, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV21)
                }
            } else { // Chroma channels are not interleaved
                val yuvBytes = ByteArray(width * (height + height / 2))
                val yPlane = planes[0].buffer
                val uPlane = planes[1].buffer
                val vPlane = planes[2].buffer

                yPlane.get(yuvBytes, 0, width * height)

                val chromaRowStride = planes[1].rowStride
                val chromaRowPadding = chromaRowStride - width / 2

                var offset = width * height
                if (chromaRowPadding == 0) {
                    // When the row stride of the chroma channels equals their width, we can copy
                    // the entire channels in one go
                    uPlane.get(yuvBytes, offset, width * height / 4)
                    offset += width * height / 4
                    vPlane.get(yuvBytes, offset, width * height / 4)
                } else {
                    // When not equal, we need to copy the channels row by row
                    for (i in 0 until height / 2) {
                        uPlane.get(yuvBytes, offset, width / 2)
                        offset += width / 2
                        if (i < height / 2 - 1) {
                            uPlane.position(uPlane.position() + chromaRowPadding)
                        }
                    }
                    for (i in 0 until height / 2) {
                        vPlane.get(yuvBytes, offset, width / 2)
                        offset += width / 2
                        if (i < height / 2 - 1) {
                            vPlane.position(vPlane.position() + chromaRowPadding)
                        }
                    }
                }

                val yuvMat = Mat(height + height / 2, width, CvType.CV_8UC1)
                yuvMat.put(0, 0, yuvBytes)
                Imgproc.cvtColor(yuvMat, rgbaMat, Imgproc.COLOR_YUV2RGBA_I420, 4)
            }
        }

        return rgbaMat
    }

    private fun decodeYUV420SPtoRedBlueGreenSum(
        yuv420sp: ByteArray?,
        width: Int,
        height: Int,
        type: Int,
    ): Int {
        if (yuv420sp == null) return 0
        val frameSize = width * height
        var sum = 0
        var sumr = 0
        var sumg = 0
        var sumb = 0
        var j = 0
        var yp = 0
        while (j < height) {
            var uvp = frameSize + (j shr 1) * width
            var u = 0
            var v = 0
            var i = 0
            while (i < width) {
                var y = (0xff and yuv420sp[yp].toInt()) - 16
                if (y < 0) y = 0
                if (i and 1 == 0) {
                    v = (0xff and yuv420sp[uvp++].toInt()) - 128
                    u = (0xff and yuv420sp[uvp++].toInt()) - 128
                }
                val y1192 = 1192 * y
                var r = y1192 + 1634 * v
                var g = y1192 - 833 * v - 400 * u
                var b = y1192 + 2066 * u
                if (r < 0) r = 0 else if (r > 262143) r = 262143
                if (g < 0) g = 0 else if (g > 262143) g = 262143
                if (b < 0) b = 0 else if (b > 262143) b = 262143
                val pixel =
                    -0x1000000 or (r shl 6 and 0xff0000) or (g shr 2 and 0xff00) or (b shr 10 and 0xff)
                val red = pixel shr 16 and 0xff
                val green = pixel shr 8 and 0xff
                val blue = pixel and 0xff
                sumr += red
                sumg += green
                sumb += blue
                i++
                yp++
            }
            j++
        }
        when (type) {
            1 -> sum = sumr
            2 -> sum = sumb
            3 -> sum = sumg
        }
        return sum
    }

    /**
     * Given a byte array representing a yuv420sp image, determine the average
     * amount of red in the image. Note: returns 0 if the byte array is NULL.
     *
     * @param yuv420sp Byte array representing a yuv420sp image
     * @param width    Width of the image.
     * @param height   Height of the image.
     * @return int representing the average amount of red in the image.
     */
    fun decodeYUV420SPtoRedBlueGreenAvg(
        yuv420sp: ByteArray?,
        width: Int,
        height: Int,
        type: Int,
    ): Double {
        if (yuv420sp == null) return 0.0
        val frameSize = width * height
        val sum = decodeYUV420SPtoRedBlueGreenSum(yuv420sp, width, height, type)
        return (sum / frameSize).toDouble()
    }

    fun ImageProxy.toBitmap(): Bitmap? {
        val nv21 = yuv420888ToNv21(this)
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        return yuvImage.toBitmap()
    }

    private fun YuvImage.toBitmap(): Bitmap? {
        val out = ByteArrayOutputStream()
        if (!compressToJpeg(Rect(0, 0, width, height), 100, out))
            return null
        val imageBytes: ByteArray = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun yuv420888ToNv21(image: ImageProxy): ByteArray {
        val pixelCount = image.cropRect.width() * image.cropRect.height()
        val pixelSizeBits = ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888)
        val outputBuffer = ByteArray(pixelCount * pixelSizeBits / 8)
        imageToByteBuffer(image, outputBuffer, pixelCount)
        return outputBuffer
    }

    private fun imageToByteBuffer(image: ImageProxy, outputBuffer: ByteArray, pixelCount: Int) {
        assert(image.format == ImageFormat.YUV_420_888)

        val imageCrop = image.cropRect
        val imagePlanes = image.planes

        imagePlanes.forEachIndexed { planeIndex, plane ->
            // How many values are read in input for each output value written
            // Only the Y plane has a value for every pixel, U and V have half the resolution i.e.
            //
            // Y Plane            U Plane    V Plane
            // ===============    =======    =======
            // Y Y Y Y Y Y Y Y    U U U U    V V V V
            // Y Y Y Y Y Y Y Y    U U U U    V V V V
            // Y Y Y Y Y Y Y Y    U U U U    V V V V
            // Y Y Y Y Y Y Y Y    U U U U    V V V V
            // Y Y Y Y Y Y Y Y
            // Y Y Y Y Y Y Y Y
            // Y Y Y Y Y Y Y Y
            val outputStride: Int

            // The index in the output buffer the next value will be written at
            // For Y it's zero, for U and V we start at the end of Y and interleave them i.e.
            //
            // First chunk        Second chunk
            // ===============    ===============
            // Y Y Y Y Y Y Y Y    V U V U V U V U
            // Y Y Y Y Y Y Y Y    V U V U V U V U
            // Y Y Y Y Y Y Y Y    V U V U V U V U
            // Y Y Y Y Y Y Y Y    V U V U V U V U
            // Y Y Y Y Y Y Y Y
            // Y Y Y Y Y Y Y Y
            // Y Y Y Y Y Y Y Y
            var outputOffset: Int

            when (planeIndex) {
                0 -> {
                    outputStride = 1
                    outputOffset = 0
                }
                1 -> {
                    outputStride = 2
                    // For NV21 format, U is in odd-numbered indices
                    outputOffset = pixelCount + 1
                }
                2 -> {
                    outputStride = 2
                    // For NV21 format, V is in even-numbered indices
                    outputOffset = pixelCount
                }
                else -> {
                    // Image contains more than 3 planes, something strange is going on
                    return@forEachIndexed
                }
            }

            val planeBuffer = plane.buffer
            val rowStride = plane.rowStride
            val pixelStride = plane.pixelStride

            // We have to divide the width and height by two if it's not the Y plane
            val planeCrop = if (planeIndex == 0) {
                imageCrop
            } else {
                Rect(
                    imageCrop.left / 2,
                    imageCrop.top / 2,
                    imageCrop.right / 2,
                    imageCrop.bottom / 2
                )
            }

            val planeWidth = planeCrop.width()
            val planeHeight = planeCrop.height()

            // Intermediate buffer used to store the bytes of each row
            val rowBuffer = ByteArray(plane.rowStride)

            // Size of each row in bytes
            val rowLength = if (pixelStride == 1 && outputStride == 1) {
                planeWidth
            } else {
                // Take into account that the stride may include data from pixels other than this
                // particular plane and row, and that could be between pixels and not after every
                // pixel:
                //
                // |---- Pixel stride ----|                    Row ends here --> |
                // | Pixel 1 | Other Data | Pixel 2 | Other Data | ... | Pixel N |
                //
                // We need to get (N-1) * (pixel stride bytes) per row + 1 byte for the last pixel
                (planeWidth - 1) * pixelStride + 1
            }

            for (row in 0 until planeHeight) {
                // Move buffer position to the beginning of this row
                planeBuffer.position(
                    (row + planeCrop.top) * rowStride + planeCrop.left * pixelStride
                )

                if (pixelStride == 1 && outputStride == 1) {
                    // When there is a single stride value for pixel and output, we can just copy
                    // the entire row in a single step
                    planeBuffer.get(outputBuffer, outputOffset, rowLength)
                    outputOffset += rowLength
                } else {
                    // When either pixel or output have a stride > 1 we must copy pixel by pixel
                    planeBuffer.get(rowBuffer, 0, rowLength)
                    for (col in 0 until planeWidth) {
                        outputBuffer[outputOffset] = rowBuffer[col * pixelStride]
                        outputOffset += outputStride
                    }
                }
            }
        }
    }
}
