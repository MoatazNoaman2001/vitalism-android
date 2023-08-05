package com.example.livenativerppg.models.MainChatInterface.data.model

import com.example.livenativerppg.component.db.models.VitalSignMeasurement
import java.util.Date

data class Message(
    val date:Date,
    val SenderID : String,
    val ReceiverID: String,
    var State:String? = MessageState.WAIT.name,
    val Text:String,
    var rppg:VitalSignMeasurement? = null
) {
    constructor() : this(date = Date() , SenderID = "" , ReceiverID = "" , State = "" , Text  = "", rppg = VitalSignMeasurement())
}