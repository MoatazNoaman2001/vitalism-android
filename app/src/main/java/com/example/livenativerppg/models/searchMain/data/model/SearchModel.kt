package com.example.livenativerppg.models.searchMain.data.model

import com.example.livenativerppg.R
import com.example.livenativerppg.component.di.MyApp
import kotlin.String

data class SearchModel(
  /**
   * TODO Replace with dynamic value
   */
  var txtSearchforVita: String? =
      MyApp.getInstance().resources.getString(R.string.msg_search_for_vita)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtCancel: String? = MyApp.getInstance().resources.getString(R.string.lbl_cancel)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtSuggested: String? = MyApp.getInstance().resources.getString(R.string.lbl_suggested)

)
