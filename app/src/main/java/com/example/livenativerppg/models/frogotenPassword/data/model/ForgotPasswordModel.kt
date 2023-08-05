package com.example.livenativerppg.models.frogotenPassword.data.model

import com.example.livenativerppg.R
import com.example.livenativerppg.component.di.MyApp
import kotlin.String

data class ForgotPasswordModel(
  var txtForgotpassword: String? =
      MyApp.getInstance().resources.getString(R.string.lbl_forgot_password)
  ,
  var txtLanguage: String? = MyApp.getInstance().resources.getString(R.string.msg_enter_your_emai)
  ,
  var etEmailValue: String? = null
)
