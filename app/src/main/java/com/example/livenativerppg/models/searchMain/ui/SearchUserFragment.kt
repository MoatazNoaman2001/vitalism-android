package com.example.livenativerppg.models.searchMain.ui

import android.util.Log
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.paging.LoadState
import androidx.paging.PagingData
import com.bumptech.glide.RequestManager
import com.example.livenativerppg.R
import com.example.livenativerppg.component.base.BaseFragment
import com.example.livenativerppg.component.db.models.ConnectRequest
import com.example.livenativerppg.databinding.FragmentSearchUserBinding
import com.example.livenativerppg.models.searchMain.data.viewmodel.SearchVM
import com.example.livenativerppg.models.searchMain.ui.SearchResultRecycleViewAdapter.UserClickInterface
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named

private const val TAG = "SearchUserFragment"

@AndroidEntryPoint
class SearchUserFragment : BaseFragment<FragmentSearchUserBinding>(R.layout.fragment_search_user),
    SearchInterface, UserClickInterface {

    lateinit var searchVM: SearchVM
    lateinit var adapter: SearchResultRecycleViewAdapter
//    lateinit var controller:NavController

    @Inject
    lateinit var imageLoader: RequestManager


    @Inject
    @Named("requests")
    lateinit var myRequestsRef: CollectionReference
    @Inject
    @Named("followers")
    lateinit var myFollowersRef: CollectionReference


    override fun onInitialized() {
        super.onInitialized()
//        controller = Navigation.findNavController(requireView())
//        Log.d(
//            TAG,
//            "onInitialized: ${controller.backQueue.map { it.id + it.destination.label }.joinToString { it }}"
//        )
        searchVM = ViewModelProvider(requireActivity())[SearchVM::class.java]
        binding.searchUserVM = searchVM
        Log.d(TAG, "onInitialized: $myRequestsRef")
        adapter = SearchResultRecycleViewAdapter(imageLoader,myFollowersRef,myRequestsRef ,   ArrayList(), this@SearchUserFragment)
        binding.searchResultRecycleView.adapter = adapter

        myRequestsRef.addSnapshotListener { value, _ ->
            if (value != null && !value.isEmpty) {
                val ids = value.documents.map { it.id }.toList()
                adapter.setMyFollowIds(ids)
            }
        }


    }

    override fun setUpClicks() {

    }

    override fun addObservers() {
        super.addObservers()
        searchVM.isSearching.observe(viewLifecycleOwner) {
            if (!it && this::adapter.isInitialized) {
                adapter.submitData(lifecycle, PagingData.empty())
            }
        }
        searchVM.users.observe(viewLifecycleOwner) {
            Log.d(TAG, "addObservers: observer called in SearchUserFragment")

            binding.searchResultRecycleView.isVisible = true
            if (this::adapter.isInitialized) {
                adapter.submitData(lifecycle, it)
                adapter.addLoadStateListener {
                    when (it.append) {
                        is LoadState.Loading -> {
                            binding.demoWindow.isVisible = true
                            binding.DemoWindowTextView.text = "Currently Loading"
                            binding.lottieIcon.setAnimation(R.raw.search_peaple)
                        }
                        is LoadState.NotLoading -> {
                            adapter.addOnPagesUpdatedListener {
                                if (adapter.itemCount == 0) {
                                    binding.demoWindow.isVisible = true
                                    binding.DemoWindowTextView.text =
                                        "there is no available, check internet connection or try something else"
                                    binding.lottieIcon.setAnimation(R.raw.search_peaple)
                                } else binding.demoWindow.isVisible = false
                            }
                        }
                        is LoadState.Error -> {
                            Log.d(
                                TAG,
                                "onTextChanged: ${(it.append as LoadState.Error).error.message}"
                            )
                            if ((it.append as LoadState.Error).error.message?.trim() != "List is empty.") {
                                binding.demoWindow.isVisible = true
                                binding.DemoWindowTextView.text =
                                    "error happened ${(it.append as LoadState.Error).error.message}"
                                binding.lottieIcon.setAnimation(R.raw.error_while_search)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun clickListener() {

    }

    override fun searchListener(searchText: String?) {

    }

    override fun OnClick(
        info: UserInfo?,
        connectBtn: MaterialButton,
        rejectBtn: MaterialButton,
        connectionSentProgress: CircularProgressIndicator,
        event: Int,
        connectRequest: ConnectRequest?,
    ) {
        when (event) {
            8 -> searchVM.sendConnectRequest(info!!, connectBtn, connectionSentProgress).thenApply {
                Toast.makeText(requireContext(), "connection Request sent", Toast.LENGTH_SHORT)
                    .show()
                connectBtn.isVisible = true
                connectionSentProgress.isVisible = false
                connectBtn.setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.white,
                        requireActivity().theme
                    )
                )
                connectBtn.setText("request sent")
            }
            2 -> searchVM.AcceptRequest(info!!, connectRequest = connectRequest!!, connectBtn, connectionSentProgress)
            10 -> {
                searchVM.FollowMedical(info!!, connectBtn, connectionSentProgress)
            }
            5 -> {
                searchVM.RejectRequest(info!!)
            }
            11 -> {
                searchVM.UnFollowMedical(info!!, rejectBtn, connectBtn, connectionSentProgress)
            }
            12 -> {
                searchVM.CancelFollowMedical(info!!, rejectBtn, connectBtn, connectionSentProgress)
            }
            else -> {
                searchVM.CancleRequest(info!!, connectBtn, connectionSentProgress)
            }
        }
    }


}