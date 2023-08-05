package com.example.livenativerppg.models.MainChatInterface.ui

import android.app.KeyguardManager
import android.content.Intent
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.livenativerppg.models.MainChatInterface.data.MainChatRepoBuilderResult
import com.example.livenativerppg.models.MainChatInterface.data.viewModel.MainChatViewModel
import com.example.livenativerppg.R
import com.example.livenativerppg.commons.checkUserActiveById
import com.example.livenativerppg.commons.makeToast
import com.example.livenativerppg.component.base.BaseFragment
import com.example.livenativerppg.databinding.FragmentMainChatBinding
import com.example.livenativerppg.models.MainChatInterface.data.MainChatRepoMessageSentResult
import com.example.livenativerppg.models.MainChatInterface.data.model.Message
import com.example.livenativerppg.models.MainChatInterface.data.model.MessageState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.Calendar

private const val PARTNER_ID = "id"
private const val TAG = "MainChatFragment"

@AndroidEntryPoint
class MainChatFragment : BaseFragment<FragmentMainChatBinding>(R.layout.fragment_main_chat) {
    private val chatViewModel by viewModels<MainChatViewModel>()
    lateinit var partnerID: String
    lateinit var user: FirebaseUser
    lateinit var adapter: ChatRecycleViewAdapter

    override fun onInitialized() {
        super.onInitialized()
        partnerID = requireArguments().getString(PARTNER_ID) as String
        user = FirebaseAuth.getInstance().currentUser!!
        adapter = ChatRecycleViewAdapter()

        binding.chatMsgListRecycleView.adapter = adapter
        CoroutineScope(lifecycleScope.coroutineContext).launch {

            val getBuilderResult = chatViewModel.getChatBuilder(partnerID)
            if (getBuilderResult != null) {
                chatViewModel.initRealTimeMessaging(getBuilderResult)
            } else {
                when (val res = chatViewModel.CreateChatBuilder(partnerID)) {
                    is MainChatRepoBuilderResult.OnSuccess -> {
                        Log.d(TAG, "onInitialized: builder created ${res.Builder}")
                        binding.chatMsgListRecycleView.scrollToPosition(adapter.itemCount)
                    }
                    is MainChatRepoBuilderResult.OnError -> {
                        Log.d(TAG, "onInitialized: ${res.error.message}")
                    }
                }
            }
        }

    }

    override fun setUpClicks() {
        binding.msgInputTextInputLayout.setEndIconOnClickListener {
            if (binding.msgInputTextInputLayout.editText?.text?.isNotEmpty() == true) {
                CoroutineScope(Dispatchers.IO).apply {
                    launch {
                        when (val res = chatViewModel.sendTextMessage(
                            Message(
                                Calendar.getInstance().time,
                                user.uid,
                                partnerID,
                                MessageState.WAIT.name,
                                binding.msgInputTextInputLayout.editText?.text.toString().also {
                                    requireActivity().runOnUiThread {
                                        binding.msgInputTextInputLayout.editText?.text?.clear()
                                    }
                                }

                            )
                        )) {
                            is MainChatRepoMessageSentResult.OnSuccess -> {
                                requireActivity().runOnUiThread {
                                    makeToast(requireContext(), "sending message succeeded")
                                }
                            }
                            is MainChatRepoMessageSentResult.OnError -> {
                                Log.d(TAG, "setUpClicks: error sending message ${res.error}")
                                requireActivity().runOnUiThread {
                                    makeToast(requireContext(), "error sending message")
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    override fun addObservers() {
        super.addObservers()
        CoroutineScope(Dispatchers.Main).launch {
            chatViewModel.messageList.collect {

                binding.chatMsgListRecycleView.isVisible = chatViewModel.error_d == null && it.isNotEmpty()
                binding.errorLayout.isVisible = chatViewModel.error_d != null && it.isEmpty()
                binding.ErrorText.text = if (chatViewModel.error_d != null) chatViewModel.error_d?.message else if (it.isEmpty()) "not previous messages " else ""

                adapter.submitList(it.sortedBy { it.date })
                binding.chatMsgListRecycleView.scrollToPosition(it.size)

                Log.d(TAG, "addObservers: received messages ${it.map { it.Text }}")
            }
        }
    }

    companion object {
        fun getInstance(id: String) = MainChatFragment().apply {
            arguments = Bundle().apply {
                putString(PARTNER_ID, id)
            }
        }
    }

}