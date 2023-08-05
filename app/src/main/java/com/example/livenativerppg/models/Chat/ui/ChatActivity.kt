package com.example.livenativerppg.models.Chat.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.livenativerppg.R
import com.example.livenativerppg.commons.accountActivations
import com.example.livenativerppg.commons.checkUserActiveById
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.ActivityChatBinding
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.google.firebase.firestore.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.tasks.await

private const val TAG = "ChatActivity"

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {
    lateinit var controler: NavController
    lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var navHostFragment: NavHostFragment
    lateinit var binding: ActivityChatBinding

    private lateinit var partnerID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        partnerID = intent.extras!!.getString("id", "")
        Log.d(TAG, "onCreate: $partnerID")

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        controler = navHostFragment.navController

        controler.setGraph(R.navigation.chat_nav_graph, intent.extras)

        appBarConfiguration = AppBarConfiguration(controler.graph)

        NavigationUI.setupWithNavController(binding.materialToolBar, controler, appBarConfiguration)

        binding.materialToolBar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24)
        binding.materialToolBar.setNavigationIconTint(resources.getColor(R.color.blue_A200))

        accountActivations(partnerID).addSnapshotListener { value, error ->
            if (error == null && value!!.exists()) {
                if (value.getBoolean(Variables.isUserActive)!!)
                    binding.materialToolBar.subtitle = "active"
                else
                    binding.materialToolBar.subtitle = ""
            }
        }

        CoroutineScope(newSingleThreadContext("partnerThread")).launch {
            val user = FirebaseFirestore.getInstance().collection(Variables.FireStoreUsersRoot)
                .document(partnerID)
                .get()
                .await()
                .toObject(UserInfo::class.java)

            launch {
                val value = checkUserActiveById(partnerID)
                if (value) {
                    MainScope().launch {
                        binding.materialToolBar.subtitle = "active"
                    }
                }
            }

            MainScope().launch {
                binding.materialToolBar.title = user?.name
                binding.materialToolBar.setSubtitleTextColor(
                    ResourcesCompat.getColor(resources, R.color.gray_400, theme)
                )
            }
        }
    }

}