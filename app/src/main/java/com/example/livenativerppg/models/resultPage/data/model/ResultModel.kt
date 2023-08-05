package com.example.livenativerppg.models.resultPage.data.model

import com.example.livenativerppg.R
import com.example.livenativerppg.component.di.MyApp
import kotlin.String

data class ResultModel(
  /**
   * TODO Replace with dynamic value
   */
  var txtYourrespirator: String? =
      MyApp.getInstance().resources.getString(R.string.msg_your_respirator)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtYourResult: String? = MyApp.getInstance().resources.getString(R.string.lbl_your_result)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtRespiratoryrat: String? =
      MyApp.getInstance().resources.getString(R.string.msg_respiratory_rat2)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtCaregiverscan: String? =
      MyApp.getInstance().resources.getString(R.string.msg_caregivers_can)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtTheuserhasbe: String? =
      MyApp.getInstance().resources.getString(R.string.msg_the_user_has_be)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtAddtoEMR: String? = MyApp.getInstance().resources.getString(R.string.lbl_add_to_emr)

)
