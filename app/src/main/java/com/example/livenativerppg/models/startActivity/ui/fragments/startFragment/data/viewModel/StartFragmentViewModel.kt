package com.example.livenativerppg.models.startActivity.ui.fragments.startFragment.data.viewModel

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.livenativerppg.models.startActivity.ui.fragments.startFragment.data.model.StartFragmentModel
import org.koin.core.component.KoinComponent

class StartFragmentViewModel : ViewModel() , KoinComponent{
    public val StartFragmentAppModel : MutableLiveData<StartFragmentModel> = MutableLiveData(
        StartFragmentModel())

    var Bundle:Bundle? = null
}