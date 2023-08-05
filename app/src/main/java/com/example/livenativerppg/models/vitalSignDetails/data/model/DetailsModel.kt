package com.example.livenativerppg.models.vitalSignDetails.data.model

import android.graphics.drawable.Drawable
import com.example.livenativerppg.R
import com.example.livenativerppg.component.di.MyApp
import kotlin.String

data class DetailsModel(
    /**
     * TODO Replace with dynamic value
     */
    var txtDetails: String? = MyApp.getInstance().resources.getString(R.string.lbl_details),
    /**
     * TODO Replace with dynamic value
     */
    var txtHeartRate: String? = MyApp.getInstance().resources.getString(R.string.lbl_heart_rate),
    /**
     * TODO Replace with dynamic value
     */
    var txtDescription: String? = MyApp.getInstance().resources.getString(R.string.msg_heart_rate_als),

    val imageUri :Drawable = MyApp.getInstance().resources.getDrawable(R.drawable.img_rectangle818)

    )
