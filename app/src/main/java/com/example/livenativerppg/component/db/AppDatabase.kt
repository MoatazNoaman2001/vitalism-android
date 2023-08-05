package com.example.livenativerppg.component.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.livenativerppg.component.db.converters.DateConverter
import com.example.livenativerppg.component.db.converters.StringListConverter
import com.example.livenativerppg.component.db.dao.MeasuredVitalSignDao
import com.example.livenativerppg.component.db.dao.MedicineScheduledDao
import com.example.livenativerppg.component.db.dao.NotificationDao
import com.example.livenativerppg.component.db.models.Notification
import com.example.livenativerppg.component.db.models.VitalSign
import com.example.livenativerppg.models.schedule.data.model.Medicine

@Database(entities = [Medicine::class , VitalSign::class , Notification::class] , version = 1 , exportSchema = false)
@TypeConverters(value = [DateConverter::class, StringListConverter::class])
abstract class AppDatabase : RoomDatabase() {
    abstract fun getMedicineDao() : MedicineScheduledDao
    abstract fun getMeasuredVitalSign() : MeasuredVitalSignDao
    abstract fun getNotificationDao() : NotificationDao
}