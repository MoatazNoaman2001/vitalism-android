package com.example.livenativerppg.models.vitalSignSelect.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.material.AlertDialog
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.livenativerppg.R
import com.example.livenativerppg.commons.makeToast
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.FragmentHomeBinding
import com.example.livenativerppg.models.startActivity.ui.StartActivity
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.example.livenativerppg.models.vitalSignDetails.ui.DetailsVitalSignFragment.Companion.getInstance
import com.example.livenativerppg.models.vitalSignSelect.data.viewmodel.HomeVM
import com.facebook.AccessToken
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "HomeFragment"

@AndroidEntryPoint
class HomeFragment : Fragment(), VitalSignsAdapter.onClickListener {
    lateinit var binding: FragmentHomeBinding
    lateinit var auth: FirebaseAuth
    lateinit var user: FirebaseUser
    lateinit var controller: NavController
    private var dialog: AlertDialog? = null

    private val homeVM: HomeVM by viewModels()


    @Inject
    lateinit var imageLoad: RequestManager


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        sharedElementReturnTransition =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.homeVM = homeVM
        controller = findNavController(requireView())
        val acccessToken = AccessToken.getCurrentAccessToken()


        imageLoad.load(user.photoUrl)
            .into(binding.userImageView)
        homeVM.homeModel.observe(viewLifecycleOwner) {
            it.txtMahmoudAlyosif = user.displayName
            homeVM.homeModel.postValue(it)
        }

        val adpater = VitalSignsAdapter(this, true, Glide.with(requireContext()), this)
        binding.fourMainVitalSigns.adapter = adpater

        binding.fourMainVitalSigns.doOnPreDraw {
            startPostponedEnterTransition()
        }
        binding.notificationBtn.setOnClickListener {
            val extras = FragmentNavigatorExtras(
                binding.upperCollapsingBar to "small_tool_bar"
            )
            controller.navigate(
                R.id.action_homeFragment_to_notificationFragment,
                null,
                null,
                extras
            )
        }
        homeVM.notifications.observe(viewLifecycleOwner) {
            binding.viewEllipse709.isVisible = !it.data?.filter { !it.connRequest.Accpeted!! }.isNullOrEmpty()
        }
        binding.notificationBtn.setOnLongClickListener {
            if (homeVM.notifications.value?.data?.filter { !it.connRequest.Accpeted!! }?.size == 0)
                makeToast(requireContext() , "you have No Notifications")
            else
                makeToast(requireContext(), "you have ${homeVM.notifications.value?.data?.size} Notifications")
            true
        }
        binding.searchTextInputLayout.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus){
                val extras = FragmentNavigatorExtras(
                    binding.searchTextInputLayout to "searchInput"
                )
                try {
                    controller.navigate(
                        R.id.action_homeFragment_to_searchMainFragment, null, null, extras
                    )
                } catch (e: Exception) {
                    Log.d(TAG, "onViewCreated: ${e.message}")
                }
            }
        }
        binding.searchTextInputLayout.setOnClickListener {
            val extras = FragmentNavigatorExtras(
                binding.searchTextInputLayout to "searchInput"
            )
            try {
                controller.navigate(
                    R.id.action_homeFragment_to_searchMainFragment, null, null, extras
                )
            } catch (e: Exception) {
                Log.d(TAG, "onViewCreated: ${e.message}")
            }
        }

        FirebaseFirestore.getInstance()
            .collection(Variables.FireStoreUsersRoot)
            .document(user.uid)
            .get()
            .addOnSuccessListener {
                if (it.exists()) {
                    val info = it.toObject(UserInfo::class.java)
                    if (info?.uid == null) {
                        info?.uid = user.uid
                        FirebaseMessaging.getInstance().token.addOnSuccessListener {
                            info?.token = it

                            FirebaseFirestore.getInstance()
                                .collection(Variables.FireStoreUsersRoot)
                                .document(user.uid).set(info!!)
                        }
                    }
                } else {
                    val builder = AlertDialog.Builder(requireContext())
                        .setTitle("Profile not exist")
                        .setMessage("your profile cant be found please sign up again")
                        .setCancelable(false)
                        .setPositiveButton("OK") { _, _ ->
                            auth.signOut()
                            requireActivity().startActivity(
                                Intent(
                                    requireContext(),
                                    StartActivity::class.java
                                )
                            )
                            requireActivity().finishAffinity()
                        }
                    if (dialog == null)
                        dialog = builder.create()
                    if (!dialog?.isShowing!!)
                        dialog?.show()
                }
            }

    }

    override fun onCick(
        imageView: AppCompatImageView?,
        textView1: AppCompatTextView?,
        textView2: AppCompatTextView?,
        value: Int,
    ) {
        val extras = FragmentNavigatorExtras(
//            (imageView as ShapeableImageView).apply {
//                transitionName += "_$value"
//            } to "big_title",
            (textView1 as AppCompatTextView).apply {
                transitionName += "_$value"
            } to "more_details",
            (textView2 as AppCompatTextView).apply {
                transitionName += "_$value"
            } to "big_img"
        )
        controller.navigate(
            R.id.action_homeFragment_to_detailsVitalSignFragment,
            getInstance(value).requireArguments(),
            null,
            extras
        )
    }


}