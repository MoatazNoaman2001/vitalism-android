package com.example.livenativerppg.models.UserFollowedDetails.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.transition.TransitionInflater
import com.bumptech.glide.RequestManager
import com.example.livenativerppg.R
import com.example.livenativerppg.component.base.BaseFragment
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.FragmentUserFollowDetailsBinding
import com.example.livenativerppg.models.emr_EMR.ui.EMRListFragment
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


private const val UID = "uid"

@AndroidEntryPoint
class UserFollowDetailsFragment :
    BaseFragment<FragmentUserFollowDetailsBinding>(R.layout.fragment_user_follow_details) {
    lateinit var controller: NavController

    lateinit var uid: String

//
//    @Composable
//    fun heightView( hr_val:String) {
//        Column(
//            modifier = Modifier.border(1.dp, color = Color.Cyan, shape = RoundedCornerShape(9.dp)),
//            Arrangement.SpaceBetween,
//            Alignment.CenterHorizontally
//        ) {
//            Text(
//                text = "hr"
//
//            )
//            Spacer(modifier = Modifier.height(4.dp))
//
//            Text(text = hr_val)
//
//        }
//    }


    @Inject
    lateinit var imageLoader: RequestManager

    override fun onInitialized() {
        super.onInitialized()
        controller = Navigation.findNavController(requireView())
        uid = requireArguments().getString(UID).toString()

        val transition = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        sharedElementEnterTransition = transition



    }

    override fun setUpClicks() {
        binding.hr.setOnClickListener {
            controller.navigate(R.id.action_userFollowDetailsFragment_to_userFollowEMRListFrgament , UserFollowEMRListFrgament.getInstance(uid).requireArguments())
        }
        binding.toolbar.setNavigationOnClickListener {
            controller.popBackStack()
        }
    }

    override fun addObservers() {
        super.addObservers()

        FirebaseFirestore.getInstance().collection(Variables.FireStoreUsersRoot).document(uid)
            .addSnapshotListener(object : EventListener<DocumentSnapshot> {

                override fun onEvent(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
                    if(value == null)
                        return
                    if (value.exists() && error == null) {
                        val userInfo = value.toObject(UserInfo::class.java)
                        if (!userInfo?.profileImgUri.isNullOrEmpty())
                            imageLoader.load(userInfo?.profileImgUri).circleCrop()
                                .into(binding.userImage)
                        else {
                            FirebaseStorage.getInstance().getReference(Variables.FireStoreUsersRoot)
                                .child(uid)
                                .child(Variables.UserProfilePic).downloadUrl.addOnSuccessListener {
                                    imageLoader.load(it).circleCrop()
                                        .into(binding.userImage)
                                }
                        }

                        binding.UserName.text = userInfo?.name
                        binding.EmailTextView.text = userInfo?.email
                        binding.phoneNumber.text = "phone number: ${userInfo?.phoneNumber}"
                        binding.bloodType.text = "blood type: ${userInfo?.BloodType?.split(" ")?.subList(0 , 1)?.joinToString { it }}"
                        binding.location.text = "addr: ${userInfo?.country}"
                        binding.gender.text = "gender: ${userInfo?.gender}"

                        binding.heightComposableView.setContent {
                            MaterialTheme {
                                Surface {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(18.dp)

                                    ) {
                                        Text(
                                            text = "height" , Modifier.fillMaxWidth(),
                                            color = colorResource(id = R.color.blue_A200),
                                            fontSize = 18.sp,
                                            fontStyle = FontStyle.Normal,
                                            fontWeight = Bold,
                                            fontFamily = FontFamily.Cursive
                                        )

                                        Spacer(modifier = Modifier.height(1.dp))

                                        Text(
                                            text = userInfo?.height.toString() , Modifier.fillMaxWidth(),
                                            color = colorResource(id = R.color.blue_A200),
                                            fontSize = 18.sp,
                                            fontStyle = FontStyle.Normal,
                                            fontWeight = Bold,
                                            fontFamily = FontFamily.Cursive
                                        )
                                    }
                                }
                            }
                        }
                        binding.weightComposableView.setContent {
                            MaterialTheme {
                                Surface {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(18.dp)

                                    ) {
                                        Text(
                                            text = "weight" , Modifier.fillMaxWidth(),
                                            color = colorResource(id = R.color.blue_A200),
                                            fontSize = 18.sp,
                                            fontStyle = FontStyle.Normal,
                                            fontWeight = Bold,
                                            fontFamily = FontFamily.Cursive
                                        )

                                        Spacer(modifier = Modifier.height(1.dp))

                                        Text(
                                            text = userInfo?.weight.toString() , Modifier.fillMaxWidth(),
                                            color = colorResource(id = R.color.blue_A200),
                                            fontSize = 18.sp,
                                            fontStyle = FontStyle.Normal,
                                            fontWeight = Bold,
                                            fontFamily = FontFamily.Cursive
                                        )
                                    }
                                }
                            }
                        }

                    }
                }

            })
    }

    companion object {
        fun getInstance(uid: String) = UserFollowDetailsFragment().apply {
            arguments = Bundle().apply {
                putString(UID, uid)
            }
        }
    }

}