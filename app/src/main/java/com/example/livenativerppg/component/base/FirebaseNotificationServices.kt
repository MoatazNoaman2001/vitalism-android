package com.example.livenativerppg.component.base

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.room.Room.databaseBuilder
import com.example.livenativerppg.R
import com.example.livenativerppg.commons.makeToast
import com.example.livenativerppg.component.db.AppDatabase
import com.example.livenativerppg.component.db.converters.DateConverter
import com.example.livenativerppg.component.db.converters.StringListConverter
import com.example.livenativerppg.component.db.models.ConnectRequest
import com.example.livenativerppg.component.db.models.Notification
import com.example.livenativerppg.component.db.models.VitalSignMeasurement
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.models.mainAppPage.ui.MainAppPageActivity
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.JsonElement


private const val TAG = "FirebaseNotificationSer"

class FirebaseNotificationServices : FirebaseMessagingService() {

    var db: AppDatabase? = null

    var NotificationID: Int = 0

    override fun onCreate() {
        super.onCreate()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("TAG", "onNewToken: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (db == null) {
            databaseBuilder(applicationContext, AppDatabase::class.java, "app Database")
                .fallbackToDestructiveMigration()
                .addTypeConverter(DateConverter())
                .addTypeConverter(StringListConverter())
                .build()
        }

        if(message.notification?.title == "Follow Request") {
            val gson = Gson()
            val element: JsonElement = gson.toJsonTree(message.data)
            val connectRequest: ConnectRequest = gson.fromJson(element, ConnectRequest::class.java)
            Log.d(TAG, "onMessageReceived: $connectRequest")

            if (connectRequest.SenderId == FirebaseAuth.getInstance().currentUser?.uid)
                return

            FirebaseFirestore.getInstance()
                .collection(Variables.FireStoreUsersRoot)
                .document(connectRequest.SenderId)
                .get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        val info = it.toObject(UserInfo::class.java)
                        makeToast(applicationContext , info?.name + " send you connection request")

                        val pendingIntent = PendingIntent.getActivity(
                            applicationContext,
                            0,
                            Intent(applicationContext, MainAppPageActivity::class.java),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        val channel: NotificationChannel;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            channel = NotificationChannel(
                                connectRequest.SenderId,
                                "channel 1",
                                NotificationManager.IMPORTANCE_DEFAULT
                            )
                            channel.description = "channel for connect request"
                            val notificationManger =
                                getSystemService(NOTIFICATION_SERVICE) as NotificationManager

                            notificationManger.createNotificationChannel(channel)
                        }

                        val notificationBuilder =
                            NotificationCompat.Builder(this, connectRequest.SenderId)
                                .setSmallIcon(R.drawable.application_icon)
                                .setLargeIcon(
                                    BitmapFactory.decodeResource(
                                        resources,
                                        R.drawable.application_icon
                                    )
                                )
                                .setContentTitle(message.notification?.title)
                                .setContentText(message.notification?.body)
                                .setAutoCancel(true)
                                .addAction(
                                    0,
                                    "Accept",
                                    PendingIntent.getBroadcast(
                                        applicationContext,
                                        0,
                                        Intent(
                                            applicationContext,
                                            AcceptBroadCast::class.java
                                        ).putExtra("info", info)
                                            .putExtra("connect", connectRequest),
                                        PendingIntent.FLAG_IMMUTABLE
                                    )
                                )
                                .setSound(defaultSoundUri)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                .setStyle(
                                    NotificationCompat.BigTextStyle()
                                        .bigText(message.notification?.body)
                                )
                                .setContentIntent(pendingIntent)

                        val notificationManager =
                            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        if (NotificationID > 1073741824) {
                            NotificationID = 0
                        }
                        notificationManager.notify(NotificationID++, notificationBuilder.build())


                        Thread(
                            {
                                db?.getNotificationDao()?.insertNotification(
                                    Notification(
                                        connectRequest,
                                        message.notification?.title!!,
                                        message.notification?.body!!,
                                        message.notification?.sound!!,
                                    )
                                )
                            }, "insert Notification thread"
                        ).start()
                    }
                }
        }else if (message.notification?.title == "vital notification"){
            val gson = Gson()
            val element: JsonElement = gson.toJsonTree(message.data)
            val vitalSignMeasurement: VitalSignMeasurement = gson.fromJson(element, VitalSignMeasurement::class.java)
            Log.d(TAG, "onMessageReceived: $vitalSignMeasurement")


        }else if (message.notification?.title == "message notification"){
            val gson = Gson()
            val element: JsonElement = gson.toJsonTree(message.data)
            val msg: com.example.livenativerppg.models.MainChatInterface.data.model.Message = gson.fromJson(element, com.example.livenativerppg.models.MainChatInterface.data.model.Message::class.java)
            Log.d(TAG, "onMessageReceived: $message")

            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                0,
                Intent(applicationContext, MainAppPageActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val channel: NotificationChannel;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                channel = NotificationChannel(
                    msg.SenderID,
                    "channel 1",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                channel.description = "channel for connect request"
                val notificationManger =
                    getSystemService(NOTIFICATION_SERVICE) as NotificationManager

                notificationManger.createNotificationChannel(channel)
            }

            val notificationBuilder =
                NotificationCompat.Builder(this, msg.SenderID)
                    .setSmallIcon(R.drawable.application_icon)
                    .setLargeIcon(
                        BitmapFactory.decodeResource(
                            resources,
                            R.drawable.application_icon
                        )
                    )
                    .setContentTitle(message.notification?.title)
                    .setContentText(message.notification?.body)
                    .setAutoCancel(true)
                    .addAction(
                        0,
                        "reply",
                        PendingIntent.getBroadcast(
                            applicationContext,
                            0,
                            Intent(
                                applicationContext,
                                AcceptBroadCast::class.java
                            ),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                    .setSound(defaultSoundUri)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(message.notification?.body)
                    )
                    .setContentIntent(pendingIntent)

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (NotificationID > 1073741824) {
                NotificationID = 0
            }
            notificationManager.notify(NotificationID++, notificationBuilder.build())

        }
    }
}