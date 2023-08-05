package com.example.livenativerppg.component.natives

import android.os.Parcel
import android.os.Parcelable

class BPRPPGResult(
    val sp:Int,
    val dp:Int,

    val date:Long,
) : java.io.Serializable , Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readLong()
    ) {
    }

    constructor() : this(sp = 0 , dp = 0 , date = 0L)

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(sp)
        dest.writeInt(dp)
        dest.writeLong(date)
    }

    companion object CREATOR : Parcelable.Creator<BPRPPGResult> {
        override fun createFromParcel(parcel: Parcel): BPRPPGResult {
            return BPRPPGResult(parcel)
        }

        override fun newArray(size: Int): Array<BPRPPGResult?> {
            return arrayOfNulls(size)
        }
    }
}