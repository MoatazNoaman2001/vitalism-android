package com.example.livenativerppg.models.startActivity.ui.fragments.verification.data.models


import com.example.livenativerppg.R
import com.example.livenativerppg.component.di.MyApp
import kotlin.String

data class VerificationModel(
  /**
   * TODO Replace with dynamic value
   */
  var txtOTPVerificatio: String? =
      MyApp.getInstance().resources.getString(R.string.msg_otp_verificatio)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtPleasecheckyo: String? =
      MyApp.getInstance().resources.getString(R.string.msg_please_check_yo)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtOTPCode: String? = MyApp.getInstance().resources.getString(R.string.lbl_otp_code)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtResendcodeto: String? =
      MyApp.getInstance().resources.getString(R.string.lbl_resend_code_to)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtTime: String? = MyApp.getInstance().resources.getString(R.string.lbl_01_26)

)
