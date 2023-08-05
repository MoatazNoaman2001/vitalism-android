package com.example.livenativerppg.models.searchMain.data

import android.util.Log
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.liveData
import com.example.livenativerppg.R
import com.example.livenativerppg.commons.getPartnerFollowerToMeInstance
import com.example.livenativerppg.commons.getPartnerRequestToMeInstance
import com.example.livenativerppg.component.base.NotificationApi
import com.example.livenativerppg.component.db.AppDatabase
import com.example.livenativerppg.component.db.models.ConnectRequest
import com.example.livenativerppg.component.db.models.ConnectionType
import com.example.livenativerppg.component.db.models.Notification
import com.example.livenativerppg.component.db.models.NotificationConnectionRequest
import com.example.livenativerppg.component.di.MyConnectionsRef
import com.example.livenativerppg.component.di.MyFollowRef
import com.example.livenativerppg.component.di.MyHrMeasurementsRef
import com.example.livenativerppg.component.di.MyRequestsRef
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.models.searchMain.data.network.FirebaseSearchEmr
import com.example.livenativerppg.models.searchMain.data.network.FirebaseSearchUsersPager
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.scopes.ViewModelScoped
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap
import kotlin.math.pow

private const val TAG = "SearchRepository"

@ViewModelScoped
class SearchRepository @Inject constructor(
    val querySearchUser: Query,
    @MyHrMeasurementsRef val querySearchEmr: com.google.firebase.database.Query,
    val notificationApi: NotificationApi,
    val appDatabase: AppDatabase,
    @MyRequestsRef val MyRequestRef: CollectionReference,
    @MyConnectionsRef val myConnectionsRef: CollectionReference,
    @MyFollowRef val myFollowRef: CollectionReference,
) {


    fun getSearchResults(query: String) =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                maxSize = 100,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { FirebaseSearchUsersPager(querySearchUser, query) }
        ).liveData

    fun getSearchEmrResult(query: String) =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                maxSize = (5 * 10.0.pow(3.0)).toInt(),
                enablePlaceholders = false
            ),
            pagingSourceFactory = { FirebaseSearchEmr(querySearchEmr, query) }
        ).liveData

    fun AcceptConnectionRequest(
        info: UserInfo,
        connectRequest: ConnectRequest,
        connectBtn: MaterialButton,
        connectionSentProgress: CircularProgressIndicator,
    ) {
        myFollowRef.document(info.uid)
            .set(HashMap<String, String>().apply { put(info.uid, "accepted") })
            .addOnSuccessListener {
                connectRequest.Accpeted = true
                connectRequest.Readed = true
                FirebaseFirestore.getInstance().collection(Variables.FireStoreUsersRoot)
                    .document(info.uid)
                    .collection(Variables.REQUEST)
                    .document(FirebaseAuth.getInstance().currentUser?.uid!!)
                    .set(connectRequest)
                MyRequestRef.document(connectRequest.SenderId).set(connectRequest)
                    .addOnSuccessListener { command: Void? ->
                        Thread {
                            val noti = appDatabase.getNotificationDao().SearchNotification(
                                connectRequest.SenderId,
                                connectRequest.RequestDate.time
                            )
                            if (noti != null) {
                                noti.connRequest = connectRequest.apply { Accpeted = true }
                                appDatabase.getNotificationDao().updateNotification(noti)
                            } else {
                                appDatabase.getNotificationDao().insertNotification(
                                    Notification(
                                        connectRequest,
                                        "Connection Request",
                                        "${info.name} is requesting to be in connect with you",
                                        "Tri-tone"
                                    )
                                )
                            }
                        }.start()
                    }.addOnFailureListener { e: Exception? ->
                        run {
                            Log.d(TAG, "AcceptConnectionRequest: ${e?.message}")
                        }
                    }
            }.addOnFailureListener {
                Log.d(TAG, "AcceptConnectionRequest: failed to add user cloud")
            }

    }

    fun RejectConnectionRequest(info: UserInfo) {
        MyRequestRef.document(info.uid).delete().addOnSuccessListener {
            getPartnerRequestToMeInstance(info.uid)
                .delete()
                .addOnSuccessListener {

                    Log.d(TAG, "RejectConnectionRequest: request Rejected")
                }.addOnFailureListener {
                    Log.d(TAG, "RejectConnectionRequest: failed to delete from partner ref")
                }
        }.addOnFailureListener {
            Log.d(TAG, "RejectConnectionRequest: failed to delete from my ref")
        }
    }

    fun CancleConnectionRequest(
        info: UserInfo,
        connectBtn: MaterialButton,
        connectionSentProgress: CircularProgressIndicator,
    ) {
        MyRequestRef.document(info.uid).delete().addOnSuccessListener {
            getPartnerRequestToMeInstance(info.uid)
                .delete()
                .addOnSuccessListener {
                    connectBtn.text = "Connect"
                    connectionSentProgress.isVisible = false
                    Log.d(TAG, "CancleConnectionRequest: request canceled")
                }.addOnFailureListener {
                    Log.d(TAG, "RejectConnectionRequest: failed to delete from partner ref")
                }
        }.addOnFailureListener {
            Log.d(TAG, "RejectConnectionRequest: failed to delete from my ref")
        }
    }

    fun sendConnectRequest(
        userInfo: UserInfo,
        connectBtn: MaterialButton,
        connectionSentProgress: CircularProgressIndicator,
    ) {
        val request = ConnectRequest(
            FirebaseAuth.getInstance().currentUser?.uid!!,
            userInfo.uid,
            Calendar.getInstance().time
        )

        MyRequestRef.document(userInfo.uid).get().addOnSuccessListener {
            if (!it.exists()) {
                excuteConnectionRequest(
                    sendNotification(userInfo, request, "Connection"),
                    userInfo,
                    request
                )
                connectBtn.text = "connection sent"
                connectBtn.setBackgroundColor(
                    ResourcesCompat.getColor(
                        connectBtn.resources,
                        R.color.white,
                        connectBtn.context.theme
                    )
                )
                connectionSentProgress.isVisible = false
                Log.d(TAG, "sendConnectRequest: $request")
            }
        }.addOnFailureListener {
            excuteConnectionRequest(
                sendNotification(userInfo, request, "Connection"),
                userInfo,
                request
            )
        }
    }

    private fun excuteConnectionRequest(
        sendNotification: Call<ResponseBody>,
        userInfo: UserInfo,
        request: ConnectRequest,
    ) {
        sendNotification.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>,
            ) {
                Log.d(TAG, "onResponse: message: ${response.message()} \t code:${response.code()}")
                Log.d(TAG, "onResponse: body: ${response.body()?.byteStream().toString()}")
                Log.d(TAG, "onResponse: error body: ${response.errorBody()}")
                Log.d(TAG, "onResponse: message again:${response.message()}")
                Log.d(TAG, "onResponse: isSuccess: ${response.isSuccessful}")
                Log.d(
                    TAG,
                    "onResponse: receive response: ${response.raw().receivedResponseAtMillis()}"
                )
                Log.d(
                    TAG,
                    "onResponse: sent request millis: ${response.raw().sentRequestAtMillis()}"
                )
                Log.d(
                    TAG,
                    "onResponse: ping: ${
                        response.raw().receivedResponseAtMillis() - response.raw()
                            .sentRequestAtMillis()
                    }"
                )

                MyRequestRef.document(userInfo.uid).set(request)
                    .addOnSuccessListener { Log.d(TAG, "onResponse: My cloud updated") }
                getPartnerRequestToMeInstance(userInfo.uid)
                    .set(request)
                    .addOnSuccessListener {
                        Log.d(TAG, "onResponse: partner cloud updated")
                    }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun sendNotification(
        userInfo: UserInfo,
        request: ConnectRequest,
        Type: String,
    ): Call<ResponseBody> {
        Log.d(TAG, "sendNotification: sending connection notification")
        val ret = notificationApi.sendConnectRequestNotification(
            NotificationConnectionRequest(
                userInfo.token,
                HashMap<String, String>().apply {
                    put("title", "$Type Request")
                    put(
                        "body",
                        "${FirebaseAuth.getInstance().currentUser?.displayName} is requesting to be in $Type with you and see your health condition"
                    )
                    put("sound", "Tri-tone")
                },
                request
            )
        )
        return ret
    }

    fun FollowMedicalUesr(
        info: UserInfo,
        connectBtn: MaterialButton,
        connectionSentProgress: CircularProgressIndicator,
    ) {
        val request = ConnectRequest(
            FirebaseAuth.getInstance().currentUser?.uid!!,
            info.uid,
            Calendar.getInstance().time,
            ConnectType = ConnectionType.FOLLOW.name
        )
        sendNotification(info, request, "Follow").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    MyRequestRef.document(request.ReceiverId).set(request)
                        .addOnSuccessListener { Log.d(TAG, "onResponse: request added to me ") }
                    getPartnerRequestToMeInstance(request.ReceiverId , request.SenderId)
                        .set(request).addOnSuccessListener {
                            Log.d(
                                TAG,
                                "onResponse: request added to ${info.name} "
                            )
                        }
                    connectBtn.text = "Cancel follow"
                    connectionSentProgress.isVisible = false
                } else {
                    Log.d(
                        TAG,
                        "onResponse: code: ${response.code()} , error: ${
                            response.errorBody().toString()
                        }"
                    )
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(
                    connectBtn.context,
                    "make sure you have good network",
                    Toast.LENGTH_SHORT
                ).show()
                connectionSentProgress.isVisible = false
            }
        })
    }

    fun CancelFollowRequest(
        info: UserInfo,
        rejectBtn: MaterialButton,
        connectBtn: MaterialButton,
        connectionSentProgress: CircularProgressIndicator,
    ) {
        MyRequestRef.document(info.uid).delete().addOnSuccessListener {
            getPartnerRequestToMeInstance(info.uid)
                .delete()
                .addOnSuccessListener {
                    rejectBtn.isVisible = false
                    connectBtn.isEnabled = true
                    connectBtn.text = "follow"
                    connectionSentProgress.isVisible = false
                    Log.d(TAG, "FollowMedicalUser: following canceled")
                }
        }
    }

    fun UnFollowMedicalUesr(
        info: UserInfo,
        rejectBtn: MaterialButton,
        connectBtn: MaterialButton,
        connectionSentProgress: CircularProgressIndicator,
    ) {

        getPartnerFollowerToMeInstance(info.uid).delete()
        getPartnerRequestToMeInstance(info.uid).delete()
        MyRequestRef.document(info.uid).delete()
        myFollowRef.document(info.uid).delete()


            .addOnSuccessListener {
                FirebaseFirestore.getInstance().collection(Variables.FireStoreUsersRoot)
                    .document(info.uid)
                    .collection(Variables.FOLLOWERS)
                    .document(FirebaseAuth.getInstance().currentUser?.uid!!)
                    .delete()
                    .addOnSuccessListener {
                        rejectBtn.isVisible = false
                        connectBtn.isEnabled = true
                        connectionSentProgress.isVisible = false
                        Log.d(TAG, "FollowMedicalUesr: followed")
                    }
            }
    }
}