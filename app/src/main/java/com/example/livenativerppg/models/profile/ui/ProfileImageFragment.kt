package com.example.livenativerppg.models.profile.ui

import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.RequestManager
import com.example.livenativerppg.R
import com.example.livenativerppg.component.base.BaseFragment
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.FragmentProfileImageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val UID = "uid"

@AndroidEntryPoint
class ProfileImageFragment() :
    BaseFragment<FragmentProfileImageBinding>(R.layout.fragment_profile_image) {
    lateinit var controller: NavController
    lateinit var user: FirebaseUser
    lateinit var uid: String

    @Inject
    lateinit var imageLoader: RequestManager

    override fun onInitialized() {
        super.onInitialized()
        controller = Navigation.findNavController(requireView())
        user = FirebaseAuth.getInstance().currentUser!!
        val transition =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        sharedElementEnterTransition = transition
        if (arguments != null) {
            uid = requireArguments().getString(UID).toString()
            FirebaseStorage.getInstance()
                .getReference(Variables.FireStoreUsersRoot).child(uid)
                .child(Variables.UserProfilePic).downloadUrl.addOnSuccessListener {
                    imageLoader.load(it).into(binding.profileImage)
                }

        } else
            imageLoader.load(user.photoUrl).into(binding.profileImage)
    }


    companion object {
        fun getInstance(uid: String) = ProfileImageFragment().apply {
            arguments = Bundle().apply {
                putString(UID, uid)
            }
        }
    }

    override fun setUpClicks() {
        binding.toolbar.setNavigationOnClickListener {
            controller.popBackStack()
        }
    }
}