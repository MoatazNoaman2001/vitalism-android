package com.example.livenativerppg.models.vitalSignDetails.data.viewmodel

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.livenativerppg.models.vitalSignDetails.data.model.DetailsModel
import org.koin.core.component.KoinComponent

class DetailsVM : ViewModel(), KoinComponent {
  val detailsModel: MutableLiveData<DetailsModel> = MutableLiveData(DetailsModel())

  var navArguments: Bundle? = null
}
