package com.example.livenativerppg.models.setting.ui

import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.example.livenativerppg.R
import com.example.livenativerppg.component.base.BaseFragment
import com.example.livenativerppg.databinding.FragmentProfileSettingBinding
import com.example.livenativerppg.databinding.SettingRecycleviewItemBinding
import com.example.livenativerppg.models.startActivity.profilePicSelection.ui.ImagePicSelectManger
import com.example.livenativerppg.models.startActivity.ui.StartActivity
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class ProfileSettingFragment :
    BaseFragment<FragmentProfileSettingBinding>(R.layout.fragment_profile_setting) {
    lateinit var controller: NavController
    lateinit var auth: FirebaseAuth
    lateinit var adapter: SettingRecycleAdapter

    lateinit var client: GoogleSignInClient
    lateinit var gso: GoogleSignInOptions
    lateinit var user:FirebaseUser
    
    override fun onInitialized() {
        super.onInitialized()
        controller = Navigation.findNavController(requireView())
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        client = GoogleSignIn.getClient(requireActivity(), gso)

        adapter =
            SettingRecycleAdapter(ArrayList<String>().apply { add("Active"); add("Show me in Search"); })
        val transation =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        sharedElementEnterTransition = transation
        binding.SettingMainRecycleView.adapter = adapter
        binding.toolbar.menu.getItem(0).icon = tintDrawable(binding.toolbar.menu.getItem(0).icon!!)
    }

    override fun setUpClicks() {

        binding.toolbar.setNavigationOnClickListener {
            controller.popBackStack()
        }
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.signoutBtn){
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
            true
        }
    }

    fun tintDrawable(drawable: Drawable): Drawable {
        val drawable_new = DrawableCompat.wrap(drawable)
        DrawableCompat.setTint(
            drawable_new,
            ResourcesCompat.getColor(resources, R.color.blue_A200, requireActivity().theme)
        )
        return drawable_new
    }

    inner class SettingRecycleAdapter(var setting_attrs: ArrayList<String>) :
        RecyclerView.Adapter<SettingRecycleAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: SettingRecycleviewItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): SettingRecycleAdapter.ViewHolder {
            return ViewHolder(
                SettingRecycleviewItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: SettingRecycleAdapter.ViewHolder, position: Int) {
            holder.binding.checkbox.text = setting_attrs[position]

            holder.binding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                when (position) {
                    0 -> if (isChecked) {
                        Toast.makeText(
                            requireContext(),
                            "you are now active to all your followers",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "you no hidden",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    1 -> if (isChecked) {
                        Toast.makeText(
                            requireContext(),
                            "you now appear to every one in search",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "you are hidden in search now",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
        }

        override fun getItemCount(): Int {
            return setting_attrs.size
        }

    }
}