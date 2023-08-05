package com.example.livenativerppg.models.vitalSignSelect.data.model

import com.example.livenativerppg.R
import com.example.livenativerppg.component.di.MyApp
import kotlin.String

data class ListfavoriteRowModel(
  /**
   * TODO Replace with dynamic value
   */
  var txtHeartRate: String? = MyApp.getInstance().resources.getString(R.string.lbl_heart_rate)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtAnormalrestin: String? =
      MyApp.getInstance().resources.getString(R.string.msg_a_normal_restin)

)
