package com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model

import com.google.gson.Gson

data class GoeInfo(
    val query: String,
    val status: String,
    val country: String,
    val countryCode: String,
    val region: String,
    val regionName: String,
    val city: String,
    val zip: String,
    val lat: Double,
    val lon: Double,
    val timezone: String,
    val isp: String,
    val org: String,
    val `as`:String
) {

    override fun toString(): String {
        return Gson().toJson(this , GoeInfo::class.java)
    }
}