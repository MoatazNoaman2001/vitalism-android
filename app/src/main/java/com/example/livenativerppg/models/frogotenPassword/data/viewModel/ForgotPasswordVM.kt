package com.example.livenativerppg.models.frogotenPassword.data.viewModel

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.livenativerppg.models.frogotenPassword.data.model.ForgotPasswordModel
import org.koin.core.component.KoinComponent

class ForgotPasswordVM : ViewModel(), KoinComponent {
  val forgotPasswordModel: MutableLiveData<ForgotPasswordModel> =
      MutableLiveData(ForgotPasswordModel())

  var navArguments: Bundle? = null
}
