package com.example.livenativerppg.models.profile.ui

import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.ExpandableListAdapter
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.livenativerppg.R
import com.example.livenativerppg.commons.ppgDateFormat
import com.example.livenativerppg.commons.rppgDateFormat
import com.example.livenativerppg.component.base.BaseFragment
import com.example.livenativerppg.component.db.models.PPGResult
import com.example.livenativerppg.component.natives.RPPGListener
import com.example.livenativerppg.component.natives.RPPGListenerList
import com.example.livenativerppg.component.natives.RPPGResult
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.FragmentProfileBinding
import com.example.livenativerppg.models.profile.data.model.ExpandableItem
import com.example.livenativerppg.models.profile.data.viewModel.ProfileVM
import com.example.livenativerppg.models.profile.ui.CustomExpandableListViewAdapter.OnItemClickListener
import com.example.livenativerppg.models.startActivity.ui.StartActivity
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.facebook.login.LoginManager
import com.google.android.datatransport.runtime.dagger.Provides
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentReference
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Double
import java.util.Date
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.ceil


private const val TAG = "ProfileFragment"


@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>(R.layout.fragment_profile),
    OnItemClickListener {
    private val viewModel by viewModels<ProfileVM>()
    lateinit var user: FirebaseUser
    lateinit var controller: NavController

    lateinit var client: GoogleSignInClient
    lateinit var gso: GoogleSignInOptions
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var profileShared: SharedPreferences

    @Named(value = "emr")
    @Inject
    lateinit var myMeasurementRef: DatabaseReference

    @Inject
    @Named(Variables.PPG_RF_PATH)
    lateinit var ppgRfDatabaseReference: DatabaseReference

    @Inject
    @Named(Variables.PPG_HR_PATH)
    lateinit var ppgHRDatabaseReference: DatabaseReference

    @Inject
    @Named(Variables.PPG_Bp_PATH)
    lateinit var ppgBpDatabaseReference: DatabaseReference

    @Inject
    @Named(Variables.PPG_o2_PATH)
    lateinit var ppgSpo2DatabaseReference: DatabaseReference


    @Inject
    lateinit var imageLoader: RequestManager

    override fun onInitialized() {
        super.onInitialized()
        user = FirebaseAuth.getInstance().currentUser!!
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()
        auth = FirebaseAuth.getInstance()

        client = GoogleSignIn.getClient(requireActivity(), gso)
        controller = Navigation.findNavController(requireView())

        Log.d(TAG, "onInitialized: fragment: $myMeasurementRef")
        binding.profileVM = viewModel
        viewModel.profileModel.observe(viewLifecycleOwner) {
            it.txtMahmoudSayedY = user.displayName
            it.txtMahmoudAlyosifyOne =
                if (user.email != null && user.email!!.isNotEmpty()) user.email else profileShared?.getString(
                    "email",
                    ""
                )
            imageLoader.asBitmap().circleCrop().load(user.photoUrl)
                .into(binding.profileImg)
        }

        val details = createExpandableList()

        bindHeartRate()

        CoroutineScope(Dispatchers.IO).launch {
            launch {
                val result = ppgRfDatabaseReference.get().await().children.toList()
                var rf = 0L
                var hrTorr = 0L
                if (result.isNotEmpty()) {
                    result.last {
                        it.children.last().children.forEach {
                            if (it.key == "rf") {
                                rf = it.value as Long
                            } else if (it.key == "hrTorr") {
                                hrTorr = it.value as Long
                            }
                        }

                        MainScope().launch {
                            if (rf != 0L)
                                binding.txtTwenty.text = "$rf"
                            if (hrTorr != 0L)
                                binding.txtNone.text = "$hrTorr"

                        }

                        true
                    }
                }
            }

            launch {
                val result = ppgBpDatabaseReference.get().await().children.toList()
                var sp: Long = 0
                var dp: Long = 0
                if (result.isNotEmpty())
                    result.last {
                        it.children.last().children.forEach {
                            if (it.key == "sp") {
                                sp = it.value as Long
                            } else if (it.key == "dp") {
                                dp = it.value as Long
                            }
                        }

                        MainScope().launch {
                            binding.BpValueTextView.text = "${sp}/${dp}"
                        }

                        true
                    }
            }

            launch {
                val result = ppgSpo2DatabaseReference.get().await().children.toList()
                var o2: Long = 0
                if (result.isNotEmpty())
                    result.last {
                        it.children.last().children.forEach {
                            if (it.key == "o2") {
                                o2 = it.value as Long
                            }
                        }
                        MainScope().launch {
                            binding.spo2ValueTextView.text = "$o2/100"
                        }

                        true
                    }
            }
        }

        val expandableAdapter = CustomExpandableListViewAdapter(requireContext(), details, this)
        binding.utilsExpandableListView.adapter = expandableAdapter
    }

    private fun bindHeartRate() {
        myMeasurementRef.get().addOnSuccessListener {
            if (it.exists()) {
                Log.d(TAG, "bindHeartRate: ${it.value.toString()}")
                try {
                    val list: ArrayList<RPPGResult> =
                        it.children.sortedBy { rppgDateFormat.parse(it.key) }
                            .map { it.children.last().children.last() }
                            .map {
                                if (it.key.toString() == "average")
                                    return@map RPPGResult(
                                        0L,
                                        it.value.toString().toDouble(),
                                        it.value.toString().toDouble() - 5,
                                        it.value.toString().toDouble() + 5,
                                    )
                                else {
                                    try {
                                        return@map it.getValue(RPPGResult::class.java)!!
                                    } catch (e: Exception) {
                                        try {
                                            val rppg = Gson().fromJson(
                                                it.value.toString(),
                                                RPPGResult::class.java
                                            )
                                            return@map rppg!!
                                        } catch (_: Exception) {
                                            Log.d(
                                                TAG,
                                                "bindHeartRate: cant convert to rppgResult any more for that ${it.value.toString()}"
                                            )
                                            return@map RPPGResult()
                                        }
                                    }
                                }

                            }
                            .toCollection(ArrayList())

                    if (list.isEmpty())
                        return@addOnSuccessListener
                    Log.d(TAG, "bindHeartRate: list values ${list.joinToString { it.toString() }}")
                    binding.hrValueTextView.text =
                        ceil((list.sumOf { it.mean } / list.count())).toString()
                } catch (e: Exception) {
                    Log.d(TAG, "bindHeartRate: ${e.message}")
                    binding.hrValueTextView.text = "None"
                }
            } else {
                binding.hrValueTextView.text = "None"
            }
        }
    }

    override fun setUpClicks() {
        binding.frameStacklastheartrate.setOnClickListener {
            controller.navigate(R.id.action_profileFragment_to_emrVisualizationFragment)
        }
        binding.toolbar.setNavigationOnClickListener {
            controller.popBackStack()
        }
        binding.toolbar.setOnMenuItemClickListener {
            controller.navigate(R.id.action_profileFragment_to_editProfileFragment)
            true
        }
        binding.profileImg.setOnClickListener {
            val extras = FragmentNavigatorExtras(
                binding.profileImg to "profile_img_big"
            )
            controller.navigate(
                R.id.action_profileFragment_to_profileImageFragment,
                null,
                null,
                extras
            )
        }
    }

    fun createExpandableList(): ArrayList<ExpandableItem> {
        return ArrayList<ExpandableItem>().apply {
            add(
                ExpandableItem(
                    R.drawable.img_user_18x14,
                    "Electronic Medical Records (EMR)",
                    R.drawable.img_arrowright
                )
            )
            add(
                ExpandableItem(
                    R.drawable.img_rectangle815,
                    "Vital Signs Details",
                    R.drawable.img_arrowright
                )
            )
            add(
                ExpandableItem(
                    R.drawable.ic_outline_person_outline_24,
                    "people you follow",
                    R.drawable.img_arrowright
                )
            )
            add(ExpandableItem(R.drawable.img_settings, "Settings", R.drawable.img_arrowright))
            add(
                ExpandableItem(
                    R.drawable.img_settings_23x24,
                    "Version of Vitalism",
                    R.drawable.img_arrowright
                )
            )
            add(
                ExpandableItem(
                    R.drawable.baseline_museum_24,
                    "About Us",
                    R.drawable.img_arrowright
                )
            )

            add(
                ExpandableItem(
                    R.drawable.ic_logout,
                    "log out",
                    R.drawable.img_arrowright
                )
            )
        }
    }

    override fun clickOn(textView: AppCompatTextView?, pos: Int) {
        val extras = FragmentNavigatorExtras(
            textView as AppCompatTextView to "toolbar_title"
        )
        when (pos) {
            0 -> {
                controller.navigate(
                    R.id.action_profileFragment_to_EMRListFragment,
                    null,
                    null,
                    extras
                )
            }
            3 -> {
                controller.navigate(
                    R.id.action_profileFragment_to_profileSettingFragment,
                    null,
                    null,
                    extras
                )
            }
            2 -> {
                controller.navigate(R.id.action_profileFragment_to_peopleIFollowFragment)
            }
            5 -> {
                controller.navigate(
                    R.id.action_profileFragment_to_aboutFragment,
                    null,
                    null,
                    extras
                )
            }
            6 -> {
                logoutfun()
            }
        }
    }

    private fun logoutfun() {
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