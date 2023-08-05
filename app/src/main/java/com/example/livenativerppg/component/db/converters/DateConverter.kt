package com.example.livenativerppg.component.db.converters

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import java.util.Date


@ProvidedTypeConverter
class DateConverter {

    @TypeConverter
    public fun DateToLong(date:Date) :Long{
        return date.time
    }

    @TypeConverter
    public fun LongToDate(date:Long) :Date{
        return Date(date)
    }
}