package com.example.livenativerppg.models.frogotenPassword.ui

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.livenativerppg.R
import com.example.livenativerppg.databinding.FragmentForgotenBinding
import com.example.livenativerppg.models.frogotenPassword.data.viewModel.ForgotPasswordVM
import com.google.firebase.auth.FirebaseAuth


class ForgotenFragment : Fragment() {

    lateinit var binding:FragmentForgotenBinding
    lateinit var controller: NavController
    lateinit var auth:FirebaseAuth

    private val viewmodel:ForgotPasswordVM by viewModels<ForgotPasswordVM>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentForgotenBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller = Navigation.findNavController(requireView())
        auth  = FirebaseAuth.getInstance()

        binding.forgotPasswordVM = viewmodel

        binding.imageArrowleft.setOnClickListener {
            controller.popBackStack()
        }

        binding.btnResetPassword.setOnClickListener {
            val email = binding.etEmail.editableText.toString();
            auth.sendPasswordResetEmail(email).addOnSuccessListener {
                val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                    .setView(R.layout.recover_email_dialog_layout)
                    .setOnDismissListener {
                        controller.popBackStack()
                    }
                val dialog = builder.create()
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
            }
        }
    }

}