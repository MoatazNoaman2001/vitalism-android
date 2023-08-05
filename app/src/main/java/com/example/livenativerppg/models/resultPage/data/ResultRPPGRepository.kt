package com.example.livenativerppg.models.resultPage.data

import android.util.Log
import com.example.livenativerppg.commons.birthDayDateFromate
import com.example.livenativerppg.commons.onlyDayName
import com.example.livenativerppg.commons.ppgTimeDateFormat
import com.example.livenativerppg.component.base.NotificationApi
import com.example.livenativerppg.component.db.models.ConnectRequest
import com.example.livenativerppg.component.db.models.NotificationRppgVitalSignMeasured
import com.example.livenativerppg.component.db.models.VitalSignMeasurement
import com.example.livenativerppg.component.db.models.VitalsSignType
import com.example.livenativerppg.component.di.MyRequestsRef
import com.example.livenativerppg.component.natives.BPRPPGResult
import com.example.livenativerppg.component.natives.RPPGResult
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.models.MainChatInterface.data.MainChatRepoBuilderResult
import com.example.livenativerppg.models.MainChatInterface.data.MainChatRepoImpl
import com.example.livenativerppg.models.MainChatInterface.data.MainChatRepoMessageSentResult
import com.example.livenativerppg.models.MainChatInterface.data.model.Message
import com.example.livenativerppg.models.MainChatInterface.data.model.MessageState
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.roundToInt


private const val TAG = "ResultRPPGRepository"

