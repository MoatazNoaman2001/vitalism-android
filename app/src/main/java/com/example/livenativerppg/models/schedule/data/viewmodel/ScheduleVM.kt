package com.example.livenativerppg.models.schedule.data.viewmodel

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.livenativerppg.component.db.AppDatabase
import com.example.livenativerppg.models.schedule.data.model.Medicine
import com.example.livenativerppg.models.schedule.data.model.ScheduleModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import java.util.*
import javax.inject.Inject

private const val TAG = "ScheduleVM"

@HiltViewModel
class ScheduleVM @Inject constructor(val appDatabase: AppDatabase) : ViewModel(), KoinComponent {
    val scheduleModel: MutableLiveData<ScheduleModel> = MutableLiveData(ScheduleModel())

    var navArguments: Bundle? = null


    fun insertMedicine(
        medicine: Medicine,
        controller: NavController,
    ) {

        CoroutineScope(Dispatchers.IO).launch {
            appDatabase.getMedicineDao().InsertMedicine(medicine)
            Log.d(TAG, "insertMedicine: added successfully")
            MainScope().launch {
                controller.popBackStack()
            }
        }
    }

    val medicines = appDatabase.getMedicineDao().allMedicine;


}
