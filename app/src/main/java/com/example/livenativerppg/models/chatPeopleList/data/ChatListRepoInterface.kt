package com.example.livenativerppg.models.chatPeopleList.data

import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.google.firebase.firestore.FirebaseFirestoreException


interface ChatListRepoInterface {
    fun getSharedFollowerList(success:((List<UserInfo>) -> Unit), error:  ((FirebaseFirestoreException) -> Unit))
    fun loadSharedFollower()
}

sealed class DocChatResult{
    data class onSuccess(val userInfo: List<UserInfo>): DocChatResult()
    data class onError(val exception: Exception) : DocChatResult()
}