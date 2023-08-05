package com.example.livenativerppg.models.vitalSignSelect.data.model

import com.example.livenativerppg.R
import com.example.livenativerppg.component.di.MyApp
import kotlin.String

data class HomeModel(
  /**
   * TODO Replace with dynamic value
   */
  var txtSearchOne: String? = MyApp.getInstance().resources.getString(R.string.lbl_search)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtLanguage: String? = MyApp.getInstance().resources.getString(R.string.lbl_hi_welcome)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtMahmoudAlyosif: String? =
      MyApp.getInstance().resources.getString(R.string.msg_mahmoud_alyosif)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtLanguageOne: String? = MyApp.getInstance().resources.getString(R.string.msg_biomarkers_you)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtRespiratoryRat: String? =
      MyApp.getInstance().resources.getString(R.string.msg_respiratory_rat)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtTimeZone: String? = MyApp.getInstance().resources.getString(R.string.msg_normal_respirat)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtOxygenSaturati: String? =
      MyApp.getInstance().resources.getString(R.string.msg_oxygen_saturati)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtAnormalpulse: String? =
      MyApp.getInstance().resources.getString(R.string.msg_a_normal_pulse)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtComingSoonBlo: String? =
      MyApp.getInstance().resources.getString(R.string.msg_coming_soon_blo)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtMeasurehemoglo: String? =
      MyApp.getInstance().resources.getString(R.string.msg_measure_hemoglo)

)
