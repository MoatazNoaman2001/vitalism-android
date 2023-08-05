package com.example.livenativerppg.models.startActivity.ui.fragments.essitionals.ui

import android.content.SharedPreferences
import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.livenativerppg.R
import com.example.livenativerppg.commons.birthDayDateFromate
import com.example.livenativerppg.commons.makeToast
import com.example.livenativerppg.component.base.BaseFragment
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.FragmentEssentialsBinding
import com.example.livenativerppg.models.startActivity.profilePicSelection.ui.ProfilePicSelectFragment
import com.example.livenativerppg.models.startActivity.ui.fragments.essitionals.data.viewModel.EssentialsViewModel
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.Gender
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserType
import com.facebook.AccessToken
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import javax.inject.Inject


private const val TAG = "EssentialsFragment"


private const val EMAIL = "email"
@AndroidEntryPoint
class EssentialsFragment : BaseFragment<FragmentEssentialsBinding>(R.layout.fragment_essentials) {
    lateinit var controller: NavController
    lateinit var auth: FirebaseAuth
    lateinit var user: FirebaseUser
    lateinit var userInfo: UserInfo
    lateinit var datePicker: MaterialDatePicker<Long>
    lateinit var birthDate: Date;
    lateinit var fb_email: String
    private val essentialVM by viewModels<EssentialsViewModel>()

    lateinit var executor: Executor

    @Inject
    lateinit var userDoc: CollectionReference
    @Inject
    lateinit var prfileShared: SharedPreferences

    override fun onInitialized() {
        super.onInitialized()
        controller = Navigation.findNavController(requireView())
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        fb_email = if (arguments!= null){
            requireArguments().getString(EMAIL , "")
        }else{
            ""
        }

        Log.d(TAG, "onInitialized: userDoc: $userDoc")

        val DiagnoseAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_expandable_list_item_2,
            android.R.id.text1,
            arrayOf(
                "High blood pressure", "Diabetes",
                "Asthma", "Arthritis",
                "Arrhythmias", "High cholesterol",
                "Depression",
                "Anxiety",
                "Migraines",
                "Allergies", "Irritable bowel syndrome (IBS)", "Other"
            )
        )

        val BloodTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_expandable_list_item_2,
            android.R.id.text1,
            arrayOf(
                "A", "A+", "B-", "B+", "AB+", "AB-", "O+", "O-"
            )
        )

        binding.bloodTypeCompleteTextView.setAdapter(BloodTypeAdapter)
        binding.DiagnosesCompleteTextView.setAdapter(DiagnoseAdapter)
        binding.GenderCompleteTextView.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_expandable_list_item_2,
                android.R.id.text1,
                Gender.values().map { it.name }.toList()
            )
        )
        CoroutineScope(lifecycleScope.coroutineContext).launch {
            try {
                val geoInfo = essentialVM.geoInfo()
                Log.d(TAG, "onViewCreated: $geoInfo")
                binding.CountryEditText.setText("${geoInfo.country}-${geoInfo.city}-${geoInfo.regionName}   ${geoInfo.lat},${geoInfo.lon}")
                binding.CountryEditText.isEnabled = false
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "error in internet", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "setUpClicks: ${e.message}")
            }
        }
    }

    override fun setUpClicks() {
        binding.toolbar.setNavigationOnClickListener {
            controller.popBackStack()
        }

        binding.CalenderEditLayout.setEndIconOnClickListener {
            datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("select your birth day")
                .build()

            datePicker.show(childFragmentManager, TAG)
            datePicker.addOnPositiveButtonClickListener {
                birthDate = Date(it)
                val dateString = birthDayDateFromate.format(birthDate)
                binding.CalenderEditLayout.editText?.setText(dateString)
            }
        }


        binding.Next.setOnClickListener {
            val phoneNumber = binding.phoneNumberTextInputLayout.editText?.text.toString()
            val birthDay = binding.CalenderEditLayout.editText?.text.toString()
            val country = binding.CountryEditText.editableText.toString()
            val bloodType = binding.BloodTypeEditLayout.editText?.text.toString()
            val diagnoses = binding.DiagnosesEditLayout.editText?.text.toString()
            val gender = binding.GenderEditLayout.editText?.text.toString()
            val weight = binding.WeightTextInputLayout.editText?.text.toString()
            val height = binding.HeightTextInputLayout.editText?.text.toString()


            if (phoneNumber.isNullOrEmpty() || birthDay.isNullOrEmpty() || country.isNullOrEmpty()
                || bloodType.isNullOrEmpty() || diagnoses.isNullOrEmpty() || gender.isNullOrEmpty()
                || weight.isNullOrEmpty() || height.isNullOrEmpty()
            ) {
                makeToast(requireContext() , "should fill all fields")
            } else {
                binding.progressIndecator.visibility = VISIBLE
                binding.Next.isVisible = false
                FirebaseMessaging.getInstance().token.addOnSuccessListener {
                    var email = ""
                    if (user.email != null){
                        email = user.email!!
                    }else if (fb_email.isNotEmpty()){
                        email = fb_email
                    }else if(AccessToken.getCurrentAccessToken() != null) {

                    }else{
                        val u = Gson().fromJson(prfileShared.getString(Variables.USER_INFO, "") , UserInfo::class.java)
                        if (user.displayName == u.userName)
                            email = u.email
                    }

                    userInfo = UserInfo(
                        user.displayName!!,
                        email,
                        user.uid,
                        it,
                        userType = UserType.Patient.name,
                        Disc = "",
                        BirthDay = birthDay,
                        BloodType = bloodType,
                        Diagnoses = diagnoses,
                        country = country,
                        gender = gender,
                        phoneNumber = phoneNumber,
                        weight = if (weight.isEmpty()) 0f else weight.toFloat(),
                        height = if (height.isEmpty()) 0f else height.toFloat(),
                        userName = email.substring(0, email.indexOf('@')).replace('.' , ' ')
                    )

                    prfileShared.edit().putString(Variables.USER_INFO , Gson().toJson(userInfo , UserInfo::class.java)).apply()


                    FirebaseFirestore.getInstance().collection(Variables.FireStoreUsersRoot)
                        .document(user.uid)
                        .set(userInfo).addOnSuccessListener {
                            Log.d(TAG, "setUpClicks: data added successfully")
                            try {
                                controller.navigate(R.id.action_essentialsFragment_to_profilePicSelectFragment, ProfilePicSelectFragment.getInstance(info = userInfo).requireArguments())
                            } catch (_: Exception) {
                                binding.progressIndecator.visibility = INVISIBLE
                                binding.Next.isVisible = true
                            }
                        }
                }.addOnFailureListener {
                    Toast.makeText(requireContext() , "bad internet connection" , Toast.LENGTH_SHORT).show()
                    binding.progressIndecator.visibility = INVISIBLE
                    binding.Next.isVisible = true
                }
            }
        }
    }

    companion object {
        fun getInstance(strEmail: String) = EssentialsFragment().apply {
            arguments = Bundle().apply {
                putString(EMAIL, strEmail)
            }
        }
    }

}