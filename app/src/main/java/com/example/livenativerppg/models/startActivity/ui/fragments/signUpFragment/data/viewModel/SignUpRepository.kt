package com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.viewModel

import com.example.livenativerppg.component.di.IpAddrRetrofit
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.GoeInfo
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.network.GeoApi
import dagger.Provides
import javax.inject.Inject


class SignUpRepository @Inject constructor(@IpAddrRetrofit val geoApi: GeoApi) {
    suspend fun getGeoInfo(): GoeInfo = geoApi.getGeoInfo()
}