package com.example.livenativerppg.component.db.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.livenativerppg.component.natives.RPPGResult
import java.util.Date

enum class VitalSignType{
    HR , BP
}

@Entity("WholeVitalSigns")
data class VitalSign(
    var UID:String,
    var CaptureDate:Date,
    var recordType:String,
    var Uploaded:Boolean,
    @Embedded(prefix = "Hr_") var HeartRateRead:RPPGResult,
    @Embedded(prefix = "BP_") var BloodPressure:RPPGResult
) :java.io.Serializable {
    @PrimaryKey var Id:Int? = 0
}