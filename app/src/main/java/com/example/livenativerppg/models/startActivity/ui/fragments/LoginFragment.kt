package com.example.livenativerppg.models.startActivity.ui.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.livenativerppg.R
import com.example.livenativerppg.commons.ErrorLoginHandler
import com.example.livenativerppg.commons.makeToast
import com.example.livenativerppg.commons.startingAccountFirstTime
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.FragmentSecondBinding
import com.example.livenativerppg.models.mainAppPage.ui.MainAppPageActivity
import com.example.livenativerppg.models.startActivity.data.viewmodel.LoginToVitalismAppVM
import com.example.livenativerppg.models.startActivity.profilePicSelection.ui.ProfilePicSelectFragment
import com.example.livenativerppg.models.startActivity.ui.fragments.UserTypeSelector.ui.UserTypeSelectionFragment
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.errorprone.annotations.Var
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONException
import java.util.regex.Pattern
import javax.inject.Inject


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */

private const val TAG = "LoginFragment"

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val viewModel: LoginToVitalismAppVM by viewModels()
    lateinit var controller: NavController
    lateinit var auth: FirebaseAuth
    lateinit var user: FirebaseUser
    lateinit var client: GoogleSignInClient
    lateinit var gso: GoogleSignInOptions
    lateinit var oneTapClient: SignInClient
    lateinit var signInReqest: BeginSignInRequest
    lateinit var googleAccountRequestLauncher: ActivityResultLauncher<Intent>
    var info: UserInfo? = null
    lateinit var callbackmanger: CallbackManager
    private var isLoginClicked = false;

    @Inject
    lateinit var prfileShared: SharedPreferences

    lateinit var profileEditor: SharedPreferences.Editor
//        UserInfo("moataz" , "moataz.noaman12@gmail.com" , "22/07/2001" , "B+" , "Diagnose 4" , "egypt" )

    private val EmailPattern = Pattern.compile(
        "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"
    )
    private val PasswordPattern = Pattern.compile(
        "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!%*#?&]{8,}\$"
    )


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        binding.loginToVitalismAppVM = viewModel
        auth = FirebaseAuth.getInstance()
        profileEditor = prfileShared.edit()


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller = Navigation.findNavController(requireView())
        if (viewModel.navArguments != null) {
            info = viewModel.navArguments!!.getSerializable("info") as UserInfo
        }

        oneTapClient = Identity.getSignInClient(requireContext())
        signInReqest = BeginSignInRequest.builder()
            .setPasswordRequestOptions(
                BeginSignInRequest.PasswordRequestOptions.builder()
                    .setSupported(true)
                    .build()
            )
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.web_client_id))
                    // Only show accounts previously used to sign in.
//                    .setFilterByAuthorizedAccounts(true)
                    .build()
            )
            // Automatically sign in when exactly one credential is retrieved.
