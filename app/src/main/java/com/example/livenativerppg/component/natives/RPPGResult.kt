package com.example.livenativerppg.component.natives

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson

data class RPPGResult(
    var time: Long,
    val mean: Double,
    val min: Double,
    val max: Double,
) :java.io.Serializable , Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble()
    )

    constructor() : this(mean = 0.0, max = 0.0, min = 0.0, time = 0L)

    override fun toString(): String {
        return Gson().toJson(this, RPPGResult::class.java)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(time)
        dest.writeDouble(mean)
        dest.writeDouble(min)
        dest.writeDouble(max)
    }

    companion object CREATOR : Parcelable.Creator<RPPGResult> {
        override fun createFromParcel(parcel: Parcel): RPPGResult {
            return RPPGResult(parcel)
        }

        override fun newArray(size: Int): Array<RPPGResult?> {
            return arrayOfNulls(size)
        }
    }

}