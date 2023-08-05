package com.example.livenativerppg.models.resultPage.data.model

import com.example.livenativerppg.R
import com.example.livenativerppg.component.di.MyApp
import kotlin.String

data class ResultRowModel(
  /**
   * TODO Replace with dynamic value
   */
  var txtOne: String? = MyApp.getInstance().resources.getString(R.string.lbl_1)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtUsername: String? = MyApp.getInstance().resources.getString(R.string.lbl_username)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtMahmoudalyosifyOne: String? =
      MyApp.getInstance().resources.getString(R.string.lbl_mahmoudalyosify)

)
