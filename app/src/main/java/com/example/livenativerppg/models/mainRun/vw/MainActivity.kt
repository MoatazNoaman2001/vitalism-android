package com.example.livenativerppg.models.mainRun.vw

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.example.livenativerppg.R
import com.example.livenativerppg.component.*
import com.example.livenativerppg.component.natives.*
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.ActivityMainBinding
import com.example.livenativerppg.models.resultPage.ui.ResultMeasurementActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.firestore.FirebaseFirestore
import org.opencv.android.*
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import org.opencv.core.*
import org.opencv.core.CvType.CV_8U
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashMap


private const val TAG = "MainActivity"
class MainActivity : CameraActivity(), CvCameraViewListener2 {

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
    private var resultsList : RPPGListenerList = RPPGListenerList()

    private var facedetector: CascadeClassifier? = null
    private var fileDir: File? = null
//    private var results :ArrayList<RPPGResult>? = ArrayList()
    lateinit var user:FirebaseUser

    private val PARTIAl_IP_ADDRESS: Pattern =
        Pattern.compile("^((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])\\.){0,3}" +
                "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])){0,1}$")


    private var mOpenCvCameraView: CameraBridgeViewBase? = null
    private var mRgba: Mat? = null
    private var mGray: Mat? = null
    private var time: Double = 0.0


    private var serverAddress: String? = null
    private var clientConnected = false

    lateinit var rPPG : RPPG
    private var camIndex:Int = 1

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

    private fun loadFacelib() {
        try {
            fileDir = getDir(FACE_DIR, MODE_PRIVATE)
            val faceModel: File = File(fileDir, FACE_MODEL)
            val outputStream = FileOutputStream(faceModel)
            val inputStream = resources.openRawResource(R.raw.haarcascade_frontalface_alt2)
            val byteBuffer = ByteArray(byteSize)
            var value: Int
            while (inputStream.read(byteBuffer).also { value = it } != -1) {
                outputStream.write(byteBuffer, 0, value)
            }
            outputStream.close()
            inputStream.close()
            facedetector = CascadeClassifier(faceModel.absolutePath)
            facedetector?.load(faceModel.absolutePath)
            if (facedetector!!.empty()) {
                Toast.makeText(this, "face detector is empty", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "loadFacelib:  its empty $facedetector")
            }
        } catch (e: IOException) {
            Log.d(TAG, "loadFacelib: error load facedetector: " + e.message)
            e.printStackTrace()
        } catch (e: CvException) {
            Log.d(TAG, "loadFacelib: error load facedetector: " + e.message)
            e.printStackTrace()
        }

    }

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")
                    loadFacelib()
                    if (facedetector!!.empty()) {
                        facedetector = null;
                    } else {
                        fileDir?.delete()
                    }
                    rPPG = RPPG()
                    mOpenCvCameraView!!.enableView()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }


    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = FirebaseAuth.getInstance().currentUser!!

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseFirestore.getInstance()
            .collection(Variables.FireStoreUsersRoot)
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection(Variables.PARMS)
            .document(Variables.Activations)
            .set(HashMap<String , Boolean>().apply {
                put(Variables.isUserActive , true)
                put(Variables.RPPG_ACTIVE , true)
            })
