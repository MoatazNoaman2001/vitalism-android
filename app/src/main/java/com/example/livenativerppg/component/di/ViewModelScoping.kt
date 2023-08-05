package com.example.livenativerppg.component.di

import com.example.livenativerppg.component.base.NotificationApi
import com.example.livenativerppg.component.utility.Variables
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(ViewModelComponent::class)
object ViewModelScoping {

    @Provides
    @ViewModelScoped
    @MyHrMeasurementsRef
    fun provideQueryRPPGResults(): Query =
        FirebaseDatabase.getInstance()
            .getReference(Variables.FireStoreUsersRoot)
            .child(FirebaseAuth.getInstance().uid!!)
            .child(Variables.MEASUREMENT)
            .child(Variables.HR)
            .orderByKey()
            .limitToFirst(Variables.EMR_PAGER_LIMIT)
    @Provides
    @ViewModelScoped
    @MyBPMeasurementsRef
    fun provideQueryBPRPPGResults(): Query =
        FirebaseDatabase.getInstance()
            .getReference(Variables.FireStoreUsersRoot)
            .child(FirebaseAuth.getInstance().uid!!)
            .child(Variables.MEASUREMENT)
            .child(Variables.BP)
            .orderByKey()
            .limitToFirst(Variables.EMR_PAGER_LIMIT)

    @Provides
    @ViewModelScoped
    fun provideSearchUsers(): com.google.firebase.firestore.Query {
        return FirebaseFirestore.getInstance()
            .collection(Variables.FireStoreUsersRoot)
            .orderBy("name")
            .limit(Variables.SEARCH_PAGER_LIMIT.toLong())
    }

    @Provides
    @ViewModelScoped
    @NotificationRetrofitBuilder
    fun NotiRetro(): Retrofit = Retrofit.Builder()
        .baseUrl(NotificationApi.baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @ViewModelScoped
    fun getNotificationApi(@NotificationRetrofitBuilder retrofit: Retrofit) =
        retrofit.create(NotificationApi::class.java)


    @Provides
    @ViewModelScoped
//    @Named("requests")
    @MyRequestsRef
    fun getMyRequestsRef() =
        FirebaseFirestore.getInstance()
            .collection(Variables.FireStoreUsersRoot)
            .document(FirebaseAuth.getInstance().currentUser?.uid!!)
            .collection(Variables.REQUEST)

    @Provides
    @ViewModelScoped
    @MyChatRef
    fun getMyChatRef() =
        FirebaseFirestore.getInstance()
            .collection(Variables.FireStoreUsersRoot)
            .document(FirebaseAuth.getInstance().currentUser?.uid!!)
            .collection(Variables.CHAT)

    @Provides
    @ViewModelScoped
    @MyConnectionsRef
    fun getMyConnectionsRef() =
        FirebaseFirestore.getInstance()
            .collection(Variables.FireStoreUsersRoot)
            .document(FirebaseAuth.getInstance().currentUser?.uid!!)
            .collection(Variables.CONNECTIONS)

    @Provides
    @ViewModelScoped
    @MyFollowRef
    fun getMyFollowRef() =
        FirebaseFirestore.getInstance()
            .collection(Variables.FireStoreUsersRoot)
            .document(FirebaseAuth.getInstance().currentUser?.uid!!)
            .collection(Variables.FOLLOWERS)


    @Provides
    @ViewModelScoped
    @MyRealTimeChatDB
    fun getMyRealTime() = FirebaseDatabase.getInstance().getReference(Variables.FireStoreUsersRoot)
        .child(Variables.CHAT)

    @Provides
    @ViewModelScoped
    fun getMainUserInfo(): DocumentReference {
        return FirebaseFirestore.getInstance()
            .collection(Variables.FireStoreUsersRoot)
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
    }
}