//            .setAutoSelectEnabled(true)
            .build()

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        client = GoogleSignIn.getClient(requireActivity(), gso)
        googleAccountRequestLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(it.data)
                task.addOnSuccessListener { googleSignInAccount ->
                    fireBaseAuthWithGoogle(googleSignInAccount)
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "google login failed", Toast.LENGTH_SHORT)
                        .show()
                    it.printStackTrace()
                }
            }

        callbackmanger = CallbackManager.Factory.create();
        LoginManager.getInstance()
            .registerCallback(callbackmanger, object : FacebookCallback<LoginResult> {
                override fun onCancel() {
                    Toast.makeText(requireContext(), "login canceled", Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: FacebookException) {
                    Log.d(TAG, "onError: ${error.message}")
                    makeToast(requireContext(), "error in login with faceBook")
                }

                override fun onSuccess(result: LoginResult) {
                    Log.d(TAG, "onSuccess: token: ${result.accessToken.token}")
                    GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken()) { json, response ->
                        if (response!!.error != null) {
                            println("ERROR")
                        } else {
                            println("Success")
                            try {
                                val jsonresult = json.toString()
                                println("JSON Result$jsonresult")
                                val str_name = json?.getString("name")
                                val str_email = json?.getString("email")
                                println(str_email)

                                handleFacebookAccessToken(
                                    result.accessToken,
                                    str_name!!,
                                    str_email!!
                                )

                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }.apply {
                        parameters = Bundle().apply {
                            putString("fields", "id,name,email")
                        }
                    }.executeAsync()
                }

                private fun handleFacebookAccessToken(
                    accessToken: AccessToken,
                    str_name: String,
                    str_email: String,
                ) {
                    val credential = FacebookAuthProvider.getCredential(accessToken.token)
                    auth.signInWithCredential(credential).addOnSuccessListener {
                        val request = userProfileChangeRequest {
                            displayName = str_name
                        }
                        user = auth.currentUser!!
                        user.updateProfile(request).addOnSuccessListener {
                            user = FirebaseAuth.getInstance().currentUser!!
                            FirebaseFirestore.getInstance().collection(Variables.FireStoreUsersRoot)
                                .document(user.uid).get().addOnSuccessListener {
                                    if (it.exists()) {
                                        info = it.toObject(UserInfo::class.java)
                                        profileEditor.putString(
                                            Variables.USER_INFO,
                                            Gson().toJson(info, UserInfo::class.java)
                                        ).apply()

                                        with(profileEditor) {
                                            putString("name", info?.name).apply()
                                            putString("email", str_email).apply()
                                            putString("bloodType", info?.BloodType).apply()
                                            putString("Diagnose", info?.Diagnoses).apply()
                                            putString("country", info?.country).apply()
                                            putString("gender", info?.gender).apply()
                                            putString("birthDay", info?.BirthDay).apply()
                                            putString("phoneNumber", info?.phoneNumber).apply()
                                        }
                                        if (info?.userType.isNullOrEmpty() || info?.BloodType.isNullOrEmpty())
                                            try {
                                                controller.navigate(R.id.action_SecondFragment_to_userTypeSelectionFragment)
                                            } catch (_: Exception) {
                                            }
                                        else {
                                            if (!prfileShared.getBoolean(
                                                    Variables.Start_Pager_Visited,
                                                    false
                                                )
                                            )
                                                checkUserFirstOpen()
                                            else requireActivity().startActivity(
                                                Intent(
                                                    requireActivity(),
                                                    MainAppPageActivity::class.java
                                                )
                                            )
                                        }
                                    } else {
                                        controller.navigate(
                                            R.id.action_SecondFragment_to_userTypeSelectionFragment,
                                            UserTypeSelectionFragment.getInstance(str_email)
                                                .requireArguments()
                                        )
                                    }
                                }

                        }

                    }.addOnFailureListener {
                        ErrorLoginHandler(it, requireContext())
                    }
                }

            })

        binding.googleBtnMaterial.setOnClickListener {
            googleAccountRequestLauncher.launch(Intent(client.signInIntent))
        }

        binding.faceBookBtn.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(
                this,
                listOf("public_profile", "email")
            )
        }

        binding.txtForgetpassword.setOnClickListener {
            controller.navigate(R.id.action_SecondFragment_to_forgotenFragment)
        }

//        if (auth.currentUser != null)
//            if (!auth.currentUser!!.isEmailVerified) {
//                val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
//                    .setView(R.layout.verify_previcly_signed_email_dialog_layout)
//                    .setPositiveButton("ok") { _, _ ->
//                        run {
//                            auth.currentUser!!.sendEmailVerification().addOnSuccessListener {
//                                Toast.makeText(requireContext(),
//                                    "verification sent",
//                                    Toast.LENGTH_SHORT)
//                                    .show()
//                            }
//                        }
//                    }
//                    .setOnDismissListener() {
//
//                    }
//                val dialog = builder.create()
//                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//                dialog.show()
//            }
        binding.passwordEditLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.passwordEditLayout.isErrorEnabled)
                    binding.passwordEditLayout.isErrorEnabled = false
            }

            override fun afterTextChanged(s: Editable?) {}

        })

        binding.btnLogin.setOnClickListener {
            binding.btnLogin.isEnabled = false
            binding.btnRegister.isEnabled = false
            binding.loginLoading.isVisible = true

            val email = binding.etFrame451.editableText.toString()
            val password = binding.passwordEditLayout.editText?.editableText.toString()

            if (email.isEmpty() or password.isEmpty()) {
                Toast.makeText(requireContext(), "should fill all fields", Toast.LENGTH_SHORT)
                    .show()
            } else if (!PasswordPattern.matcher(password).matches()) {
                binding.passwordEditLayout.isErrorEnabled = true
                binding.passwordEditLayout.error = "invalid password"
            } else if (!EmailPattern.matcher(email).matches()) {
                makeToast(requireContext(), "email bad format")
            } else {
                auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
                    user = auth.currentUser!!
                    FirebaseFirestore.getInstance().collection(Variables.FireStoreUsersRoot)
                        .document(user.uid).get().addOnSuccessListener {
                            if (it.exists()) {
                                info = it.toObject(UserInfo::class.java)
                                profileEditor.putString(
                                    Variables.USER_INFO,
                                    Gson().toJson(info, UserInfo::class.java)
                                ).apply()

                                profileEditor.putString("name", info?.name).apply()
                                profileEditor.putString("email", info?.email).apply()
                                profileEditor.putString("bloodType", info?.BloodType).apply()
                                profileEditor.putString("Diagnose", info?.Diagnoses).apply()
                                profileEditor.putString("country", info?.country).apply()
                                profileEditor.putString("gender", info?.gender).apply()
                                profileEditor.putString("birthDay", info?.BirthDay).apply()
                                profileEditor.putString("phoneNumber", info?.phoneNumber).apply()

                                if (info?.userType.isNullOrEmpty() || info?.BloodType.isNullOrEmpty())
                                    try {
                                        controller.navigate(R.id.action_SecondFragment_to_userTypeSelectionFragment)
                                    } catch (e: Exception) {
                                        binding.btnLogin.isEnabled = true
                                        binding.btnRegister.isEnabled = true
                                        binding.loginLoading.isVisible = false
                                    }
                                else {
                                    try {
                                        checkUserFirstOpen()
                                    } catch (e: Exception) {
                                        binding.btnLogin.isEnabled = true
                                        binding.btnRegister.isEnabled = true
                                        binding.loginLoading.isVisible = false
                                    }
                                }
                            } else {
                                controller.navigate(R.id.action_SecondFragment_to_userTypeSelectionFragment)
                            }
                        }.addOnFailureListener {
                            binding.btnLogin.isEnabled = true
                            binding.btnRegister.isEnabled = true
                            binding.loginLoading.isVisible = false
                        }

                }.addOnFailureListener {
                    ErrorLoginHandler(it, requireContext())
                    binding.btnLogin.isEnabled = true
                    binding.btnRegister.isEnabled = true
                    binding.loginLoading.isVisible = false
                }
            }
        }
        binding.btnRegister.setOnClickListener {
            controller.navigate(R.id.action_SecondFragment_to_signUpFragment)
        }

        binding.btnFingerprint.setOnClickListener {
            Toast.makeText(requireContext(), "still working on", Toast.LENGTH_SHORT).show()
        }

        controller.currentBackStackEntry?.savedStateHandle?.getLiveData<UserInfo>("info")
            ?.observe(viewLifecycleOwner) {
                if (it != null) {
                    info = it
                    val data = Bundle()
                    data.putSerializable("info", info)
                    viewModel.navArguments = data
                    controller.currentBackStackEntry?.savedStateHandle?.set("info", null)
                }
            }
    }

    private fun fireBaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnSuccessListener {
                user = auth.currentUser!!
                Toast.makeText(requireContext(), "google success", Toast.LENGTH_SHORT).show()
                val builder = UserProfileChangeRequest.Builder()
                    .setDisplayName(account.displayName)
                    .setPhotoUri(account.photoUrl)
                FirebaseFirestore.getInstance().collection(Variables.FireStoreUsersRoot)
                    .document(user.uid)
                    .get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            info = it.toObject(UserInfo::class.java)
                            profileEditor.putString(
                                Variables.USER_INFO,
                                Gson().toJson(info, UserInfo::class.java)
                            ).apply()

                            profileEditor.putString("name", info?.name).apply()
                            profileEditor.putString("email", info?.email).apply()
                            profileEditor.putString("bloodType", info?.BloodType).apply()
                            profileEditor.putString("Diagnose", info?.Diagnoses).apply()
                            profileEditor.putString("country", info?.country).apply()
                            profileEditor.putString("gender", info?.gender).apply()
                            profileEditor.putString("birthDay", info?.BirthDay).apply()
                            profileEditor.putString("phoneNumber", info?.phoneNumber).apply()
                            if (info?.profileImgUri != null)
                                builder.photoUri = Uri.parse(info?.profileImgUri)
                            UpdateAcount(builder)
                            if (info?.userType.isNullOrEmpty() || info?.BloodType.isNullOrEmpty()) {
                                if (info?.profileImgUri == null) {
                                    FirebaseStorage.getInstance()
                                        .getReference(Variables.FireStoreUsersRoot)
                                        .child(info?.uid!!)
                                        .child(Variables.UserProfilePic)
                                        .downloadUrl
                                        .addOnSuccessListener {
                                            if (it != null) {
                                                info?.profileImgUri = it.toString()
                                                profileEditor.putString(
                                                    Variables.USER_INFO,
                                                    Gson().toJson(info, UserInfo::class.java)
                                                ).apply()

                                                FirebaseFirestore.getInstance()
                                                    .collection(Variables.FireStoreUsersRoot)
                                                    .document(info?.uid!!).set(info!!)
                                                try {
                                                    controller.navigate(R.id.action_SecondFragment_to_userTypeSelectionFragment)
                                                } catch (e: Exception) {
                                                }
                                            } else {
                                                try {
                                                    controller.navigate(
                                                        R.id.action_SecondFragment_to_profilePicSelectFragment,
                                                        ProfilePicSelectFragment.getInstance(info!!)
                                                            .requireArguments()
                                                    )
                                                } catch (e: Exception) {
                                                }
                                            }
                                        }.addOnFailureListener {
                                            try {
                                                controller.navigate(
                                                    R.id.action_SecondFragment_to_profilePicSelectFragment,
                                                    ProfilePicSelectFragment.getInstance(info!!)
                                                        .requireArguments()
                                                )
                                            } catch (e: Exception) {
                                            }
                                        }
                                } else {
                                    checkUserFirstOpen()
                                }
                            } else {
                                checkUserFirstOpen()
                            }
                        } else {
                            controller.navigate(R.id.action_SecondFragment_to_userTypeSelectionFragment)
                        }
                    }

            }
            .addOnFailureListener {
                it.printStackTrace()
            }


    }

    private fun UpdateAcount(builder: UserProfileChangeRequest.Builder) {
        user.updateProfile(builder.build()).addOnSuccessListener {

        }.addOnFailureListener {
            it.printStackTrace()
        }
    }

    private fun checkUserFirstOpen() {
        startingAccountFirstTime.get()
            .addOnSuccessListener {
                if (it.exists()) {
                    requireActivity().startActivity(
                        Intent(
                            requireContext(),
                            MainAppPageActivity::class.java
                        )
                    )
                } else {
                    startingAccountFirstTime.set(HashMap<String, Boolean>().apply {
                        put(
                            "isFirstOpen",
                            true
                        )
                    }).addOnSuccessListener {
                        requireActivity().startActivity(
                            Intent(
                                requireContext(),
                                MainAppPageActivity::class.java
                            )
                        )
                    }.addOnFailureListener {
                        ErrorLoginHandler(it, requireContext())
                    }
                }
            }.addOnFailureListener {
                ErrorLoginHandler(it, requireContext())
            }
    }

    override fun onResume() {
        super.onResume()
        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (event.action === KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                requireActivity().finish()
                true
            }
            false
        })
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (this.isVisible && binding.etFrame451.editableText.isEmpty() && binding.passwordEditLayout.editText?.editableText!!.isEmpty()) {
            outState.putString("email", binding.etFrame451.editableText.toString())
            outState.putString(
                "password",
                binding.passwordEditLayout.editText?.editableText.toString()
            )
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            binding.etFrame451.setText(savedInstanceState.getString("email").toString())
            binding.passwordEditLayout.editText?.setText(
                savedInstanceState.getString("password")
                    .toString()
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackmanger.onActivityResult(requestCode, resultCode, data)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}