//        FirebaseFirestore.getInstance()
//            .collection(Variables.FireStoreUsersRoot)
//            .document(FirebaseAuth.getInstance().currentUser!!.uid)
//            .collection(Variables.PARMS)
//            .document(Variables.Activations)
//            .get()
//            .addOnSuccessListener {
//                if (it.exists()) {
//                    val rppg_active:Boolean = it.get(Variables.RPPG_ACTIVE , Boolean::class.java)!!
//                    if (!rppg_active) {
//                        //navigate
//                    }
//                }
//            }
//            .set(HashMap<String , Boolean>().apply {
//                put(Variables.isUserActive , true)
//                put(Variables.RPPG_ACTIVE , true)
//            })

        mOpenCvCameraView = binding.fdActivitySurfaceView

        mOpenCvCameraView?.setCameraIndex(camIndex)
        mOpenCvCameraView?.setCvCameraViewListener(this)
        binding.floatingActionButton.setOnClickListener {
            camIndex = (if (camIndex == 1) 0 else 1)
            mOpenCvCameraView?.disableView()
            mOpenCvCameraView?.setCameraIndex(camIndex)
            mOpenCvCameraView?.enableView()
        }
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
        FirebaseFirestore.getInstance()
            .collection(Variables.FireStoreUsersRoot)
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection(Variables.PARMS)
            .document(Variables.Activations)
            .set(HashMap<String , Boolean>().apply {
                put(Variables.isUserActive , false)
                put(Variables.RPPG_ACTIVE , false)
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView?.disableView()
            if (fileDir != null && fileDir!!.exists()) fileDir!!.delete()
            FirebaseDatabase.getInstance()
                .getReference(Variables.FireStoreUsersRoot)
                .child(user.uid)
                .child(SimpleDateFormat("dd-MM-yyyy EEE").format(Calendar.getInstance().time))
                .updateChildren(mapOf(
                    SimpleDateFormat("hh:mm").format(Calendar.getInstance().time)
                            to resultsList.results
                ))
                .addOnSuccessListener {
                    Toast.makeText(this@MainActivity , "firebase updated" , Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Log.d(TAG, "onCameraViewStopped: error: ${it.message}")
                    Toast.makeText(this@MainActivity , "firebase failed to update" , Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        mRgba = Mat(width, height, CvType.CV_8UC4)
        mGray = Mat(width, height, CvType.CV_8UC4)
        val cascadeDir = getDir("cascade", MODE_PRIVATE)
        Log.d(TAG, "onCameraViewStarted: start camera, width: $width, height: $height ")
        // Initialise rPPG
        try {
            rPPG.load(
                object :RPPGListener {
                    override fun onRPPGResult(result: RPPGResult?) {
                        Log.d(TAG, "onRPPGResult: Result: $result")
                        resultsList.addResult(result!!)
                        if (resultsList.getSize() == 60){
                            startActivity(Intent(this@MainActivity , ResultMeasurementActivity::class.java)
                                .putParcelableArrayListExtra("results" , resultsList.results))
                            finish()
                        }
                    }

                    override fun onNewPointGenerated(signalPoint: SignalPoint) {

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
                applicationContext.getExternalFilesDir(null)!!.absolutePath,
                loadCascadeFile(cascadeDir,
                    R.raw.haarcascade_frontalface_alt,
                    "haarcascade_frontalface_alt.xml").toString(),
                LOG,
                GUI,
            false, 0.0,0.0,0f,0f)
            Log.i(TAG, "Loaded rPPG")
        } catch (e: IOException) {
            Log.e(TAG,
                "Failed to load cascade. Exception thrown: $e")
        }

        cascadeDir.delete()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    override fun onCameraViewStopped() {


        mRgba?.release()
        mGray?.release()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        time = (Core.getTickCount() *1000 ) / Core.getTickFrequency();

        mRgba?.release();
        mGray?.release();


        mRgba = inputFrame?.rgba()
        mGray = inputFrame?.gray()

        val NewMat = Mat.ones(mRgba!!.size(), mRgba!!.type())

        Imgproc.resize(mRgba, NewMat , Size(mRgba!!.height().toDouble()  , mRgba!!.width().toDouble()))
        Core.transpose(NewMat , NewMat)
        Core.flip(NewMat , NewMat , -1)

        Imgproc.cvtColor(NewMat , mGray , Imgproc.COLOR_RGBA2GRAY)

        Log.d(TAG, "onCameraViewStarted: start camera, width: ${mRgba?.width()}, height: ${mRgba?.height()} ")
        // Send the frame to rPPG for processing
        // To C++
        try {
            rPPG.processFrame(NewMat!!.nativeObjAddr, mGray!!.nativeObjAddr, time)
        }catch (e:Exception){
            e.printStackTrace()
        }
        //process roi

//        var rgb = Mat()
//        ProcessSkinNatively(mRgba!!.nativeObjAddr , rgb.nativeObjAddr)

//        mRgba = drawFaceRectangle(inputFrame!!.rgba());
        return NewMat!!
    }

    external fun ProcessSkinNatively(frame: Long, dst: Long)
    external fun detectLandmarks(yuv:ByteArray, rotation:Int,width: Int, height: Int, left:Int,top:Int,right:Int,bottom:Int) : LongArray
    private fun ProcessRoi(frame: Mat): Mat {
        val hsv = Mat()
        val ycbcr = Mat()
        val globalMask = Mat()
        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV)
        Core.inRange(hsv, Scalar(0.0, 15.0, 0.0), Scalar(17.0, 170.0, 255.0), hsv)
        Imgproc.morphologyEx(hsv, hsv, Imgproc.MORPH_OPEN, Mat.ones(3, 3, CV_8U))

        Imgproc.cvtColor(frame, ycbcr, Imgproc.COLOR_BGR2YCrCb)
        Core.inRange(ycbcr, Scalar(0.0, 135.0, 85.0), Scalar(255.0, 180.0, 135.0), ycbcr)
        Imgproc.morphologyEx(ycbcr, ycbcr, Imgproc.MORPH_OPEN, Mat.ones(3, 3, CV_8U))

        Core.bitwise_and(ycbcr, hsv, globalMask)
        Imgproc.medianBlur(globalMask, globalMask, 3)
        Imgproc.morphologyEx(globalMask, globalMask, Imgproc.MORPH_OPEN, Mat.ones(4, 4, CV_8U))


        val size = frame.size()
        for (i in 0 until size.width.toInt()) {
            for (j in 0 until size.height.toInt()) {
                if (globalMask[j, i][0] != 0.0) {
                    frame[j, i][0] = 0.0
                    frame[j, i][1] = 0.0
                    frame[j, i][2] = 0.0
                }
            }
        }
        return frame
    }

    private fun drawFaceRectangle(frame: Mat): Mat {
        val rects = MatOfRect()
        facedetector?.detectMultiScale(frame, rects)
        val imageRatio = 1.0f

//        frame.submat(rects.toArray().filter { rect -> rect.area()/ 100 > 100 }.sortedBy { rect -> rect.area() }
//            .get(0)).copyTo(frame)

        for (rect in rects.toArray()) {
            var x = 0.0
            var y = 0.0
            var w = 0.0
            var h = 0.0
            if (imageRatio.toDouble() == 1.0) {
                x = rect.x.toDouble()
                y = rect.y.toDouble()
                w = x + rect.width
                h = y + rect.height
            } else {
                x = (rect.x / imageRatio).toDouble()
                y = (rect.y / imageRatio).toDouble()
                w = x + rect.width / imageRatio
                h = y + rect.height / imageRatio
            }
            Imgproc.rectangle(
                frame,
                Point(x, y),
                Point(w, h),
                Scalar(0.0, 255.0, 0.0),
                5
            )
        }
        return frame
    }

    /**
     * A native method that is implemented by the 'livenativerppg' native library,
     * which is packaged with this application.
     */

    companion object {
        // Used to load the 'livenativerppg' library on application startup.
        init {
            System.loadLibrary("livenativerppg")
        }
    }

}