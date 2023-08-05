package com.example.livenativerppg.models.notification.data

import android.util.Log
import androidx.room.withTransaction
import com.example.livenativerppg.component.db.AppDatabase
import com.example.livenativerppg.component.db.models.ConnectRequest
import com.example.livenativerppg.component.db.models.Notification
import com.example.livenativerppg.component.di.MyRequestsRef
import com.example.livenativerppg.component.utility.networkBoundResource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val TAG = "NotificationRepo"
class NotificationRepo @Inject constructor(@MyRequestsRef val requestsRef :CollectionReference, val appDatabase: AppDatabase) {

    val notifications = networkBoundResource(
        query = {
            appDatabase.getNotificationDao().allNotifications
        },
        fetch = {
            requestsRef.get().await().documents.map { it.toObject(ConnectRequest::class.java) }.filter { it?.SenderId != FirebaseAuth.getInstance().currentUser?.uid!! }.toList()
        },
        saveFetchResult = {
            Log.d(TAG, ": ${it.map { it?.RequestDate }.joinToString { it.toString() }}")
            appDatabase.withTransaction {
                appDatabase.getNotificationDao().deleteAllNotifications()
                appDatabase.getNotificationDao().insertNotifications(it.map {
                    Notification(it!! , "${it.ConnectType} Request" , "${it.SenderId} is requesting to ${it.ConnectType} with you")
                }.toList())
            }
        }
    )

    fun UpdateNotification(notification: Notification){
        val disposable= Observable.just<Notification>(notification)
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(Schedulers.io())
            .subscribe({
                appDatabase.getNotificationDao().updateNotification(notification)
            } , {
                Log.d(TAG, "UpdateNotification: error: ${it.message}")
            } , {
                Log.d(TAG, "UpdateNotification: succeeded to update")
            })
    }

}