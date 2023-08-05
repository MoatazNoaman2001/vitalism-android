package com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model

import com.example.livenativerppg.R
import com.example.livenativerppg.component.di.MyApp
import kotlin.String

data class SignUpModel(
  /**
   * TODO Replace with dynamic value
   */
  var txtSignupnow: String? = MyApp.getInstance().resources.getString(R.string.lbl_sign_up_now)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtPleasefillthe: String? =
      MyApp.getInstance().resources.getString(R.string.msg_please_fill_the)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtGroupTwenty: String? =
      MyApp.getInstance().resources.getString(R.string.name)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtGroupSixteen: String? =
      MyApp.getInstance().resources.getString(R.string.msg_birth_day_dd_m)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtGroupSeventeen: String? = MyApp.getInstance().resources.getString(R.string.lbl_country)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtGroupEighteen: String? = MyApp.getInstance().resources.getString(R.string.lbl_blood_type)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtGroupNineteen: String? = MyApp.getInstance().resources.getString(R.string.lbl_diagnoses)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtEmail: String? = MyApp.getInstance().resources.getString(R.string.msg_vitalism_exampl)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtTimeZone: String? = MyApp.getInstance().resources.getString(R.string.msg_password_must_b)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtConfirmation: String? =
      MyApp.getInstance().resources.getString(R.string.msg_already_have_an)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtSignin: String? = MyApp.getInstance().resources.getString(R.string.lbl_sign_in)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtOrconnect: String? = MyApp.getInstance().resources.getString(R.string.lbl_or_connect)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var etFrameSixValue: String? = null,
  /**
   * TODO Replace with dynamic value
   */
  var etFrameSixOneValue: String? = null
)
