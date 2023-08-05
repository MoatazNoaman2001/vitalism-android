package com.example.livenativerppg.models.MainChatInterface.data.model

data class ChatBuilder(
    val initiator: String,
    val participant: List<String>?,
    val chatUniqueKey: String,
) {
    constructor() : this(initiator = "", participant =  emptyList<String>(), chatUniqueKey ="")
}