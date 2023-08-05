package com.example.livenativerppg.component.di

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.livenativerppg.commons.updateUserIsActive
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.moatazsapplication.app.appcomponents.utility.PreferenceHelper
import dagger.hilt.android.HiltAndroidApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * The application class which used to start koin for dependency injection
 */
private const val TAG = "MyApp"

@HiltAndroidApp
class MyApp : Application() {

    public override fun onCreate(): Unit {
        super.onCreate()
        instance = this
        FirebaseApp.initializeApp(applicationContext)
        startKoin {
            androidLogger()
            androidContext(this@MyApp)
            loadKoinModules(getKoinModules())
        }
        val lifecycleEventObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    if (FirebaseAuth.getInstance().currentUser != null)
                        updateUserIsActive(false)
                    Log.d(TAG, "onAppOnForeGround: deActivated")
                }
                Lifecycle.Event.ON_START -> {
                    if (FirebaseAuth.getInstance().currentUser != null)
                        updateUserIsActive(true)
                    Log.d(TAG, "onAppOnForeGround: activated")
                }
                else -> {}
            }
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleEventObserver)


    }

    /**
     * method which prepares [PreferenceHelper]s koin module
     * @return [Module] - the koin module
     */
    private fun preferenceModule(): Module {
        val prefsModule = module {
            single {
                PreferenceHelper()
            }
        }
        return prefsModule
    }

    /**
     * method which returns the list of koin module to register
     * @return MutableList<Module> - list of koin modules
     */
    private fun getKoinModules(): MutableList<Module> {
        val koinModules = mutableListOf<Module>()
        koinModules.add(preferenceModule()) //register preference module
        return koinModules
    }

    public companion object {

        // the application instance
        private lateinit var instance: MyApp

        /**
         * method to get instance of application object
         */
        public fun getInstance(): MyApp = instance
    }
}