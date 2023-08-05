package com.example.livenativerppg.component.db.models

data class NotificationConnectionRequest(
    val to: String,
    val notification: HashMap<String, String>,
    val data: ConnectRequest,
) : java.io.Serializable