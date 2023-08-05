package com.example.livenativerppg.models.startActivity.data.model


import com.example.livenativerppg.R
import com.example.livenativerppg.component.di.MyApp
import kotlin.String

data class LoginToVitalismAppModel(
  /**
   * TODO Replace with dynamic value
   */
  var txtWelcome: String? = MyApp.getInstance().resources.getString(R.string.lbl_welcome)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtBysigninginy: String? =
      MyApp.getInstance().resources.getString(R.string.msg_by_signing_in_y)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtRememberpasswo: String? =
      MyApp.getInstance().resources.getString(R.string.msg_remember_passwo)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtForgetpassword: String? =
      MyApp.getInstance().resources.getString(R.string.lbl_forget_password)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtRegister: String? = MyApp.getInstance().resources.getString(R.string.lbl_register)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtOrconnectwith: String? =
      MyApp.getInstance().resources.getString(R.string.lbl_or_connect_with)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtLoginwittouch: String? =
      MyApp.getInstance().resources.getString(R.string.msg_login_wit_touch)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var etFrame451Value: String? = null,
  /**
   * TODO Replace with dynamic value
   */
  var etFrame452Value: String? = null,
  /**
   * TODO Replace with dynamic value
   */
  var etFrameSixValue: String? = null,
  /**
   * TODO Replace with dynamic value
   */
  var etFrameSixOneValue: String? = MyApp.getInstance().resources.getString(R.string.msg_log_in_with_fac)
)
