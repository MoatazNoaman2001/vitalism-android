package com.example.livenativerppg.models.vitalSignSelect.data.viewmodel

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.livenativerppg.component.db.AppDatabase
import com.example.livenativerppg.models.vitalSignSelect.data.model.HomeModel
import com.example.livenativerppg.models.vitalSignSelect.data.model.HomeRepo
import com.example.livenativerppg.models.vitalSignSelect.data.model.ListfavoriteFourRowModel
import com.example.livenativerppg.models.vitalSignSelect.data.model.ListfavoriteRowModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlin.collections.MutableList
import org.koin.core.component.KoinComponent
import javax.inject.Inject

@HiltViewModel
class HomeVM @Inject constructor(val homeRepo: HomeRepo): ViewModel(), KoinComponent {
  val homeModel: MutableLiveData<HomeModel> = MutableLiveData(HomeModel())

  var navArguments: Bundle? = null

  val notifications = homeRepo.notifications.asLiveData()

  val listfavoriteList: MutableLiveData<MutableList<ListfavoriteRowModel>> =
      MutableLiveData(mutableListOf())

  val listfavoriteFourList: MutableLiveData<MutableList<ListfavoriteFourRowModel>> =
      MutableLiveData(mutableListOf())
}
