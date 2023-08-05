package com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.viewModel

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.SignUpModel
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.network.GeoApi
import dagger.hilt.android.lifecycle.HiltViewModel
import org.koin.core.component.KoinComponent
import javax.inject.Inject

@HiltViewModel
class SignUpVM @Inject constructor(val repository: SignUpRepository) : ViewModel(), KoinComponent {
    val signUpModel: MutableLiveData<SignUpModel> = MutableLiveData(SignUpModel())
    suspend fun geoInfo() = repository.getGeoInfo()

    var navArguments: Bundle? = null
}