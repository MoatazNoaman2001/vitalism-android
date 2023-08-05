package com.example.livenativerppg.models.resultPage.data.viewmodel

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.livenativerppg.component.natives.BPRPPGResult
import com.example.livenativerppg.component.natives.RPPGResult
import com.example.livenativerppg.models.MainChatInterface.data.MainChatRepoImpl
import com.example.livenativerppg.models.resultPage.data.ResultRPPGRepository
import com.example.livenativerppg.models.resultPage.data.model.ResultModel
import com.example.livenativerppg.models.resultPage.data.model.ResultRowModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlin.collections.MutableList
import org.koin.core.component.KoinComponent
import javax.inject.Inject


@HiltViewModel
class ResultVM @Inject constructor(val resultRPPGRepository: ResultRPPGRepository , val mainChatRepoImpl: MainChatRepoImpl) : ViewModel(), KoinComponent {
  val resultModel: MutableLiveData<ResultModel> = MutableLiveData(ResultModel())

  var navArguments: Bundle? = null

  val resultList: MutableLiveData<MutableList<ResultRowModel>> = MutableLiveData(mutableListOf())

  fun sendMeasureNotificationHr(rppgResult: RPPGResult, type: String) = resultRPPGRepository.sendMeasureNotificationHr(rppgResult, type , mainChatRepoImpl)
  fun sendMeasureNotificationBP(bprppgResult: BPRPPGResult, type: String) = resultRPPGRepository.sendMeasureNotificationBP(bprppgResult, type , mainChatRepoImpl)
}
