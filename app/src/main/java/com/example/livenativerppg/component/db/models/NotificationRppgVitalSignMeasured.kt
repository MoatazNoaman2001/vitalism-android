package com.example.livenativerppg.component.db.models

import com.example.livenativerppg.component.natives.RPPGResult

data class NotificationRppgVitalSignMeasured(
    val registration_ids:Array<String>,
    val notification: HashMap<String, String>,
    val data: VitalSignMeasurement
) {
    constructor():this(registration_ids= emptyArray<String>() , notification= HashMap(), data=VitalSignMeasurement())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NotificationRppgVitalSignMeasured) return false

        if (!registration_ids.contentEquals(other.registration_ids)) return false
        if (notification != other.notification) return false
        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        var result = registration_ids.contentHashCode()
        result = 31 * result + notification.hashCode()
        result = 31 * result + data.hashCode()
        return result
    }
}