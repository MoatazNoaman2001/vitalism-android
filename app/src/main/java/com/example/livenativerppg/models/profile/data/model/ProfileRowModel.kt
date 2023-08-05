package com.example.livenativerppg.models.profile.data.model

import com.example.livenativerppg.R
import com.example.livenativerppg.component.di.MyApp
import kotlin.String

data class ProfileRowModel(
  /**
   * TODO Replace with dynamic value
   */
  var txtElectronicMedi: String? =
      MyApp.getInstance().resources.getString(R.string.msg_electronic_medi)

)
