package com.example.livenativerppg.models.notification.ui

import android.app.Notification
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.bumptech.glide.RequestManager
import com.example.livenativerppg.R
import com.example.livenativerppg.component.base.BaseFragment
import com.example.livenativerppg.component.utility.Resource
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.FragmentNotificationBinding
import com.example.livenativerppg.databinding.NotificationRecycleViewItemBinding
import com.example.livenativerppg.models.notification.data.viewModel.NotificationVM
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


private const val TAG = "NotificationFragment"

@AndroidEntryPoint
class NotificationFragment :
    BaseFragment<FragmentNotificationBinding>(R.layout.fragment_notification) {
    lateinit var controller: NavController

    private val notificationVM by viewModels<NotificationVM>()
    lateinit var adapter: NotificationAdapter

    @Inject
    lateinit var imageLoader: RequestManager

    override fun onInitialized() {
        super.onInitialized()
        controller = Navigation.findNavController(requireView())


        val transition =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)

        sharedElementEnterTransition = transition
        sharedElementReturnTransition = transition
        adapter = NotificationAdapter(imageLoader)
        binding.notificationList.adapter = adapter

        notificationVM.notification.observe(viewLifecycleOwner) {
            Log.d(TAG, "onInitialized: ${it.data?.size}")
            Log.d(TAG, "onInitialized: ${it.data?.map { it.connRequest.SenderId }?.joinToString { it }}")

            binding.emptyLayoutNotification.isVisible =
                it is Resource.Failed && it.data.isNullOrEmpty()
            binding.notificationList.isVisible = !binding.emptyLayoutNotification.isVisible

            if (binding.emptyLayoutNotification.isVisible) {
                binding.errorTextView.text = it.error?.message
            } else {
                adapter.submitList(it.data!!)
            }
        }

    }

    override fun setUpClicks() {
        binding.toolbar.setNavigationOnClickListener {
            controller.popBackStack()
        }
    }

    override fun addObservers() {
        super.addObservers()
    }

    inner class NotificationAdapter(val imageLoader: RequestManager) : ListAdapter<com.example.livenativerppg.component.db.models.Notification, NotificationAdapter.ViewHolder>(
            notificationItemDiffUtil()
        ) {

        inner class ViewHolder(val binding: NotificationRecycleViewItemBinding) :
            RecyclerView.ViewHolder(binding.root) {}

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): NotificationAdapter.ViewHolder {
            return ViewHolder(
                NotificationRecycleViewItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: NotificationAdapter.ViewHolder, position: Int) {
            Log.d(TAG, "onBindViewHolder: sender id: ${getItem(position).connRequest.SenderId}")
            FirebaseFirestore.getInstance()
                .collection(Variables.FireStoreUsersRoot)
                .document(getItem(position).connRequest.SenderId)
                .get()
                .addOnSuccessListener {
                    if (it.exists())
                        holder.binding.TopicName.text = it.toObject(UserInfo::class.java)?.name
                }
            holder.binding.NotificationBody.text = "New Notification"

            FirebaseStorage.getInstance().getReference(Variables.FireStoreUsersRoot)
                .child(getItem(position).connRequest.SenderId)
                .child(Variables.UserProfilePic)
                .downloadUrl
                .addOnSuccessListener {
                    imageLoader.load(it).circleCrop().into(holder.binding.applicationIcon)
                }.addOnFailureListener {
                    imageLoader.load(R.drawable.img_person_blue).circleCrop()
                        .into(holder.binding.applicationIcon)
                }

            if (getItem(position).connRequest.Accpeted!!) {
                holder.binding.NotificationBody.text = "connection accepted"
            } else
                holder.binding.root.setOnClickListener {
                    val extras = FragmentNavigatorExtras(
                        holder.binding.applicationIcon.apply {
                            transitionName = transitionName + "_" + position
                        } to "userImg_big",
                        holder.binding.TopicName.apply {
                            transitionName = transitionName + "_" + position
                        } to "name_full"
                    )

                    try {
                        controller.navigate(
                            R.id.action_notificationFragment_to_notificationDetailsFragment,
                            NotificationDetailsFragment.newInstance(getItem(position))
                                .requireArguments(),
                            null,
                            extras
                        )
                    } catch (e: Exception) {
                    }
                }
        }


    }

    inner class notificationItemDiffUtil :
        DiffUtil.ItemCallback<com.example.livenativerppg.component.db.models.Notification>() {
        override fun areItemsTheSame(
            oldItem: com.example.livenativerppg.component.db.models.Notification,
            newItem: com.example.livenativerppg.component.db.models.Notification,
        ): Boolean {
            return oldItem.connRequest.RequestDate.equals(newItem.connRequest.RequestDate)
        }

        override fun areContentsTheSame(
            oldItem: com.example.livenativerppg.component.db.models.Notification,
            newItem: com.example.livenativerppg.component.db.models.Notification,
        ): Boolean {
            return true
        }

    }
}