package com.example.livenativerppg.models.notification.data.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.livenativerppg.models.notification.data.NotificationRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Notification
import javax.inject.Inject


@HiltViewModel
class NotificationVM @Inject constructor(val notificationRepo: NotificationRepo) : ViewModel() {
    val notification = notificationRepo.notifications.asLiveData()
    fun updateNotification(notification: com.example.livenativerppg.component.db.models.Notification) = notificationRepo.UpdateNotification(notification)
}