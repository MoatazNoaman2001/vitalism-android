package com.example.livenativerppg.models.startActivity.ui.fragments.UserTypeSelector.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.livenativerppg.R
import com.example.livenativerppg.component.base.BaseFragment
import com.example.livenativerppg.databinding.FragmentUserTypeSelectionBinding
import com.example.livenativerppg.models.startActivity.ui.fragments.essitionals.ui.EssentialsFragment
import com.example.livenativerppg.models.startActivity.ui.fragments.essitionals.ui.MedicalInfoTypeFragment


private const val EMAIL = "email"

class UserTypeSelectionFragment :
    BaseFragment<FragmentUserTypeSelectionBinding>(R.layout.fragment_user_type_selection) {
    lateinit var controller: NavController
    lateinit var str_email: String

    override fun onInitialized() {
        super.onInitialized()
        controller = Navigation.findNavController(requireView())
        str_email = if (arguments != null)
            requireArguments().getString(EMAIL, "")
        else
            ""
    }

    override fun setUpClicks() {
        binding.PatientLayout.setOnClickListener {
            controller.navigate(
                R.id.action_userTypeSelectionFragment_to_essentialsFragment,
                EssentialsFragment.getInstance(str_email).requireArguments()
            )
        }
        binding.medicalLayout.setOnClickListener {
            controller.navigate(
                R.id.action_userTypeSelectionFragment_to_medicalInfoTypeFragment,
                MedicalInfoTypeFragment.getInstance(str_email).requireArguments()
            )
        }

    }

    companion object {
        fun getInstance(strEmail: String) = UserTypeSelectionFragment().apply {
            arguments = Bundle().apply {
                putString(EMAIL, strEmail)
            }
        }
    }

}