package com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.network

import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.GoeInfo
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Query

interface GeoApi {

    companion object{
        val baseUrl = "http://ip-api.com"
    }

    @GET("json")
    suspend fun getGeoInfo(): GoeInfo
}