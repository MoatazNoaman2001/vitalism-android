package com.example.livenativerppg.component.db.models

import com.example.livenativerppg.models.MainChatInterface.data.model.Message

data class NotificationMessage(
    val registration_ids:Array<String>,
    val notification: HashMap<String, String>,
    val data: Message
) {
}