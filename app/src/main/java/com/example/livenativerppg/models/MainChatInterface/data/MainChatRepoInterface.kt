package com.example.livenativerppg.models.MainChatInterface.data

import com.example.livenativerppg.models.MainChatInterface.data.model.ChatBuilder
import com.example.livenativerppg.models.MainChatInterface.data.model.Message
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.firestore.FirebaseFirestoreException

interface MainChatRepoInterface {
    suspend fun getChatBuilder(partnerID: String): ChatBuilder?
    suspend fun CreateChatBuilder (partnerID: String) : MainChatRepoBuilderResult

    fun getMessagesList(success: ((List<Message>) -> Unit), error: ((DatabaseError) -> Unit)?)
    fun initMessagesTracker(builder : ChatBuilder)

    suspend fun sendTextMessage(message: Message) : MainChatRepoMessageSentResult
}

sealed class MainChatRepoBuilderResult{
    data class OnSuccess(val Builder: ChatBuilder) : MainChatRepoBuilderResult()
    data class OnError(val error : Exception): MainChatRepoBuilderResult()
}

sealed class MainChatRepoMessageSentResult{
    data class OnSuccess(val message: Message) : MainChatRepoMessageSentResult()
    data class OnError(val error: Exception) : MainChatRepoMessageSentResult()
}