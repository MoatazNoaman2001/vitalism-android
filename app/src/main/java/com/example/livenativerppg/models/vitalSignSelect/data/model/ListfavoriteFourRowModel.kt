package com.example.livenativerppg.models.vitalSignSelect.data.model

import com.example.livenativerppg.R
import com.example.livenativerppg.component.di.MyApp
import kotlin.String

data class ListfavoriteFourRowModel(
  /**
   * TODO Replace with dynamic value
   */
  var txtBloodPressure: String? =
      MyApp.getInstance().resources.getString(R.string.lbl_blood_pressure)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtHealthyandunh: String? =
      MyApp.getInstance().resources.getString(R.string.msg_healthy_and_unh)

)
