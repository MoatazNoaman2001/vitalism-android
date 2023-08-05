package com.example.livenativerppg.models.chatPeopleList.data.viewModel

import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.StateFlow

interface ChatPeopleListInterface {

    val peopleList : StateFlow<List<UserInfo>>
    val error: FirebaseFirestoreException?

    fun getAllSharedPeople()
}