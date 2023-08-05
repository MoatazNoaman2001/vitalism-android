package com.example.livenativerppg.models.chatPeopleList.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.livenativerppg.R
import com.example.livenativerppg.component.base.BaseFragment
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.FragmentChatListBinding
import com.example.livenativerppg.databinding.PeapleListRecycleItemBinding
import com.example.livenativerppg.models.chatPeopleList.data.viewModel.ChatPeopleListViewModel
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

private const val TAG = "ChatListFragment"

@AndroidEntryPoint
class ChatListFragment : BaseFragment<FragmentChatListBinding>(R.layout.fragment_chat_list) {

    private val chatViewModel: ChatPeopleListViewModel by viewModels()
    lateinit var controller: NavController
    lateinit var adapter:PeopleChatListAdapter

    override fun onInitialized() {
        super.onInitialized()
        controller = Navigation.findNavController(requireView())
        NavigationUI.setupWithNavController(binding.toolbar, controller)

        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24)
        binding.toolbar.setNavigationIconTint(R.color.blue_A200)

        adapter = PeopleChatListAdapter()
        binding.PeopleChatListRecycleView.adapter = adapter;

        chatViewModel.getAllSharedPeople()

        CoroutineScope(lifecycleScope.coroutineContext).launch {
            chatViewModel.peopleList.collect {
                Log.d(TAG, "onInitialized: size: ${it.size}")
                binding.errorLayout.isVisible = it.isEmpty() || chatViewModel.error != null
                binding.PeopleChatListRecycleView.isVisible = it.isNotEmpty() || chatViewModel.error == null

                binding.errorTextView.text = if (chatViewModel.error != null) chatViewModel.error!!.message else "no contacts to ask them"

                adapter.submitList(it)
                if (it.isNotEmpty())
                    Log.d(TAG, "onInitialized: index 0: ${it[0]}")
                if (chatViewModel.error != null){
                    Toast.makeText(requireContext() , chatViewModel.error!!.message  , Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "onInitialized: error: ${chatViewModel.error!!.message}")
                }
            }

        }
    }


    class PeopleChatListAdapter : androidx.recyclerview.widget.ListAdapter<UserInfo , PeopleChatListAdapter.ViewHolder>(object : DiffUtil.ItemCallback<UserInfo>(){
        override fun areItemsTheSame(oldItem: UserInfo, newItem: UserInfo): Boolean {
            return oldItem.uid.equals(newItem.uid)
        }

        override fun areContentsTheSame(oldItem: UserInfo, newItem: UserInfo): Boolean {
            return oldItem == newItem
        }

    }){
        class ViewHolder(val binding: PeapleListRecycleItemBinding) : RecyclerView.ViewHolder(binding.root){

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(PeapleListRecycleItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.UserName.text = getItem(position).name
            holder.binding.UserEmail.text = getItem(position).email


            holder.binding.root.setOnClickListener {
                try {
                    val action = ChatListFragmentDirections.actionChatListFragmentToChatActivity()
                        .apply { arguments.apply { putString("id", getItem(position).uid) } }
                    Navigation.findNavController(it).navigate(action)
                }catch (e :Exception){
                    Log.d(TAG, "onBindViewHolder: ${e.message}")
                }
            }

            FirebaseStorage.getInstance()
                .getReference(Variables.FireStoreUsersRoot)
                .child(getItem(position).uid)
                .child(Variables.UserProfilePic)
                .downloadUrl
                .addOnSuccessListener {
                    Glide.with(holder.binding.root.context)
                        .asBitmap()
                        .load(it)
                        .circleCrop()
                        .into(holder.binding.profileImg)
                }.addOnFailureListener {
                    Glide.with(holder.binding.root.context)
                        .load(R.drawable.img_person_blue)
                        .circleCrop()
                        .into(holder.binding.profileImg)
                }

        }



    }
    override fun setUpClicks() {


    }

}