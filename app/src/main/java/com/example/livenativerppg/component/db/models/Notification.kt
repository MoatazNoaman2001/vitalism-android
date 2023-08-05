package com.example.livenativerppg.component.db.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date


@Entity("NotificationTable")
data class Notification(
    @Embedded(prefix = "Noti_") var connRequest:ConnectRequest,
    var title:String= "",
    var text:String= "",
    var sound:String= ""
) :java.io.Serializable{
    @PrimaryKey(autoGenerate = true) var id:Int =0
}