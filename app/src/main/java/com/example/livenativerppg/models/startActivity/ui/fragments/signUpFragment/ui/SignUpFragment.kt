package com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.ui

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.livenativerppg.R
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.viewModel.SignUpVM
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@AndroidEntryPoint
class SignUpFragment : Fragment() {
    private val TAG = "SignUpFragment"

    private var param1: String? = null
    private var param2: String? = null

    private val viewModel: SignUpVM by viewModels()
    lateinit var controller: NavController
    lateinit var auth: FirebaseAuth

    private val EmailPattern = Pattern.compile(
        "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"
    )
    private val PasswordPattern = Pattern.compile(
        "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!%*#?&]{8,}\$"
    )

    lateinit var datePicker: MaterialDatePicker<Long>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    lateinit var fragmentSignUpBinding: com.example.livenativerppg.databinding.FragmentSignUpBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        fragmentSignUpBinding =
            com.example.livenativerppg.databinding.FragmentSignUpBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        return fragmentSignUpBinding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentSignUpBinding.signUpVM = viewModel

        controller = Navigation.findNavController(requireView())

        fragmentSignUpBinding.toolbar.setNavigationOnClickListener {
            controller.popBackStack()
        }

        fragmentSignUpBinding.TextInputEmail.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.toString().contains("@"))
                    fragmentSignUpBinding.NameEditText.setText(s.toString().replace('.' ,' '))
                else{
                    fragmentSignUpBinding.NameEditText.setText(s.toString().substringBefore("@").replace('.' , ' '))
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        fragmentSignUpBinding.ConfirmPasswordEditLayout.editText?.addTextChangedListener(passAndConfirmPassTextWathcer())
        fragmentSignUpBinding.passwordEditLayout.editText?.addTextChangedListener(passAndConfirmPassTextWathcer())
        fragmentSignUpBinding.btnSignUp.setOnClickListener {
            val email = fragmentSignUpBinding.TextInputEmail.editText?.text?.toString()!!
            val password = fragmentSignUpBinding.passwordEditLayout.editText?.text?.toString()!!
            val confirmPassword = fragmentSignUpBinding.ConfirmPasswordEditLayout.editText?.text?.toString()!!
            val Name = fragmentSignUpBinding.NameEditText.text.toString()
//

            if (email.isNullOrEmpty() or password.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "make sure you fill all fields", Toast.LENGTH_SHORT).show()
            } else if (confirmPassword != password) {
                fragmentSignUpBinding.ConfirmPasswordEditLayout.error = "confirm password and password does not match"
                fragmentSignUpBinding.ConfirmPasswordEditLayout.isErrorEnabled = true
            }else if (!EmailPattern.matcher(email).matches()) {
                Toast.makeText(requireContext(), "invalid email", Toast.LENGTH_SHORT)
                    .show()
            } else if (!PasswordPattern.matcher(password).matches()) {
                Toast.makeText(requireContext(), "invalid password", Toast.LENGTH_SHORT)
                    .show()
            } else {
                fragmentSignUpBinding.signingIndecator.isVisible = true
                fragmentSignUpBinding.btnSignUp.isClickable = false
                auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
                    auth.currentUser!!.sendEmailVerification().addOnSuccessListener {
                        val request = userProfileChangeRequest {
                            this.displayName = Name
                        }
                        val user = FirebaseAuth.getInstance().currentUser
                        user?.updateProfile(request)!!
                            .addOnSuccessListener {
                                val builder: AlertDialog.Builder =
                                    AlertDialog.Builder(requireContext())
                                        .setView(R.layout.verify_email_dialog_layout)
                                        .setOnDismissListener {
                                            controller.popBackStack()
                                        }
                                val dialog = builder.create()
                                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                dialog.show()
                                auth.currentUser!!.sendEmailVerification().addOnSuccessListener {
                                    Toast.makeText(
                                        requireContext(),
                                        "verified sent to mail box",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            }

                    }
                }.addOnFailureListener {
                    Log.d(TAG, "onViewCreated: " + it.message)
                }
            }
        }

    }

    inner class passAndConfirmPassTextWathcer : TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (fragmentSignUpBinding.ConfirmPasswordEditLayout.isErrorEnabled) {
                fragmentSignUpBinding.ConfirmPasswordEditLayout.error = ""
                fragmentSignUpBinding.ConfirmPasswordEditLayout.isErrorEnabled = false
            }
        }

        override fun afterTextChanged(s: Editable?) {}

    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignUpFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}