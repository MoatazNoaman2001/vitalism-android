package com.example.livenativerppg.models.follow.ui

import android.util.Log
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.RequestManager
import com.example.livenativerppg.R
import com.example.livenativerppg.component.base.BaseFragment
import com.example.livenativerppg.component.di.MyFollowRef
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.FragmentPeapleIFollowBinding
import com.example.livenativerppg.models.follow.data.viewModel.FollowVM
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named


private const val TAG = "PeopleIFollowFragment"

@AndroidEntryPoint
class PeopleIFollowFragment :
    BaseFragment<FragmentPeapleIFollowBinding>(R.layout.fragment_peaple_i_follow) {

    private val followVM by viewModels<FollowVM>()

    lateinit var adapter: FollowRecycleAdapter
    lateinit var controller: NavController

    @Inject
    lateinit var imageLoader: RequestManager

    @Inject
    @Named("followers")
    lateinit var myFollowRef: CollectionReference

    override fun onInitialized() {
        super.onInitialized()
        controller = Navigation.findNavController(requireView())
        adapter = FollowRecycleAdapter(imageLoader)
        binding.recycleView.adapter = adapter;
        FirebaseFirestore.getInstance()
            .collection(Variables.FireStoreUsersRoot)
            .document(FirebaseAuth.getInstance().currentUser?.uid!!)
            .get()
            .addOnSuccessListener {
                val user = it.toObject(UserInfo::class.java)
                CoroutineScope(lifecycleScope.coroutineContext).launch {
                    val l = followVM.followers()
                    requireActivity().runOnUiThread {
                        adapter.submitList(l)
                    }
                }
//                if (user?.userType.equals(UserType.Medical.name)) {
//                } else {
//                    CoroutineScope(lifecycleScope.coroutineContext).launch {
//                        val l = followVM.connections()
//                        requireActivity().runOnUiThread {
//                            adapter.submitList(l)
//                        }
//                    }
//                }
            }

    }

    override fun setUpClicks() {

        binding.toolbar.setNavigationOnClickListener {
            controller.popBackStack()
        }
    }
}