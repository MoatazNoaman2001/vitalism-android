package com.example.livenativerppg.models.profile.data.viewModel

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.livenativerppg.models.profile.data.model.ProfileModel
import com.example.livenativerppg.models.profile.data.model.ProfileRowModel
import org.koin.core.component.KoinComponent
import kotlin.collections.MutableList

class ProfileVM : ViewModel(), KoinComponent {
  val profileModel: MutableLiveData<ProfileModel> = MutableLiveData(ProfileModel())

  var navArguments: Bundle? = null

  val profileList: MutableLiveData<MutableList<ProfileRowModel>> = MutableLiveData(mutableListOf())
}
