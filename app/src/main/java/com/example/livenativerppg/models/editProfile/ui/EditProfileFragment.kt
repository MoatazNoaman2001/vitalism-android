package com.example.livenativerppg.models.editProfile.ui

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.livenativerppg.R
import com.example.livenativerppg.component.base.BaseFragment
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.FragmentEditProfileBinding
import com.example.livenativerppg.models.editProfile.data.viewModel.EditProfileVM
import com.example.livenativerppg.models.startActivity.profilePicSelection.ui.ImagePicSelectManger
import com.example.livenativerppg.models.startActivity.ui.StartActivity
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.BloodTypes
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.Gender
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserType
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.*
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


private const val TAG = "EditProfileFragment"

@AndroidEntryPoint
class EditProfileFragment :
    BaseFragment<FragmentEditProfileBinding>(R.layout.fragment_edit_profile) {
    lateinit var controller: NavController
    lateinit var user: FirebaseUser
    lateinit var datePacker: MaterialDatePicker<Long>

    private val viewModel: EditProfileVM by viewModels<EditProfileVM>()

    @Inject
    lateinit var infoTask: CollectionReference

    @Inject
    lateinit var profileShared: SharedPreferences

    @Inject
    lateinit var imageLoader: RequestManager

    lateinit var mainInfo: UserInfo


    lateinit var auth: FirebaseAuth
    lateinit var client: GoogleSignInClient
    lateinit var gso: GoogleSignInOptions
    lateinit var picker: ActivityResultLauncher<PickVisualMediaRequest>
    private var imageUri: String? = null
    lateinit var uploader: ImagePicSelectManger

    override fun onInitialized() {
        super.onInitialized()
        controller = Navigation.findNavController(requireView())
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        uploader = ImagePicSelectManger()
        datePacker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("update you birth day")
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .build()
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        client = GoogleSignIn.getClient(requireActivity(), gso)

        if(user.email.isNullOrEmpty()){
            binding.resetPassword.isVisible = false
            binding.changeEmailBtn.isVisible = false
        }
        infoTask.document(user.uid).addSnapshotListener { value, error ->
            if (error != null && error.message?.isEmpty()!!) {
                Log.d(TAG, "onInitialized: ${error.message}")
                return@addSnapshotListener
            }
            try {
                if (value != null)
                    if (value.exists()) {
                        mainInfo = value.toObject(UserInfo::class.java)!!
                        profileShared.edit().putString(
                            Variables.USER_INFO,
                            Gson().toJson(mainInfo, UserInfo::class.java)
                        ).apply()
                        if (mainInfo.name != user.displayName) {
                            val request = userProfileChangeRequest {
                                displayName = mainInfo.name
                            }
                            auth.currentUser!!.updateProfile(request)
                        }

                        Log.d(TAG, "onInitialized: user info: $mainInfo")
                        profileShared.edit().putString("name", mainInfo.name).apply()
                        profileShared.edit().putString("email", mainInfo.email).apply()
                        profileShared.edit().putString("bloodType", mainInfo.BloodType).apply()
                        profileShared.edit().putString("Diagnose", mainInfo.Diagnoses).apply()
                        profileShared.edit().putString("country", mainInfo.country).apply()
                        profileShared.edit().putString("gender", mainInfo.gender).apply()
                        profileShared.edit().putString("birthDay", mainInfo.BirthDay).apply()
                        profileShared.edit().putString("phoneNumber", mainInfo.phoneNumber)
                            .apply()

                        binding.NameTextInputLayout.editText?.setText(
                            mainInfo.name
                        )
                        binding.EmailTextInputLayout.editText?.setText(
                            mainInfo.email
                        )
                        binding.BloodTypeTextInputLayout.editText?.setText(
                            mainInfo.BloodType
                        )
                        binding.DiagnosesTextInputLayout.editText?.setText(
                            mainInfo.Diagnoses
                        )
                        binding.CountryTextInputLayout3.editText?.setText(
                            mainInfo.country
                        )
                        binding.GenderTextInputLayout3.editText?.setText(
                            mainInfo.gender
                        )
                        binding.birthDayTextInputLayout.editText?.setText(
                            mainInfo.BirthDay
                        )
                        binding.phoneNumberTextInputLayout.editText?.setText(
                            mainInfo.phoneNumber
                        )
                    } else {

                    }
            } catch (e: Exception) {
                Log.d(TAG, "onInitialized: ${e.message}")
            }
        }
        binding.NameTextInputLayout.editText?.setText(profileShared.getString("name", ""))
        binding.EmailTextInputLayout.editText?.setText(profileShared.getString("email", ""))
        binding.BloodTypeTextInputLayout.editText?.setText(
            profileShared.getString(
                "blood Type",
                ""
            )
        )

        try {
            val userInfo = Gson().fromJson(
                profileShared.getString(Variables.USER_INFO, ""),
                UserInfo::class.java
            )
            if (userInfo != null) {
                binding.DiagnosesTextInputLayout.isVisible =
                    userInfo.userType == UserType.Patient.name
            }
        } catch (e: Exception) {

        }
        binding.DiagnosesTextInputLayout.editText?.setText(profileShared.getString("Diagnose", ""))
        binding.CountryTextInputLayout3.editText?.setText(profileShared.getString("country", ""))
        binding.GenderTextInputLayout3.editText?.setText(profileShared.getString("gender", ""))

        imageLoader.asBitmap().load(user.photoUrl).circleCrop().into(binding.ProfImage)

        val gender_adapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1,
            android.R.id.text1,
            Gender.values().map { it.name })
        val bloodType = ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1,
            android.R.id.text1,
            BloodTypes.values().map { it.name })
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
        binding.genderAutoCompleteTextView.setAdapter(gender_adapter)
        binding.bloodTypeCompleteTextView.setAdapter(bloodType)
        binding.diaganosesAutoComplete.setAdapter(DiagnoseAdapter)

    }

    override fun addObservers() {
        super.addObservers()
        picker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            if (it != null) {
                Log.d(TAG, "onViewCreated: $it")
                imageUri = it.toString()
                Glide.with(requireContext())
                    .asBitmap()
                    .load(it)
                    .apply(RequestOptions().override(800, 1200))
                    .addListener(object : RequestListener<Bitmap> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Bitmap>?,
                            isFirstResource: Boolean,
                        ): Boolean {
                            return false;
                        }

                        override fun onResourceReady(
                            resource: Bitmap?,
                            model: Any?,
                            target: Target<Bitmap>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean,
                        ): Boolean {
                            requireActivity().runOnUiThread {
                                binding.ProfImage.setImageBitmap(resource)
                            }
                            val baos = ByteArrayOutputStream()
                            resource?.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                            uploader.UploadImage(it, baos.toByteArray())
                                .addOnProgressListener {
                                    val transfered = it.bytesTransferred
                                    val total = it.totalByteCount

                                    val progress = (transfered * 100) / total
                                    binding.updateImgUploadIndecator.setProgress(
                                        progress.toInt(),
                                        true
                                    )
                                }
                                .addOnSuccessListener {
                                    uploader.downloadUri.addOnSuccessListener { uri ->
                                        profileShared.edit()
                                            .putString("profile pic", uri.toString())
                                            .apply()
                                        mainInfo.profileImgUri = uri.toString()
                                        FirebaseFirestore.getInstance()
                                            .collection(Variables.FireStoreUsersRoot)
                                            .document(user.uid)
                                            .set(mainInfo).addOnSuccessListener {
                                                val request = userProfileChangeRequest {
                                                    photoUri = uri
                                                }
                                                user.updateProfile(request)

                                                Toast.makeText(
                                                    requireContext(),
                                                    "image uploaded",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                }
                            return true
                        }
                    })
                    .submit()
                binding.ProfImage.invalidate()
            }
        }

        binding.NameTextInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::mainInfo.isInitialized) {
                    binding.cancleBtn.isVisible = s.toString() != mainInfo.name
                    Log.d(
                        TAG,
                        "onTextChanged: new name: ${s.toString()} , actual name: ${mainInfo.name}"
                    )
                }


            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        binding.EmailTextInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
        binding.GenderTextInputLayout3.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::mainInfo.isInitialized) {
                    binding.cancleBtn.isVisible = s.toString() != mainInfo.name
                    Log.d(
                        TAG,
                        "onTextChanged: new name: ${s.toString()} , actual name: ${mainInfo.name}"
                    )
                }

            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
        binding.DiagnosesTextInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
        binding.BloodTypeTextInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::mainInfo.isInitialized) {
                    binding.cancleBtn.isVisible = s.toString() != mainInfo.name
                    Log.d(
                        TAG,
                        "onTextChanged: new name: ${s.toString()} , actual name: ${mainInfo.name}"
                    )
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
        binding.CountryTextInputLayout3.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::mainInfo.isInitialized) {
                    binding.cancleBtn.isVisible = s.toString() != mainInfo.name
                    Log.d(
                        TAG,
                        "onTextChanged: new name: ${s.toString()} , actual name: ${mainInfo.name}"
                    )
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })



        binding.phoneNumberTextInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::mainInfo.isInitialized) {
                    binding.cancleBtn.isVisible = s.toString() != mainInfo.name
                    Log.d(
                        TAG,
                        "onTextChanged: new name: ${s.toString()} , actual name: ${mainInfo.name}"
                    )
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
    }

    override fun setUpClicks() {
        binding.toolbar.setNavigationOnClickListener {
            controller.popBackStack()
        }
        binding.cancleBtn.setOnClickListener {
            mainInfo.name = binding.NameTextInputLayout.editText?.text.toString()
            mainInfo.gender = binding.GenderTextInputLayout3.editText?.text.toString()
            mainInfo.BloodType = binding.BloodTypeTextInputLayout.editText?.text.toString()
            mainInfo.phoneNumber = binding.phoneNumberTextInputLayout.editText?.text.toString()
            mainInfo.gender = binding.GenderTextInputLayout3.editText?.text.toString()
            mainInfo.country = binding.CountryTextInputLayout3.editText?.text.toString()
            mainInfo.BirthDay = binding.birthDayTextInputLayout.editText?.text.toString()
            mainInfo.uid = user.uid


            FirebaseFirestore.getInstance().collection(Variables.FireStoreUsersRoot)
                .document(user.uid)
                .set(mainInfo)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Updated", Toast.LENGTH_SHORT).show()
                }

        }
        binding.resetPassword.setOnClickListener {
            FirebaseAuth.getInstance().sendPasswordResetEmail(user.email!!).addOnSuccessListener {
                val builder: AlertDialog.Builder =
                    AlertDialog.Builder(requireContext())
                        .setView(R.layout.verify_email_dialog_layout)
                        .setOnDismissListener {
                            controller.popBackStack()
                        }
                val dialog = builder.create()
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
                Toast.makeText(
                    requireContext(),
                    "reset email sent to mail box",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
        binding.changeEmailBtn.setOnClickListener {
//            val user = FirebaseAuth.getInstance().currentUser
//
//            val credential = EmailAuthProvider.getCredential("user@example.com",
//                "password1234")
//
//            user!!.reauthenticate(credential).addOnSuccessListener {
//                val user = FirebaseAuth.getInstance().currentUser
//                user!!.updateEmail("user@example.com").addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        Log.d(TAG, "User email address updated.")
//                    }
//                }
//            }
        }
        binding.editImageBtn.setOnClickListener {
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.birthDayTextInputLayout.setEndIconOnClickListener {
            datePacker.show(childFragmentManager, "date")
            datePacker.addOnPositiveButtonClickListener {
                val date: Date = Date(it)
                val dateString = SimpleDateFormat("dd/MM/YYYY").format(date)
                binding.birthDayTextInputLayout.editText?.setText(dateString)
            }
        }

        binding.CountryTextInputLayout3.setEndIconOnClickListener {
            CoroutineScope(lifecycleScope.coroutineContext).launch {
                try {
                    val geoInfo = viewModel.geoInfo()
                    Log.d(TAG, "onViewCreated: $geoInfo")
                    binding.CountryTextInputLayout3.editText?.setText("${geoInfo.country}-${geoInfo.city}-${geoInfo.regionName}   ${geoInfo.lat},${geoInfo.lon}")
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "error in internet", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "setUpClicks: ${e.message}")
                }
            }
        }
        binding.ProfImage.setOnClickListener {
            val extras = FragmentNavigatorExtras(
                binding.ProfImage to "profile_img_big"
            )
            controller.navigate(
                R.id.action_editProfileFragment_to_profileImageFragment,
                null,
                null,
                extras
            )
        }

        binding.signoutBtn.setOnClickListener {
            if (AccessToken.getCurrentAccessToken() != null) {
                GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/me/permissions/",
                    null,
                    HttpMethod.DELETE, {
                        auth.signOut()
                        LoginManager.getInstance().logOut()
                        requireActivity().startActivity(
                            Intent(
                                requireContext(),
                                StartActivity::class.java
                            )
                        )
                        requireActivity().finishAffinity()
                    }).executeAsync()
            } else {
                client.signOut()
                auth.signOut()
                requireActivity().startActivity(Intent(requireContext(), StartActivity::class.java))
                requireActivity().finishAffinity()
            }
        }
    }

}