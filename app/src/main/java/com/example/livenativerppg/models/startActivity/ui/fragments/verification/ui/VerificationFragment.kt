package com.example.livenativerppg.models.startActivity.ui.fragments.verification.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.livenativerppg.databinding.FragmentVerificationBinding
import com.example.livenativerppg.models.mainRun.vw.MainActivity
import com.example.livenativerppg.models.startActivity.ui.fragments.verification.data.viewmodel.VerificationVM

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class VerificationFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    private val viewModel:VerificationVM by viewModels<VerificationVM>()
    lateinit var _binding: FragmentVerificationBinding
    lateinit var controller: NavController

    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentVerificationBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller = Navigation.findNavController(requireView())

//        viewModel.navArguments = requireArguments().getBundle("otp_data")
        binding.verificationVM = viewModel
        binding.btnArrowleft.setOnClickListener {
            controller.popBackStack()
        }
        binding.btnVerify.setOnClickListener {
            requireActivity().startActivity(Intent(requireActivity() , MainActivity::class.java))
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            VerificationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}