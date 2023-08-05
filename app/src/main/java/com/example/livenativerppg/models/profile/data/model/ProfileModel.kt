package com.example.livenativerppg.models.profile.data.model


import com.example.livenativerppg.R
import com.example.livenativerppg.component.di.MyApp
import kotlin.String

data class ProfileModel(
  /**
   * TODO Replace with dynamic value
   */
  var txtProfile: String? = MyApp.getInstance().resources.getString(R.string.lbl_profile)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtMahmoudSayedY: String? =
      MyApp.getInstance().resources.getString(R.string.msg_mahmoud_sayed_y)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtMahmoudAlyosifyOne: String? =
      MyApp.getInstance().resources.getString(R.string.lbl_mahmoudalyosify2)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtLastHeartrate: String? =
      MyApp.getInstance().resources.getString(R.string.lbl_last_heart_rate)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtEightySeven: String? = MyApp.getInstance().resources.getString(R.string.lbl_87)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtLastRespirator: String? =
      MyApp.getInstance().resources.getString(R.string.msg_last_respirator2)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtTwenty: String? = MyApp.getInstance().resources.getString(R.string.lbl_20)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtHeartratevari: String? =
      MyApp.getInstance().resources.getString(R.string.msg_heart_rate_vari3)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtSixty: String? = MyApp.getInstance().resources.getString(R.string.lbl_60)
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
  var txtNinetySeven: String? = MyApp.getInstance().resources.getString(R.string.lbl_97)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtPRQ: String? = MyApp.getInstance().resources.getString(R.string.lbl_prq)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtNone: String? = MyApp.getInstance().resources.getString(R.string.lbl_none)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtBloodPressure: String? =
      MyApp.getInstance().resources.getString(R.string.lbl_blood_pressure)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtNoneOne: String? = MyApp.getInstance().resources.getString(R.string.lbl_none)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtPeopleyoufoll: String? =
      MyApp.getInstance().resources.getString(R.string.msg_people_you_foll)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtPreviousmeasur: String? =
      MyApp.getInstance().resources.getString(R.string.msg_previous_measur)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtSettingsOne: String? = MyApp.getInstance().resources.getString(R.string.lbl_settings)
  ,
  /**
   * TODO Replace with dynamic value
   */
  var txtVersionofVita: String? =
      MyApp.getInstance().resources.getString(R.string.msg_version_of_vita)

)
