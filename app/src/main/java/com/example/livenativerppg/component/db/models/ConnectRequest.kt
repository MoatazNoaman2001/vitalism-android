package com.example.livenativerppg.component.db.models

import com.google.gson.Gson
import java.util.Date


enum class ConnectionType{FOLLOW , CONNECT , FRIEND}
data class ConnectRequest(
    var SenderId:String,
    var ReceiverId:String,
    var RequestDate:Date,
    var ConnectType:String? = ConnectionType.CONNECT.name,
    var Readed:Boolean? = false,
    var Accpeted:Boolean? = false
) :java.io.Serializable {

    constructor():this(SenderId="" , ReceiverId="", RequestDate = Date(), ConnectType = "", Readed = false, Accpeted = false)
    override fun toString(): String {
        return Gson().toJson(this, ConnectRequest::class.java)
    }
}