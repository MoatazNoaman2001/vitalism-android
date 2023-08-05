package com.example.livenativerppg.component.base

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.livenativerppg.component.db.models.ConnectRequest
import com.example.livenativerppg.component.db.models.ConnectionType
import com.example.livenativerppg.component.db.models.Notification
import com.example.livenativerppg.component.di.MyConnectionsRef
import com.example.livenativerppg.component.di.MyFollowRef
import com.example.livenativerppg.component.di.MyRequestsRef
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


private const val TAG = "AcceptBroadCast"

@AndroidEntryPoint
class AcceptBroadCast : BroadcastReceiver() {

    lateinit var myConnectionsRef: CollectionReference
    lateinit var MyRequestRef: CollectionReference
    lateinit var myFollowRef: CollectionReference


    override fun onReceive(context: Context, intent: Intent) {
        val info = intent.getSerializableExtra("info") as UserInfo?
        val connectRequest = intent.getSerializableExtra("connect") as ConnectRequest?
        myConnectionsRef = FirebaseFirestore.getInstance()
            .collection(Variables.FireStoreUsersRoot)
            .document(FirebaseAuth.getInstance().currentUser?.uid!!)
            .collection(Variables.CONNECTIONS)
        MyRequestRef = FirebaseFirestore.getInstance()
            .collection(Variables.FireStoreUsersRoot)
            .document(FirebaseAuth.getInstance().currentUser?.uid!!)
            .collection(Variables.REQUEST)

        myFollowRef = FirebaseFirestore.getInstance()
            .collection(Variables.FireStoreUsersRoot)
            .document(FirebaseAuth.getInstance().currentUser?.uid!!)
            .collection(Variables.FOLLOWERS)

        if (connectRequest?.ConnectType.equals(ConnectionType.FOLLOW.name)) {
            myFollowRef.document(info?.uid!!).set(HashMap<String ,  Boolean>().apply { put(info.uid , true) }).addOnSuccessListener {
                connectRequest?.Accpeted = true
                connectRequest?.Readed = true
                connectRequest?.SenderId?.let { it1 ->
                    MyRequestRef.document(it1).set(connectRequest).addOnSuccessListener { command: Void? ->
                        Toast.makeText(context , "follow accepted" ,Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener { e: Exception? ->
                        run {
                            Log.d(TAG, "AcceptConnectionRequest: ${e?.message}")
                        }
                    }
                }
            }
        } else
            myConnectionsRef.document(info?.uid!!)
                .set(HashMap<String, String>().apply { put(info.uid, "accepted") })
                .addOnSuccessListener {
                    connectRequest?.Accpeted = true
                    connectRequest?.Readed = true
                    connectRequest?.SenderId?.let { it1 ->
                        MyRequestRef.document(it1).set(connectRequest)
                            .addOnSuccessListener { command: Void? ->

                            }.addOnFailureListener { e: Exception? ->
                                run {
                                    Log.d(TAG, "AcceptConnectionRequest: ${e?.message}")
                                }
                            }
                    }
                }.addOnFailureListener {
                    Log.d(TAG, "AcceptConnectionRequest: failed to add user cloud")
                }

    }
}