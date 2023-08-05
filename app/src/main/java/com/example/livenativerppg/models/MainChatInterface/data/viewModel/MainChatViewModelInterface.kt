package com.example.livenativerppg.models.MainChatInterface.data.viewModel

import com.example.livenativerppg.models.MainChatInterface.data.MainChatRepoBuilderResult
import com.example.livenativerppg.models.MainChatInterface.data.MainChatRepoMessageSentResult
import com.example.livenativerppg.models.MainChatInterface.data.model.ChatBuilder
import com.example.livenativerppg.models.MainChatInterface.data.model.Message
import com.google.firebase.database.DatabaseError
import kotlinx.coroutines.flow.StateFlow

interface MainChatViewModelInterface {
    suspend fun getChatBuilder(partnerID : String) : ChatBuilder?
    suspend fun CreateChatBuilder(partnerID: String) : MainChatRepoBuilderResult

    fun initRealTimeMessaging(builder : ChatBuilder)

    val messageList : StateFlow<List<Message>>
    val error_d : DatabaseError?

    suspend fun sendTextMessage(message: Message): MainChatRepoMessageSentResult


}
