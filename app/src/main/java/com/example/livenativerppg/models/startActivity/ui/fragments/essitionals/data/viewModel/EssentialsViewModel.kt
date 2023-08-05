package com.example.livenativerppg.models.startActivity.ui.fragments.essitionals.data.viewModel

import androidx.lifecycle.ViewModel
import com.example.livenativerppg.models.startActivity.ui.fragments.essitionals.data.EssentialRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class EssentialsViewModel @Inject constructor(val essentialRepo: EssentialRepo): ViewModel(){
    suspend fun geoInfo() = essentialRepo.getGeoInfo()
}