@ViewModelScoped
class ResultRPPGRepository @Inject constructor(
    val notificationApi: NotificationApi,
    @MyRequestsRef val myRequestsRef: CollectionReference,
) {
    fun sendMeasureNotificationHr(
        rppgResult: RPPGResult,
        type: String,
        chatListRepoImpl: MainChatRepoImpl,
    ) {
        val vitalSignMeasurement = VitalSignMeasurement(rppgResult,null, type, Calendar.getInstance().time)
        myRequestsRef.get().addOnSuccessListener {
            var ids = ArrayList<String>()
            if (it != null && !it.isEmpty) {
                ids =
                    it.documents.filter { return@filter it.toObject(ConnectRequest::class.java)?.Accpeted!! }
                        .map { it.id }.toCollection(ArrayList())
            }
            FirebaseFirestore.getInstance()
                .collection(Variables.FireStoreUsersRoot)
                .whereIn("uid", ids)
                .whereEqualTo("userType", UserType.Medical.name)
                .get()
                .addOnSuccessListener {
                    if (it != null && !it.isEmpty) {
                        val tokens = it.documents.map { it.toObject(UserInfo::class.java)!!.token }
                        val notificationRppg =
                            NotificationRppgVitalSignMeasured(
                                tokens.toTypedArray(),
                                HashMap<String, String>().apply {
                                    put("title", "vital notification")
                                    put(
                                        "body",
                                        "${FirebaseAuth.getInstance().currentUser?.displayName} sent you his last $type \n $type: ${rppgResult.mean}"
                                    )
                                    put("sound", "Tri-tone")
                                },
                                vitalSignMeasurement
                            )


                        handleCallResponseHR(notificationApi.sendMeasureNotification(notificationRppg), it , chatListRepoImpl , rppgResult)
                    }
                }
        }
    }

    fun sendMeasureNotificationBP(
        bprppgResult: BPRPPGResult,
        type: String,
        chatListRepoImpl: MainChatRepoImpl,
    ) {
        val vitalSignMeasurement = VitalSignMeasurement(rppgBP = bprppgResult , rppgHr = null, type = type, Date =  Calendar.getInstance().time)
        myRequestsRef.get().addOnSuccessListener {
            var ids = ArrayList<String>()
            if (it != null && !it.isEmpty) {
                ids =
                    it.documents.filter { return@filter it.toObject(ConnectRequest::class.java)?.Accpeted!! }
                        .map { it.id }.toCollection(ArrayList())
            }
            FirebaseFirestore.getInstance()
                .collection(Variables.FireStoreUsersRoot)
                .whereIn("uid", ids)
                .whereEqualTo("userType", UserType.Medical.name)
                .get()
                .addOnSuccessListener {
                    if (it != null && !it.isEmpty) {
                        val tokens = it.documents.map { it.toObject(UserInfo::class.java)!!.token }
                        val notificationRppg =
                            NotificationRppgVitalSignMeasured(
                                tokens.toTypedArray(),
                                HashMap<String, String>().apply {
                                    put("title", "vital notification")
                                    put(
                                        "body",
                                        "${FirebaseAuth.getInstance().currentUser?.displayName} sent you his last $type \n $type: ${bprppgResult.dp}/${bprppgResult.sp}"
                                    )
                                    put("sound", "Tri-tone")
                                },
                                vitalSignMeasurement
                            )


                        handleCallResponseBR(notificationApi.sendMeasureNotification(notificationRppg), it , chatListRepoImpl , bprppgResult)
                    }
                }
        }
    }
    private fun handleCallResponseBR(
        sendMeasureNotification: Call<ResponseBody>,
        it: QuerySnapshot,
        chatListRepoImpl: MainChatRepoImpl,
        bprppgResult: BPRPPGResult
    ) {
        sendMeasureNotification.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                it.documents.map { it.toObject(UserInfo::class.java) }.forEach {
                    CoroutineScope(Dispatchers.IO).launch {
                        val builder = chatListRepoImpl.getChatBuilder(it?.uid!!)
                        if (builder != null) {
                            val date = Calendar.getInstance().time
                            when(val res= chatListRepoImpl.sendTextMessage(
                                Message(
                                    Calendar.getInstance().time,
                                    FirebaseAuth.getInstance().currentUser?.uid!!,
                                    it.uid,
                                    MessageState.WAIT.name,
                                    "Hello Dr. ${it.name}, I am Vitalism Chat-bot. The patient, Mr.${FirebaseAuth.getInstance().currentUser?.displayName!!}, who monitored the vital signs (bloodPressure), measured on ${onlyDayName.format(date)} corresponding to ${birthDayDateFromate.format(date)} at ${ppgTimeDateFormat.format(date)}, and the result of the measurement was  ${bprppgResult.sp}/${bprppgResult.dp} beats per minute\n" +
                                            "Thank you for your use Vitalism",
                                    VitalSignMeasurement(null, bprppgResult , VitalsSignType.BP.name, date)
                                )
                            )){
                                is MainChatRepoMessageSentResult.OnSuccess -> {
                                    Log.d(TAG, "sendMeasureNotification: ${res.message}")
                                }
                                is MainChatRepoMessageSentResult.OnError -> {
                                    Log.d(TAG, "sendMeasureNotification: error:${res.error.message}")
                                }
                            }
                        }else{
                            val date = Calendar.getInstance().time
                            when(val res = chatListRepoImpl.CreateChatBuilder(it.uid)){
                                is MainChatRepoBuilderResult.OnSuccess -> {
                                    when(val res2= chatListRepoImpl.sendTextMessage(
                                        Message(
                                            Calendar.getInstance().time,
                                            FirebaseAuth.getInstance().currentUser?.uid!!,
                                            it.uid,
                                            MessageState.WAIT.name,
                                            "Hello Dr. ${it.name}, I am Vitalism Chat-bot. The patient, Mr.${FirebaseAuth.getInstance().currentUser?.displayName!!}, who monitored the vital signs (bloodPressure), measured on ${onlyDayName.format(date)} corresponding to ${birthDayDateFromate.format(date)} at ${ppgTimeDateFormat.format(date)}, and the result of the measurement was ${bprppgResult.sp}/${bprppgResult.dp} beats per minute\n" +
                                                    "Thank you for your use Vitalism",                                        )
                                    )){
                                        is MainChatRepoMessageSentResult.OnSuccess -> {
                                            Log.d(TAG, "sendMeasureNotification: ${res2.message}")
                                        }
                                        is MainChatRepoMessageSentResult.OnError -> {
                                            Log.d(TAG, "sendMeasureNotification: error:${res2.error.message}")
                                        }
                                    }
                                }
                                is MainChatRepoBuilderResult.OnError -> {
                                    Log.d(TAG, "onResponse: ${res.error.message}")
                                }
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun handleCallResponseHR(
        sendMeasureNotification: Call<ResponseBody>,
        it: QuerySnapshot,
        chatListRepoImpl: MainChatRepoImpl,
        rppgResult: RPPGResult
    ) {
        sendMeasureNotification.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                it.documents.map { it.toObject(UserInfo::class.java) }.forEach {
                    CoroutineScope(Dispatchers.IO).launch {
                        val builder = chatListRepoImpl.getChatBuilder(it?.uid!!)
                        if (builder != null) {
                            val date = Calendar.getInstance().time
                            when(val res= chatListRepoImpl.sendTextMessage(
                                Message(
                                    Calendar.getInstance().time,
                                    FirebaseAuth.getInstance().currentUser?.uid!!,
                                    it.uid,
                                    MessageState.WAIT.name,
                                    "Hello Dr. ${it.name}, I am Vitalism Chatbot. The patient, Mr.${FirebaseAuth.getInstance().currentUser?.displayName!!}, who monitored the vital signs (heartbeat), measured on ${onlyDayName.format(date)} corresponding to ${birthDayDateFromate.format(date)} at ${ppgTimeDateFormat.format(date)}, and the result of the measurement was ${(rppgResult.mean *100).roundToInt() / 100} beats per minute\n" +
                                            "Thank you for your use Vitalism",
                                    VitalSignMeasurement(rppgResult ,null, VitalsSignType.HR.name, date)
                                )
                            )){
                                is MainChatRepoMessageSentResult.OnSuccess -> {
                                    Log.d(TAG, "sendMeasureNotification: ${res.message}")
                                }
                                is MainChatRepoMessageSentResult.OnError -> {
                                    Log.d(TAG, "sendMeasureNotification: error:${res.error.message}")
                                }
                            }
                        }else{
                            val date = Calendar.getInstance().time
                            when(val res = chatListRepoImpl.CreateChatBuilder(it.uid)){
                                is MainChatRepoBuilderResult.OnSuccess -> {
                                    when(val res2= chatListRepoImpl.sendTextMessage(
                                        Message(
                                            Calendar.getInstance().time,
                                            FirebaseAuth.getInstance().currentUser?.uid!!,
                                            it.uid,
                                            MessageState.WAIT.name,
                                            "Hello Dr. ${it.name}, I am Vitalism Chatbot. The patient, Mr.${FirebaseAuth.getInstance().currentUser?.displayName!!}, who monitored the vital signs (heartbeat), measured on ${onlyDayName.format(date)} corresponding to ${birthDayDateFromate.format(date)} at ${ppgTimeDateFormat.format(date)}, and the result of the measurement was ${(rppgResult.mean *100).roundToInt() / 100} beats per minute\n" +
                                                    "Thank you for your use Vitalism",                                        )
                                    )){
                                        is MainChatRepoMessageSentResult.OnSuccess -> {
                                            Log.d(TAG, "sendMeasureNotification: ${res2.message}")
                                        }
                                        is MainChatRepoMessageSentResult.OnError -> {
                                            Log.d(TAG, "sendMeasureNotification: error:${res2.error.message}")
                                        }
                                    }
                                }
                                is MainChatRepoBuilderResult.OnError -> {
                                    Log.d(TAG, "onResponse: ${res.error.message}")
                                }
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }
}
