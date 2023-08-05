package com.example.livenativerppg.models.UserFollowedDetails.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.paging.LoadState
import com.bumptech.glide.RequestManager
import com.example.livenativerppg.R
import com.example.livenativerppg.component.base.BaseFragment
import com.example.livenativerppg.component.natives.RPPGResult
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.FragmentUserFollowEMRListFrgamentBinding
import com.example.livenativerppg.models.emr_EMR.ui.DateStringEMRRecycleAdapter
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.apache.commons.lang3.tuple.ImmutablePair
import javax.inject.Inject

private const val UID = "uid"

private const val TAG = "UserFollowEMRListFrgame"

@AndroidEntryPoint
class UserFollowEMRListFrgament : BaseFragment<FragmentUserFollowEMRListFrgamentBinding>(R.layout.fragment_user_follow_e_m_r_list_frgament) {

    @Inject
    lateinit var imageLoader: RequestManager
    lateinit var controller: NavController
    lateinit var adapter: DateStringEMRRecycleListAdapter

    lateinit var uid: String


    override fun onInitialized() {
        super.onInitialized()
        controller = Navigation.findNavController(requireView())
        uid = requireArguments().getString(UID).toString()
        adapter = DateStringEMRRecycleListAdapter(null);
        CoroutineScope(lifecycleScope.coroutineContext).launch {
            binding.toolbar.title = "${
                FirebaseFirestore.getInstance().collection(Variables.FireStoreUsersRoot)
                    .document(uid).get().await().toObject(UserInfo::class.java)?.name
            } Emr"
        }

        binding.recycleView.adapter = adapter
    }

    override fun setUpClicks() {
        binding.toolbar.setNavigationOnClickListener {
            controller.popBackStack()
        }
    }

    override fun addObservers() {
        super.addObservers()


        val results: ArrayList<ImmutablePair<String, ArrayList<ImmutablePair<String, ArrayList<RPPGResult>>>>> =
            ArrayList()
        FirebaseDatabase.getInstance()
            .getReference(Variables.FireStoreUsersRoot)
            .child(uid)
            .child(Variables.MEASUREMENT)
            .child(Variables.HR)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        results.add(
                            ImmutablePair(it.key, ArrayList<ImmutablePair<String, ArrayList<RPPGResult>>>().apply {
                                    for (child in it.children) {
                                        add(
                                            ImmutablePair<String, ArrayList<RPPGResult>>(child.key,
                                                ArrayList<RPPGResult>().apply {
                                                    for (child in child.children) {
                                                        if (child.key.toString() == "average")
                                                            add(RPPGResult(
                                                                0L,
                                                                child.value.toString().toDouble(),
                                                                child.value.toString().toDouble() -5 ,
                                                                child.value.toString().toDouble() +5,
                                                            ))
                                                        else{
                                                            try{
                                                                add(child.getValue(RPPGResult::class.java)!!)
                                                            }catch (e :Exception) {
                                                                try {
                                                                    val rppg = Gson().fromJson(
                                                                        child.value.toString(),
                                                                        RPPGResult::class.java
                                                                    )
                                                                    add(rppg)
                                                                }catch (_:Exception){

                                                                }
                                                            }
                                                        }
                                                    }
                                                })
                                        )
                                    }
                                })
                        )
                    }

                    Log.d(TAG, "addObservers: result: $results")
                    adapter.submitList(results)

//                    binding.emptylayout.isVisible = results.isEmpty()
                }

                override fun onCancelled(error: DatabaseError) {}

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