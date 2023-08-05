package com.example.livenativerppg.models.UserFollowedDetails.data.viewModel

import androidx.lifecycle.ViewModel
import com.example.livenativerppg.models.UserFollowedDetails.data.UserFollowDetailsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class UserFollowDetailsVM @Inject constructor(val userFollowDetailsRepo: UserFollowDetailsRepo) : ViewModel() {
}