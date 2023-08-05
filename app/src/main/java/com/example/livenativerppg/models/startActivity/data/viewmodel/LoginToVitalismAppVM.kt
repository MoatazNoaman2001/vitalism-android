package com.example.livenativerppg.models.startActivity.data.viewmodel

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.livenativerppg.models.startActivity.data.model.LoginToVitalismAppModel
import org.koin.core.component.KoinComponent

class LoginToVitalismAppVM : ViewModel(), KoinComponent {
  val loginToVitalismAppModel: MutableLiveData<LoginToVitalismAppModel> =
      MutableLiveData(LoginToVitalismAppModel())

  var navArguments: Bundle? = null
}
