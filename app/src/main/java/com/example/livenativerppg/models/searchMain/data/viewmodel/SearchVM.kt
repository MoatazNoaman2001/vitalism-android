package com.example.livenativerppg.models.searchMain.data.viewmodel

import android.os.Bundle
import androidx.lifecycle.*
import androidx.paging.cachedIn
import com.example.livenativerppg.component.db.models.ConnectRequest
import com.example.livenativerppg.models.searchMain.data.SearchRepository
import com.example.livenativerppg.models.searchMain.data.model.Listrectangle837RowModel
import com.example.livenativerppg.models.searchMain.data.model.Listrectangle837TwoRowModel
import com.example.livenativerppg.models.searchMain.data.model.SearchModel
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.lifecycle.HiltViewModel
import org.koin.core.component.KoinComponent
import java.util.concurrent.CompletableFuture
import javax.inject.Inject
import kotlin.collections.MutableList

@HiltViewModel
class SearchVM @Inject constructor(val searchRepo: SearchRepository) : ViewModel(), KoinComponent {
    val searchModel: MutableLiveData<SearchModel> = MutableLiveData(SearchModel())

    val isSearching: MutableLiveData<Boolean> = MutableLiveData()

    var navArguments: Bundle? = null

    val listrectangle837List: MutableLiveData<MutableList<Listrectangle837RowModel>> =
        MutableLiveData(mutableListOf())

    val listrectangle837TwoList: MutableLiveData<MutableList<Listrectangle837TwoRowModel>> =
        MutableLiveData(mutableListOf())

    val searchUserData = MutableLiveData("")
    val searchEmrData = MutableLiveData("")

    val users = searchUserData.switchMap { queryString ->
        searchRepo.getSearchResults(queryString).cachedIn(viewModelScope)
    }

    val emrData = searchEmrData.switchMap { queryString ->
        searchRepo.getSearchEmrResult(queryString).cachedIn(viewModelScope)
    }

    fun sendConnectRequest(
        info: UserInfo,
        connectBtn: MaterialButton,
        connectionSentProgress: CircularProgressIndicator,
    ) = CompletableFuture.supplyAsync {
        searchRepo.sendConnectRequest(info, connectBtn, connectionSentProgress)
    }


    fun CancleRequest(
        info: UserInfo,
        connectBtn: MaterialButton,
        connectionSentProgress: CircularProgressIndicator,
    ) = searchRepo.CancleConnectionRequest(info, connectBtn, connectionSentProgress);

    fun AcceptRequest(
        info: UserInfo,
        connectRequest: ConnectRequest,
        connectBtn: MaterialButton,
        connectionSentProgress: CircularProgressIndicator,
    ) = searchRepo.AcceptConnectionRequest(
        info,
        connectRequest,
        connectBtn,
        connectionSentProgress
    );

    fun RejectRequest(info: UserInfo) = searchRepo.RejectConnectionRequest(info);

    fun FollowMedical(
        info: UserInfo,
        connectBtn: MaterialButton,
        connectionSentProgress: CircularProgressIndicator,
    ) = searchRepo.FollowMedicalUesr(info, connectBtn, connectionSentProgress);

    fun UnFollowMedical(
        info: UserInfo,
        rejectedBtn:MaterialButton,
        connectBtn: MaterialButton,
        connectionSentProgress: CircularProgressIndicator,
    ) = searchRepo.UnFollowMedicalUesr(info, rejectedBtn,  connectBtn, connectionSentProgress)

    fun CancelFollowMedical(
        info: UserInfo,
        rejectedBtn:MaterialButton,
        connectBtn: MaterialButton,
        connectionSentProgress: CircularProgressIndicator,
    ) = searchRepo.CancelFollowRequest(info, rejectedBtn,  connectBtn, connectionSentProgress)

}
