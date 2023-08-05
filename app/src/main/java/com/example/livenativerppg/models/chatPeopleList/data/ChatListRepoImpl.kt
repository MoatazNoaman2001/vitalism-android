package com.example.livenativerppg.models.chatPeopleList.data

import android.util.Log
import com.example.livenativerppg.component.db.models.ConnectRequest
import com.example.livenativerppg.component.db.models.ConnectionType
import com.example.livenativerppg.component.di.MyRealTimeChatDB
import com.example.livenativerppg.component.di.MyRequestsRef
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.*
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val TAG = "DocChatRepoImpl"

@ViewModelScoped
class ChatListRepoImpl @Inject constructor(
    @MyRealTimeChatDB val myRealTimeDB: DatabaseReference,
    @MyRequestsRef val myFollowRef: CollectionReference,
    val myProfile: DocumentReference,

) : ChatListRepoInterface {
    val docList = mutableListOf<UserInfo>()
    var getSharedDocList: ((List<UserInfo>) -> Unit)? = null
    var error_gen: ((FirebaseFirestoreException) -> Unit)? = null

    override fun getSharedFollowerList(
        success: (List<UserInfo>) -> Unit,
        error: ((FirebaseFirestoreException) -> Unit),
    ) {
        getSharedDocList = success
        error_gen = error
    }


    override fun loadSharedFollower() {
        val user = FirebaseAuth.getInstance().currentUser
        myFollowRef.addSnapshotListener { value, error ->
            if (error == null) {
                if (!value?.isEmpty!!) {
                    val ids = value.documents
                        .filter {
                            val req = it.toObject(ConnectRequest::class.java)
                            Log.d(TAG, "loadSharedDoctor: $req")
                            return@filter req?.Accpeted!! && req.ConnectType == ConnectionType.FOLLOW.name
                        }
                        .map { it.id }
                        .toList()

                    Log.d(TAG, "loadSharedDoctor: $ids")

                    CoroutineScope(Dispatchers.IO).launch {
                        val me  = myProfile.get().await().toObject(UserInfo::class.java)
                        if(ids.isNotEmpty()) {
                            FirebaseFirestore.getInstance().collection(Variables.FireStoreUsersRoot)
                                .orderBy("name")
                                .whereIn("uid", ids)
                                .whereEqualTo(
                                    "userType",
                                    if (me?.userType == UserType.Patient.name) UserType.Medical.name else UserType.Patient.name
                                )
                                .get()
                                .addOnSuccessListener {
                                    getSharedDocList?.invoke(it.documents.map { it.toObject(UserInfo::class.java)!! }
                                        .toList())
                                }.addOnFailureListener {
                                    error_gen?.invoke(it as FirebaseFirestoreException)
                                }
                        }
                    }

                }
            } else {
                error_gen?.invoke(error)
            }
        }
    }
}