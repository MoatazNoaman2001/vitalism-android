package com.example.livenativerppg.component.db.converters

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

@ProvidedTypeConverter
class StringListConverter {

    @TypeConverter
    fun fromString(value: String?): ArrayList<String?>? {
        val listType: Type? = object : TypeToken<ArrayList<String?>?>() {}.getType()
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<String?>?): String? {
        val gson: Gson? = Gson()
        val json: String? = gson?.toJson(list)
        return json
    }
}