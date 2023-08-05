package com.example.livenativerppg.models.MainChatInterface.data.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.livenativerppg.models.MainChatInterface.data.MainChatRepoImpl
import com.example.livenativerppg.models.MainChatInterface.data.MainChatRepoMessageSentResult
import com.example.livenativerppg.models.MainChatInterface.data.model.ChatBuilder
import com.example.livenativerppg.models.MainChatInterface.data.model.Message
import com.google.firebase.database.DatabaseError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainChatViewModel @Inject constructor(val repo: MainChatRepoImpl) : ViewModel(),
    MainChatViewModelInterface {
    override suspend fun getChatBuilder(partnerID: String) = repo.getChatBuilder(partnerID)
    override suspend fun CreateChatBuilder(partnerID: String) = repo.CreateChatBuilder(partnerID)


    override fun initRealTimeMessaging(builder: ChatBuilder) {
        repo.initMessagesTracker(builder)
    }

    private val _messageList = MutableStateFlow<List<Message>>(mutableListOf())
    override val messageList: StateFlow<List<Message>>
        get() = _messageList.asStateFlow()

    private var _error_d: DatabaseError? = null
    override val error_d: DatabaseError?
        get() = _error_d

    init {
        repo.getMessagesList(
            {
                viewModelScope.launch {
                    _messageList.emit(it.toList())
                }
            }, {
                _error_d = it
            }
        )
    }

    override suspend fun sendTextMessage(message: Message) = repo.sendTextMessage(message)
}