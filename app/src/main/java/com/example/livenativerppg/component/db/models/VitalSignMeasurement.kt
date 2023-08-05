package com.example.livenativerppg.component.db.models

import com.example.livenativerppg.component.natives.BPRPPGResult
import com.example.livenativerppg.component.natives.RPPGResult
import java.util.Date


data class VitalSignMeasurement(
    val rppgHr:RPPGResult?,
    val rppgBP:BPRPPGResult?,
    val type : String,
    var Date : Date?
) {
    constructor():this(rppgHr=RPPGResult(), type="", Date=Date() , rppgBP = BPRPPGResult())
}