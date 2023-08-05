package com.example.livenativerppg.models.cameraActivtiy.view

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Rect
import android.hardware.Camera
import android.net.ConnectivityManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import com.dev.anzalone.luca.facelandmarks.camera.CameraPreview
import com.dev.anzalone.luca.facelandmarks.camera.CameraUtils
import com.dev.anzalone.luca.facelandmarks.utils.Downloader
import com.dev.anzalone.luca.facelandmarks.utils.Model
import com.dev.anzalone.luca.facelandmarks.utils.UserDialog
import com.dev.anzalone.luca.facelandmarks.utils.mapTo
import com.example.livenativerppg.R
import com.example.livenativerppg.databinding.ActivityCameraBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import org.json.JSONArray
import java.io.File
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.coroutineContext

class CameraActivity : Activity(), Camera.PreviewCallback, Camera.FaceDetectionListener {

    lateinit var cameraBinding: ActivityCameraBinding


    init {
        System.loadLibrary("native-lib")
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        cameraBinding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(cameraBinding.root)

    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
    }

    override fun onFaceDetection(faces: Array<out Camera.Face>?, camera: Camera?) {
    }
}

