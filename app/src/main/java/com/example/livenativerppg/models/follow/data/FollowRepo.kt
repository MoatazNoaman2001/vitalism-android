package com.example.livenativerppg.models.follow.data

import com.example.livenativerppg.component.db.AppDatabase
import com.example.livenativerppg.component.di.MyConnectionsRef
import com.example.livenativerppg.component.di.MyFollowRef
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.component.utility.networkBoundResource
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Named

class FollowRepo @Inject constructor(
    @MyConnectionsRef val myConnectionsRef: CollectionReference,
    @MyFollowRef val myFollowRef: CollectionReference,
    appDatabase: AppDatabase,
) {


    suspend fun connections() = myConnectionsRef.get().await().documents.map { it.id }.map {
        FirebaseFirestore.getInstance().collection(Variables.FireStoreUsersRoot).document(it).get()
            .await().toObject(UserInfo::class.java)
    }.toList()

    suspend fun followers() =  myFollowRef.get().await().documents.map { it.id }.map {
        FirebaseFirestore.getInstance().collection(Variables.FireStoreUsersRoot).document(it).get()
            .await().toObject(UserInfo::class.java)
    }.toList()


}