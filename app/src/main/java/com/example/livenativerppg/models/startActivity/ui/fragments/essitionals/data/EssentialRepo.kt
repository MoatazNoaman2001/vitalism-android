package com.example.livenativerppg.models.startActivity.ui.fragments.essitionals.data

import com.example.livenativerppg.component.di.IpAddrRetrofit
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.GoeInfo
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.network.GeoApi
import javax.inject.Inject

class EssentialRepo @Inject constructor(@IpAddrRetrofit val geoApi: GeoApi) {
    suspend fun getGeoInfo(): GoeInfo = geoApi.getGeoInfo()
}