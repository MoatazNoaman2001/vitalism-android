package com.example.livenativerppg.models.startActivity.ui.fragments.startFragment.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.livenativerppg.R
import com.example.livenativerppg.commons.ErrorLoginHandler
import com.example.livenativerppg.commons.startingAccountFirstTime
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.FragmentFirstBinding
import com.example.livenativerppg.models.mainAppPage.ui.MainAppPageActivity
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.example.livenativerppg.models.startActivity.ui.fragments.startFragment.data.viewModel.StartFragmentViewModel
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class StartFragment : Fragment() {

    private val TAG = "StartFragment"

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var controller: NavController
    lateinit var handler: Handler

    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var preferences: SharedPreferences
    @Inject
    lateinit var myDocRef : CollectionReference


    private val viewModel: StartFragmentViewModel by viewModels<StartFragmentViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentFirstBinding.inflate(layoutInflater)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller = Navigation.findNavController(requireView())
        handler = Handler()
        binding.startFragmentViewModel = viewModel
        FirebaseApp.initializeApp(requireContext())

        auth = FirebaseAuth.getInstance()

        Glide.with(requireContext())
            .asGif()
            .load(R.drawable.enter_logo_anim)
            .into(binding.logo)
        val user = FirebaseAuth.getInstance().currentUser

        if (AccessToken.getCurrentAccessToken() != null) {
            myDocRef.document(user!!.uid).get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        val userInfo = it.toObject(UserInfo::class.java)
                        if (!userInfo?.BloodType.isNullOrEmpty() && !userInfo?.userType.isNullOrEmpty()) {
                            Log.d(TAG, "onViewCreated:user id: ${userInfo?.uid} blood type: ${userInfo?.BloodType} , userType: ${userInfo?.userType}")
                            if (!preferences.getBoolean(Variables.Start_Pager_Visited, false))
                                checkUserFirstOpen()
                            requireActivity().startActivity(Intent(requireActivity(), MainAppPageActivity::class.java))
                            requireActivity().finishAffinity()

                        } else {
                            Handler().postDelayed({
                                controller.navigate(
                                    R.id.action_FirstFragment_to_userTypeSelectionFragment
                                )
                            }, 3000)
                        }

                    } else {
                        Handler().postDelayed({
                            if (this@StartFragment.isVisible) {
                                controller.navigate(R.id.action_FirstFragment_to_userTypeSelectionFragment)
                            }
                        }, 3000)
                    }
                }.addOnFailureListener {
                    Log.d(TAG, "onViewCreated: " + it.message)
                    it.printStackTrace()
                    preferences = requireActivity().getSharedPreferences(
                        Variables.Utils,
                        Context.MODE_PRIVATE
                    )
                    if (it is FirebaseNetworkException)
                        checkUserFirstOpen()
                    else {
                        if (!preferences.getBoolean(Variables.Start_Pager_Visited, false))
                            controller.navigate(R.id.action_FirstFragment_to_pagerStartInstructionFragment)
                        else
                            controller.navigate(R.id.action_FirstFragment_to_SecondFragment)
                    }
                }
        } else if (GoogleSignIn.getLastSignedInAccount(requireContext()) == null || auth.currentUser == null) {
            if (auth.currentUser == null || !auth.currentUser!!.isEmailVerified)
                binding.logo.postDelayed({
                    if (this@StartFragment.isVisible) {
                        val extras = FragmentNavigatorExtras(binding.logo to "vitalism_logo_big")
                        controller.navigate(
                            R.id.action_FirstFragment_to_SecondFragment,
                            null,
                            null,
                            extras
                        )
                    }
                }, 3000)
            else if (auth.currentUser != null && auth.currentUser!!.isEmailVerified) {
                FirebaseFirestore.getInstance().collection(Variables.FireStoreUsersRoot)
                    .document(auth.currentUser!!.uid).get().addOnSuccessListener {
                        if (it.exists()) {
                            val userInfo = it.toObject(UserInfo::class.java)
                            if (!userInfo?.BloodType.isNullOrEmpty() && !userInfo?.userType.isNullOrEmpty()) {
                                Log.d(
                                    TAG,
                                    "onViewCreated:user id: ${userInfo?.uid} blood type: ${userInfo?.BloodType} , userType: ${userInfo?.userType}"
                                )
                                checkUserFirstOpen()
                            } else {
                                Handler().postDelayed({
                                    controller.navigate(
                                        R.id.action_FirstFragment_to_userTypeSelectionFragment
                                    )
                                }, 3000)
                            }

                        } else {
                            Handler().postDelayed({
                                if (this@StartFragment.isVisible) {
                                    controller.navigate(R.id.action_FirstFragment_to_userTypeSelectionFragment)
                                }
                            }, 3000)
                        }
                    }.addOnFailureListener {
                        Log.d(TAG, "onViewCreated: " + it.message)
                        it.printStackTrace()
                        preferences = requireActivity().getSharedPreferences(
                            Variables.Utils,
                            Context.MODE_PRIVATE
                        )
                        if (it is FirebaseNetworkException)
                            checkUserFirstOpen()

                        else {
                            if (!preferences.getBoolean(Variables.Start_Pager_Visited, false))
                                controller.navigate(R.id.action_FirstFragment_to_pagerStartInstructionFragment)
                            else
                                controller.navigate(R.id.action_FirstFragment_to_SecondFragment)
                        }
                    }
            }
        } else if (auth.currentUser?.isEmailVerified!!) {
            binding.logo.postDelayed({
                if (this@StartFragment.isVisible) {
                    preferences = requireActivity().getSharedPreferences(
                        Variables.Utils,
                        Context.MODE_PRIVATE
                    )
                    checkUserFirstOpen()

                }
            }, 3000)
        } else if (auth.currentUser != null && auth.currentUser?.email.isNullOrEmpty()) {
            Handler().postDelayed({
                checkUserFirstOpen()
            }, 3000)
        } else {
            FirebaseFirestore.getInstance().collection(Variables.FireStoreUsersRoot)
                .document(auth?.currentUser!!.uid).get().addOnSuccessListener {
                    if (it.exists()) {
                        val userInfo = it.toObject(UserInfo::class.java)
                        if (!userInfo?.BloodType.isNullOrEmpty() && !userInfo?.userType.isNullOrEmpty()) {
                            Handler().postDelayed({
                                controller.navigate(
                                    R.id.action_FirstFragment_to_userTypeSelectionFragment
                                )
                            }, 3000)
                        } else {
                            checkUserFirstOpen()

                        }

                    } else {
                        Handler().postDelayed({
                            if (this@StartFragment.isVisible) {
                                controller.navigate(R.id.action_FirstFragment_to_userTypeSelectionFragment)
                            }
                        }, 3000)
                    }
                }


        }
        Log.d(TAG, "onViewCreated: user: ${auth.currentUser} , is veriefied: ${auth.currentUser?.isEmailVerified}")

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}