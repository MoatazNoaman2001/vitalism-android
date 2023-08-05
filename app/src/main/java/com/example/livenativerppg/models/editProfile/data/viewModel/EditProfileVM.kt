package com.example.livenativerppg.models.editProfile.data.viewModel

import androidx.lifecycle.ViewModel
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.viewModel.SignUpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class EditProfileVM @Inject constructor(val respiratory:  SignUpRepository) : ViewModel() {
    suspend fun geoInfo() = respiratory.getGeoInfo()
}