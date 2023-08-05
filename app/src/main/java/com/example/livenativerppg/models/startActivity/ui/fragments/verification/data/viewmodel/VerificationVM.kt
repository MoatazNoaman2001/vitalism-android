package com.example.livenativerppg.models.startActivity.ui.fragments.verification.data.viewmodel

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.livenativerppg.models.startActivity.ui.fragments.verification.data.models.VerificationModel
import org.koin.core.component.KoinComponent

class VerificationVM : ViewModel(), KoinComponent {
  val verificationModel: MutableLiveData<VerificationModel> = MutableLiveData(VerificationModel())

  var navArguments: Bundle? = null
}
