package com.example.livenativerppg.models.chatPeopleList.data.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.livenativerppg.models.chatPeopleList.data.ChatListRepoImpl
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


private const val TAG = "ChatPeopleListViewModel"
@HiltViewModel
class ChatPeopleListViewModel @Inject constructor(val repoImpl: ChatListRepoImpl) : ViewModel(),
    ChatPeopleListInterface {

    private val _peopleList = MutableStateFlow<List<UserInfo>>(mutableListOf())
    override val peopleList: StateFlow<List<UserInfo>>
        get() = _peopleList.asStateFlow()
    private var _error : FirebaseFirestoreException? = null
    override val error: FirebaseFirestoreException?
        get() = _error


    init {
        repoImpl.getSharedFollowerList({
            viewModelScope.launch {
                _peopleList.emit(it.toList())
                Log.d(TAG, "shared people if patient: $it")
            }
        }, {
            viewModelScope.launch {
                _error = it
            }
        })
    }

    override fun getAllSharedPeople() {
        repoImpl.loadSharedFollower()
    }
}