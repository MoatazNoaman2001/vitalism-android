package com.example.livenativerppg.models.startActivity.ui.fragments.essitionals.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.compose.material.TopAppBar
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.livenativerppg.R
import com.example.livenativerppg.commons.ErrorLoginHandler
import com.example.livenativerppg.commons.makeToast
import com.example.livenativerppg.component.base.BaseFragment
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.FragmentMedicalInfoTypeBinding
import com.example.livenativerppg.models.startActivity.profilePicSelection.ui.ProfilePicSelectFragment
import com.example.livenativerppg.models.startActivity.ui.fragments.essitionals.data.viewModel.EssentialsViewModel
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.Gender
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.MedicalSpecialist
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserType
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

private const val TAG = "MedicalInfoTypeFragment"


private const val EMAIL = "email"

@AndroidEntryPoint
class MedicalInfoTypeFragment :
    BaseFragment<FragmentMedicalInfoTypeBinding>(R.layout.fragment_medical_info_type) {
    lateinit var controller: NavController
    lateinit var userInfo: UserInfo
    lateinit var auth: FirebaseAuth
    lateinit var user: FirebaseUser
    lateinit var birthDate: Date;
    lateinit var datePicker: MaterialDatePicker<Long>
    lateinit var fb_email: String

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private val essentialVM by viewModels<EssentialsViewModel>()

    override fun onInitialized() {
        super.onInitialized()
        controller = Navigation.findNavController(requireView())
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        fb_email = if (arguments != null) {
            requireArguments().getString(EMAIL, "")
        } else
            ""

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

        val MedicalSpecialistAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_expandable_list_item_2,
            android.R.id.text1,
            MedicalSpecialist.values().map { it.name }.toList()
        )


        binding.MedicalSpecialistAutoCompleteTextView.setAdapter(MedicalSpecialistAdapter)
        binding.bloodTypeCompleteTextView.setAdapter(BloodTypeAdapter)
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
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "error in internet", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "setUpClicks: ${e.message}")
            }
        }

    }

    override fun setUpClicks() {

        binding.Next.setOnClickListener {
            val phoneNumber = binding.phoneNumberTextInputLayout.editText?.text.toString()
            val birthDay = binding.CalenderEditLayout.editText?.text.toString()
            val country = binding.CountryEditText.editableText.toString()
            val bloodType = binding.BloodTypeEditLayout.editText?.text.toString()
            val gender = binding.GenderEditLayout.editText?.text.toString()
            val weight = binding.WeightTextInputLayout.editText?.text.toString()
            val height = binding.HeightTextInputLayout.editText?.text.toString()


            val license_number = binding.licenseNumberTextInputLayout.editText?.text.toString()
            val medicine_specialty =
                binding.MedicalSpecialtyTextInputLayout.editText?.text.toString()

            if (license_number.isNullOrEmpty() || medicine_specialty.isNullOrEmpty()) {
                makeToast(requireContext(), "you should fill medical essential info")
            } else if (phoneNumber.isNullOrEmpty() || birthDay.isNullOrEmpty() || country.isNullOrEmpty() || gender.isNullOrEmpty() || weight.isNullOrEmpty() || height.isNullOrEmpty()) {
                makeToast(requireContext(), "all fields must be filled")
            } else {

                FirebaseMessaging.getInstance().token.addOnSuccessListener {
                    var email = ""
                    if (user.email != null) {
                        email = user.email!!
                    } else if (fb_email.isNotEmpty()) {
                        email = fb_email
                    } else {
                        try {
                            val u = Gson().fromJson(
                                sharedPreferences.getString(
                                    Variables.USER_INFO,
                                    ""
                                ), UserInfo::class.java
                            )
                            if (user.displayName == u.userName)
                                email = u.email
                        } catch (_: Exception) {

                        }
                    }

                    UserInfo(
                        user.displayName!!,
                        email,
                        user.uid,
                        it,
                        userType = UserType.Medical.name,
                        Disc = "",
                        BirthDay = birthDay,
                        BloodType = bloodType,
                        country = country,
                        gender = gender,
                        phoneNumber = phoneNumber,
                        weight = if (weight.isEmpty()) 0f else weight.toFloat(),
                        height = if (height.isEmpty()) 0f else height.toFloat(),
                        userName = email.substring(0, email.indexOf('@')).replace('.', ' '),
                        medicine_specialty = medicine_specialty,
                        license_number = license_number
                    )

                    sharedPreferences.edit().putString(
                        Variables.USER_INFO,
                        Gson().toJson(userInfo, UserInfo::class.java)
                    ).apply()


                    FirebaseFirestore.getInstance().collection(Variables.FireStoreUsersRoot)
                        .document(user.uid)
                        .set(userInfo).addOnSuccessListener {
                            Log.d(TAG, "setUpClicks: data added successfully")
                            try {
                                controller.navigate(
                                    R.id.action_essentialsFragment_to_profilePicSelectFragment,
                                    ProfilePicSelectFragment.getInstance(info = userInfo)
                                        .requireArguments()
                                )
                            } catch (_: Exception) {
                                binding.progressIndecator.visibility = View.INVISIBLE
                                binding.Next.isVisible = true
                            }
                        }
                }.addOnFailureListener {
                    ErrorLoginHandler(it, requireContext())
                    binding.progressIndecator.visibility = View.INVISIBLE
                    binding.Next.isVisible = true
                }

            }
        }

        binding.CalenderEditLayout.setEndIconOnClickListener {
            datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("select your birth day")
                .build()

            datePicker.show(childFragmentManager, TAG)
            datePicker.addOnPositiveButtonClickListener {
                birthDate = Date(it)
                val dateString = SimpleDateFormat("dd/MM/YYYY").format(birthDate)
                binding.CalenderEditLayout.editText?.setText(dateString)
            }
        }
    }

    companion object {
        fun getInstance(strEmail: String) = MedicalInfoTypeFragment().apply {
            arguments = Bundle().apply {
                putString(EMAIL, strEmail)
            }
        }
    }

}