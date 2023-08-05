package com.example.livenativerppg.component.natives

import android.os.Parcel
import android.os.Parcelable

data class SignalPoint(
    val time: Long,
    val point1: Int,
    val point2: Int,
) : java.io.Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readInt(),
        parcel.readInt()
    )
    override fun describeContents(): Int {
        return 0;
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(time)
        dest.writeInt(point1)
        dest.writeInt(point2)
    }

    constructor() : this(time = 0L, point1 = 0, point2 = 0)

    companion object CREATOR : Parcelable.Creator<SignalPoint> {
        override fun createFromParcel(parcel: Parcel): SignalPoint {
            return SignalPoint(parcel)
        }

        override fun newArray(size: Int): Array<SignalPoint?> {
            return arrayOfNulls(size)
        }
    }


}