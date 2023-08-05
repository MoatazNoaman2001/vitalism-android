package com.example.livenativerppg.component.db.models

import java.util.Date

enum class PPGType{HR, RF, O2 , BP, HR_RF}
enum class State{normal, upnormal}
data class PPGResult(
    val hr:Int? = 0,
    val RF:Int? = 0,
    val Sp:Int? = 0,
    val Dp:Int? = 0,
    val o2:Int? = 0,
    val hrTorr:Float? = 0f,

    val MeasureDate:Long,
    val type:Int,
    val state:Int
) {

    constructor():this(hr=0, RF=0, Sp=0, Dp=0, o2=0, hrTorr=0f, MeasureDate=0, type=0, state=0)
}