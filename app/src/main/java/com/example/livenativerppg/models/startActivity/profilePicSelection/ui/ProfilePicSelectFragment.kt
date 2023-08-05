package com.example.livenativerppg.models.startActivity.profilePicSelection.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.livenativerppg.R
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.FragmentProfilePicSelectBinding
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream


private val INFO = "info"
private const val TAG = "ProfilePicSelectFragment"

class ProfilePicSelectFragment : Fragment() {


    lateinit var binding: FragmentProfilePicSelectBinding
    lateinit var controller: NavController
    lateinit var picker: ActivityResultLauncher<PickVisualMediaRequest>
    lateinit var uploader: ImagePicSelectManger
    lateinit var auth: FirebaseAuth
    lateinit var user: FirebaseUser

    private var info: UserInfo? = null
    var imageUri: String? = null

    companion object {
        @JvmStatic
        fun getInstance(info: UserInfo) = ProfilePicSelectFragment().apply {
            arguments = Bundle().apply {
                putSerializable(INFO, info)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(arguments != null){
            info = requireArguments().getSerializable(INFO) as UserInfo
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProfilePicSelectBinding.inflate(layoutInflater)
        uploader = ImagePicSelectManger()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller = Navigation.findNavController(requireView())
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        info = requireArguments().getSerializable(INFO) as UserInfo?


        picker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            if (it != null) {
                Log.d(TAG, "onViewCreated: $it")
                imageUri = it.toString()
                Glide.with(requireContext())
                    .asBitmap()
                    .load(it)
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
                                binding.imageView.setImageBitmap(resource)
                                binding.skipBtn.isVisible = false
                            }
                            val baos = ByteArrayOutputStream()
                            resource?.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                            uploader.UploadImage(it, baos.toByteArray())
                                .addOnProgressListener {
                                    val transfered = it.bytesTransferred
                                    val total = it.totalByteCount

                                    val progress = (transfered * 100) / total
                                    binding.circularProgressIndicator.setProgress(progress.toInt(),
                                        true)
                                }
                            return true
                        }
                    })
                    .submit()
                binding.imageView.invalidate()


            }
        }

        binding.skipBtn.setOnClickListener {
            if (controller.previousBackStackEntry?.destination?.id == R.id.signUpFragment) {
                controller.popBackStack()
                controller.popBackStack()
            } else {
                controller.navigate(R.id.action_profilePicSelectFragment_to_pagerStartInstructionFragment)
            }
        }
        binding.nextBtn.setOnClickListener {
            if (imageUri == null) {
                Toast.makeText(requireContext(),
                    "you should select an image",
                    Toast.LENGTH_SHORT).show()
            } else {
                if (uploader.isFinished) {
                    FirebaseStorage.getInstance().getReference(Variables.FireStoreUsersRoot)
                        .child(user.uid)
                        .child(Variables.UserProfilePic)
                        .downloadUrl.addOnSuccessListener {
                            user.updateProfile(userProfileChangeRequest {
                                photoUri = it
                                displayName = info?.name
                            })

                            FirebaseFirestore.getInstance().collection(Variables.FireStoreUsersRoot)
                                .document(FirebaseAuth.getInstance().currentUser?.uid.toString())
                                .set(info!!).addOnSuccessListener {
                                    controller.navigate(R.id.action_profilePicSelectFragment_to_pagerStartInstructionFragment)
                                }

                        }
                }
            }
        }
        binding.uploadBtn.setOnClickListener {
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

    }


}