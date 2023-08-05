package com.example.livenativerppg.models.vitalSignSelect.data.model

import android.util.Log
import androidx.room.withTransaction
import com.example.livenativerppg.component.db.AppDatabase
import com.example.livenativerppg.component.db.models.ConnectRequest
import com.example.livenativerppg.component.db.models.Notification
import com.example.livenativerppg.component.di.MyRequestsRef
import com.example.livenativerppg.component.utility.networkBoundResource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val TAG = "HomeRepo"
class HomeRepo @Inject constructor(@MyRequestsRef val connectionCollection: CollectionReference, val appDatabase: AppDatabase) {

    val notifications = networkBoundResource(
        query = {
            appDatabase.getNotificationDao().allNotifications
        },
        fetch = {
            connectionCollection.get().await().documents.map { it.toObject(ConnectRequest::class.java) }.filter { it?.SenderId != FirebaseAuth.getInstance().currentUser?.uid!! }.toList()
        },
        saveFetchResult = {
            Log.d(TAG, ": ${it.map { it?.RequestDate }.joinToString { it.toString() }}")

            appDatabase.withTransaction {
                appDatabase.getNotificationDao().deleteAllNotifications()
                appDatabase.getNotificationDao().insertNotifications(it.map {
                    Notification(it!! , "Connection Request" , "${it.SenderId} is requesting to be in connect with you")
                }.toList())
            }
        }
    )

}