package com.example.livenativerppg.commons

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat


val messageDateFormat = SimpleDateFormat("dd-MM-yyyy  hh:mm:ss a")

val onlyDayName = SimpleDateFormat("EEE")
val calenderDateFormat = SimpleDateFormat("dd")
val birthDayDateFromate = SimpleDateFormat("dd/MM/YYYY")
val rppgDateFormat = SimpleDateFormat("dd-MM-yyyy EEE")
val rppgTimeDateFormat = SimpleDateFormat("hh:mm")

val ppgDateFormat = SimpleDateFormat("yyyy-MM-dd EEE")
val ppgTimeDateFormat = SimpleDateFormat("hh:mm a")

fun makeToast(context: Context , msg:String){
    Toast.makeText(context , msg , Toast.LENGTH_SHORT).show()
}