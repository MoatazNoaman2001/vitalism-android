package com.example.livenativerppg.component.base

import com.example.livenativerppg.component.db.models.NotificationConnectionRequest
import com.example.livenativerppg.component.db.models.NotificationMessage
import com.example.livenativerppg.component.db.models.NotificationRppgVitalSignMeasured
import com.example.livenativerppg.models.MainChatInterface.data.model.Message
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationApi {

    companion object {
        val baseUrl = "https://fcm.googleapis.com/fcm/"
    }

    @Headers(
        "Content-Type: application/json",
        "Authorization: key=AAAA7LwBIJs:APA91bEOYW28sGfDTsuCLDGmPjWGXIF-1m4Cr-TrXedYsqO1n-jtQ413xT1pb6ATPQaBQeJjj6J5e3ReFR0HWm80kFj3wMfM0sYLaOU25ET_wtE9UzPJAabQpq7HfPBtLWycd-DCMZKu"
    )
    @POST("send")
    fun sendConnectRequestNotification(@Body notificationConnectionRequest: NotificationConnectionRequest) :Call<ResponseBody>

    @Headers(
        "Content-Type: application/json",
        "Authorization: key=AAAA7LwBIJs:APA91bEOYW28sGfDTsuCLDGmPjWGXIF-1m4Cr-TrXedYsqO1n-jtQ413xT1pb6ATPQaBQeJjj6J5e3ReFR0HWm80kFj3wMfM0sYLaOU25ET_wtE9UzPJAabQpq7HfPBtLWycd-DCMZKu"
    )
    @POST("send")
    fun sendMessageNotification(@Body notificationMessage: NotificationMessage) : Call<ResponseBody>

    @Headers(
        "Content-Type: application/json",
        "Authorization: key=AAAA7LwBIJs:APA91bEOYW28sGfDTsuCLDGmPjWGXIF-1m4Cr-TrXedYsqO1n-jtQ413xT1pb6ATPQaBQeJjj6J5e3ReFR0HWm80kFj3wMfM0sYLaOU25ET_wtE9UzPJAabQpq7HfPBtLWycd-DCMZKu"
    )
    @POST("send")
    fun sendMeasureNotification(@Body notificationRppgVitalSignMeasured: NotificationRppgVitalSignMeasured) : Call<ResponseBody>
}