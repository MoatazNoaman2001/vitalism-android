package com.example.livenativerppg.models.schedule.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.apache.commons.lang3.tuple.ImmutablePair
import java.util.Date


@Entity("medicine table")
data class Medicine(
    var name:String? = "",
    var concentration:Float? = 0.0f,
    var disc:String? = "",
    var days:ArrayList<String>,
    var hours :Int,
    var minute:Int,
    var Start:Date,
    var End:Date,
    var quantityPerDay:Int? = 1
) {
    @PrimaryKey(true) var id:Int = 0
}