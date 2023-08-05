package com.example.livenativerppg.component.db.models

import androidx.room.Entity


@Entity(tableName = "VS category")
data class VitalSignsCategory(
    val id:String,
    val name:String,
    val brief:String,
    val description:String,
    val preferred:Boolean,
) {
}