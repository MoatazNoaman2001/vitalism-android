package com.example.livenativerppg.models.mainAppPage.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import com.example.livenativerppg.R
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.ActivityMainAppPageBinding
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.facebook.AccessToken
import com.facebook.AccessTokenTracker
import com.facebook.CallbackManager
import com.facebook.GraphRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonArray
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.json.JSONArray




private const val TAG = "MainAppPageActivity"

@AndroidEntryPoint
class MainAppPageActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainAppPageBinding

    lateinit var controller: NavController
    lateinit var configuration: AppBarConfiguration
    lateinit var callbackManager: CallbackManager
    lateinit var accessTokenTracker: AccessTokenTracker

    @Inject
    lateinit var sharedPreferences: SharedPreferences


    lateinit var myDocRef:DocumentReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainAppPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        binding.bottomNavBar.background = null
        controller = (supportFragmentManager.findFragmentById(R.id.fragmentContainerView2) as NavHostFragment).navController
        configuration = AppBarConfiguration(controller.graph)

        NavigationUI.setupWithNavController(binding.bottomNavBar, controller)
        // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val flags = SYSTEM_UI_FLAG_LAYOUT_STABLE or
                SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                SYSTEM_UI_FLAG_FULLSCREEN or SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.decorView.systemUiVisibility = flags
            val decorView = window.decorView
            decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                    decorView.systemUiVisibility = flags
                }
            }
        }

        myDocRef = FirebaseFirestore.getInstance()
            .collection(Variables.FireStoreUsersRoot)
            .document(FirebaseAuth.getInstance().currentUser?.uid!!)
        myDocRef.get().addOnSuccessListener {
            if (it.exists()){
                val info = it.toObject(UserInfo::class.java)
                if (info?.token.isNullOrEmpty()) {
                    FirebaseMessaging.getInstance().token.addOnSuccessListener {
                        info?.token = it
                        myDocRef.set(info!!)
                    }
                }
            }
        }

        callbackManager = CallbackManager.Factory.create()
        accessTokenTracker = object : AccessTokenTracker(){
            override fun onCurrentAccessTokenChanged(
                oldAccessToken: AccessToken?,
                currentAccessToken: AccessToken?,
            ) {
                val token = currentAccessToken?.token
                sharedPreferences.edit().putString("fbToken" , token).apply()
            }
        }
        if (sharedPreferences.getString("email" , null) == null){
            val request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken()){jsonobject, responce->
                val email = jsonobject?.getString("email").toString()
                Log.d(TAG, "onCreate: fb email: ${email}")
                run {
                    if (responce?.error != null) {
                        Log.d(TAG, "onCreate: error happen in get user info , ${responce.error!!.errorMessage}")

                    } else {
                        val email = jsonobject?.getString("email").toString()
                        Log.d(TAG, "onCreate: fb email: ${email}")
//                        Toast.makeText(this@MainAppPageActivity , email, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            request.parameters = Bundle().apply {
                putString("fields" , "email")
            }
            request.executeAsync();
        }else{
            Log.d(TAG, "onCreate: current email: ${sharedPreferences.getString("email", null)}")
        }
        val request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken()){jsonobject, responce->
            run {
                if (responce?.error != null) {
                    Log.d(TAG, "onCreate: error happen in get user info , ${responce.error!!.errorMessage}")

                } else {
                    val email = jsonobject?.getString("email").toString()
                    Log.d(TAG, "onCreate: fb email: ${email}")

                    Toast.makeText(this@MainAppPageActivity , email, Toast.LENGTH_SHORT).show()
                    sharedPreferences.edit().putString("email", email)
                }
            }
        }
        request.parameters = Bundle().apply {
            putString("fields" , "email")
        }
        request.executeAsync();
        val friend_request =  GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken()){ jsonObject , response->
            run {
                if (response != null) {
                    if (response.error != null) {
                        val friendList = response.jsonObject?.getJSONArray("data")
                        Toast.makeText(
                            this@MainAppPageActivity,
                            "friends retrieved",
                            Toast.LENGTH_SHORT
                        ).show()
                    }else{
                        Log.d(TAG, "onCreate: error happen while retrieve friend list")
                    }
                }
            }
        }
        friend_request.executeAsync()

        binding.bottomNavBar.setOnItemSelectedListener {
            Log.d(TAG, "onCreate: " + it.itemId)
            if (it.itemId != controller.currentDestination?.id) {
                NavigationUI.onNavDestinationSelected(it, controller)
            }
            true
        }

        binding.searchFapBtn.setOnClickListener {
            Log.d(TAG,
                "onCreate: fab current fragment label: ${controller.currentDestination?.label}")
            when (controller.currentDestination?.id) {
                R.id.homeFragment -> controller.navigate(R.id.action_homeFragment_to_searchMainFragment)
                R.id.scheduleFragment -> controller.navigate(R.id.action_scheduleFragment_to_searchMainFragment)
                R.id.profileFragment -> controller.navigate(R.id.action_profileFragment_to_searchMainFragment)
            }
        }


        controller.addOnDestinationChangedListener { controller, destination, arguments ->
            Log.d(TAG, "onCreate: " + destination.id)
            val ids = arrayOf(R.id.scheduleFragment,
                R.id.homeFragment,
                R.id.profileFragment,
                R.id.chatListFragment)
            if (!ids.contains(destination.id)) {
                binding.bottomNavBar.visibility = GONE
                binding.searchFapBtn.visibility = GONE
                binding.bottomBar.visibility = GONE
                binding.rootContainerView.layoutParams = (binding.rootContainerView.layoutParams as CoordinatorLayout.LayoutParams).apply {
                    width = MATCH_PARENT
                    height = MATCH_PARENT
                    bottomMargin = 0
                }
            } else {
                binding.rootContainerView.layoutParams = (binding.rootContainerView.layoutParams as CoordinatorLayout.LayoutParams).apply {
                    width = MATCH_PARENT
                    height = MATCH_PARENT
                    bottomMargin = 100
                }
                binding.bottomNavBar.visibility = VISIBLE
                binding.searchFapBtn.visibility = VISIBLE
                binding.bottomBar.visibility = VISIBLE
            }
        }


    }

    companion object {
        init {
            System.loadLibrary("livenativerppg")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode , resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        accessTokenTracker.stopTracking()
    }
    override fun onSupportNavigateUp(): Boolean {
        return controller.navigateUp(configuration) || super.onSupportNavigateUp()
    }
}