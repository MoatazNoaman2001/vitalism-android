package com.example.livenativerppg.models.emr_EMR.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.livenativerppg.R
import com.example.livenativerppg.component.base.BaseFragment
import com.example.livenativerppg.databinding.FragmentChooseVitalSignToShowBinding
import com.google.android.material.appbar.MaterialToolbar


class ChooseVitalSignToShowFragment : BaseFragment<FragmentChooseVitalSignToShowBinding>(R.layout.fragment_choose_vital_sign_to_show) {
    override fun onInitialized() {
        super.onInitialized()
        binding.vitalSignList.adapter = ArrayAdapter<String>(requireContext() , android.R.layout.simple_list_item_1 , android.R.id.text1, arrayOf("Heart Rate" , "Blood Pressure"))
        requireActivity().findViewById<MaterialToolbar>(R.id.toolbar).apply {
            subtitle = ""
        }
    }

    override fun setUpClicks() {
        binding.vitalSignList.setOnItemClickListener { parent, view, position, id ->
            val adapter = (requireActivity().findViewById<ViewPager2>(R.id.viewPager).adapter as EMRViewPagerAdapter)

            if(position == 0){
                if (adapter.isContain(EMRFirstScreenFragment.getInstance("hr"))) {
                    adapter.removeFragment(EMRFirstScreenFragment.getInstance("hr"))
                }
                adapter.addFragment(EMRFirstScreenFragment.getInstance("hr"))
            }else{
                adapter.addFragment(EMRFirstScreenFragment.getInstance("bp"))
            }

        }
    }

}