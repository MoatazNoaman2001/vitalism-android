package com.example.livenativerppg.models.follow.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.livenativerppg.R
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.PeapleFollowRecycleItemBinding
import com.example.livenativerppg.models.UserFollowedDetails.ui.UserFollowDetailsFragment
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.google.firebase.storage.FirebaseStorage

class FollowRecycleAdapter(val imageLoader: RequestManager) : ListAdapter<UserInfo , FollowRecycleAdapter.ViewHolder>(FollowItemDiff()) {

    inner class ViewHolder(val peopleFollowRecycleItemBinding: PeapleFollowRecycleItemBinding) : RecyclerView.ViewHolder(peopleFollowRecycleItemBinding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(PeapleFollowRecycleItemBinding.inflate(LayoutInflater.from(parent.context) , parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.peopleFollowRecycleItemBinding.nameTextView.text = getItem(position).name
        holder.peopleFollowRecycleItemBinding.emailTextView.text = getItem(position).email
        imageLoader.load(getItem(position).profileImgUri).into(holder.peopleFollowRecycleItemBinding.imagUser)

        FirebaseStorage.getInstance().getReference(Variables.FireStoreUsersRoot)
            .child(getItem(position).uid).child(Variables.UserProfilePic)
            .downloadUrl.addOnSuccessListener {
                imageLoader.load(it).circleCrop().into(holder.peopleFollowRecycleItemBinding.imagUser)
            }
        holder.peopleFollowRecycleItemBinding.root.setOnClickListener {
            val extras = FragmentNavigatorExtras(
                holder.peopleFollowRecycleItemBinding.imagUser.apply {
                    transitionName = "Uimage_$position"
                } to "BImg"
            ,    holder.peopleFollowRecycleItemBinding.nameTextView.apply {
                    transitionName = "Uname_$position"
                } to "Bname"
            ,holder.peopleFollowRecycleItemBinding.emailTextView.apply {
                    transitionName = "Uemail_$position"
                } to "Bemail"

            )
            Navigation.findNavController(it).navigate(R.id.action_peopleIFollowFragment_to_userFollowDetailsFragment , UserFollowDetailsFragment.getInstance(getItem(position).uid).requireArguments(), null , extras)
        }
    }
}

class FollowItemDiff : DiffUtil.ItemCallback<UserInfo>() {
    override fun areItemsTheSame(oldItem: UserInfo, newItem: UserInfo): Boolean {
        return oldItem.email.equals(newItem.email)
    }

    override fun areContentsTheSame(oldItem: UserInfo, newItem: UserInfo): Boolean {
        return true
    }

}