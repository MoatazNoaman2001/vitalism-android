package com.example.livenativerppg.models.follow.data.viewModel

import androidx.lifecycle.ViewModel
import com.example.livenativerppg.component.di.MyConnectionsRef
import com.example.livenativerppg.models.follow.data.FollowRepo
import com.google.firebase.firestore.CollectionReference
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FollowVM @Inject constructor(val followRepo: FollowRepo) : ViewModel() {
    suspend fun connections() = followRepo.connections()
    suspend fun followers() = followRepo.followers()
}