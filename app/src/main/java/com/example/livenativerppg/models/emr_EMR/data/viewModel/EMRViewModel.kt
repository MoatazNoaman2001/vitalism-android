package com.example.livenativerppg.models.emr_EMR.data.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.livenativerppg.component.di.MyBPMeasurementsRef
import com.example.livenativerppg.component.di.MyHrMeasurementsRef
import com.example.livenativerppg.models.emr_EMR.data.network.FirebaseDatabasePagingSource
import com.example.livenativerppg.models.emr_EMR.data.network.FirebaseDatabasePagingSourceBloodPressure
import com.google.firebase.database.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class EMRViewModel @Inject constructor(
    @MyHrMeasurementsRef private val queryRPPGResult: Query,
    @MyBPMeasurementsRef private val queryBPRPPGResult: Query,
) :
    ViewModel() {
    var intervalList: ArrayList<Date> = ArrayList()
    var type: String = ""

    fun HRflow() = Pager(
        PagingConfig(1)
    ) {
        FirebaseDatabasePagingSource(queryRPPGResult, intervalList)
    }.flow.cachedIn(viewModelScope).asLiveData()
    fun BPflow() = Pager(
        PagingConfig(1)
    ) {
        FirebaseDatabasePagingSourceBloodPressure(queryBPRPPGResult , intervalList)
    }.flow.cachedIn(viewModelScope).asLiveData()
}