package com.example.livenativerppg.models.MainChatInterface.data

import android.util.Log
import com.example.livenativerppg.commons.makeToast
import com.example.livenativerppg.models.MainChatInterface.data.model.ChatBuilder
import com.example.livenativerppg.models.MainChatInterface.data.model.Message
import com.example.livenativerppg.commons.messageDateFormat
import com.example.livenativerppg.component.base.NotificationApi
import com.example.livenativerppg.component.db.models.Notification
import com.example.livenativerppg.component.db.models.NotificationMessage
import com.example.livenativerppg.component.db.models.VitalSignMeasurement
import com.example.livenativerppg.component.di.MyChatRef
import com.example.livenativerppg.component.di.MyRealTimeChatDB
import com.example.livenativerppg.component.di.MyRequestsRef
import com.example.livenativerppg.component.natives.BPRPPGResult
import com.example.livenativerppg.component.natives.RPPGResult
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.models.MainChatInterface.data.model.MessageState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

private const val TAG = "MainChatRepoImpl"

@ViewModelScoped
class MainChatRepoImpl @Inject constructor(
    @MyRequestsRef val myRequestsRef: CollectionReference,
    @MyRealTimeChatDB val myRealTimeChatDB: DatabaseReference,
    @MyChatRef val myChatRef: CollectionReference,
    val notificationApi: NotificationApi
) : MainChatRepoInterface {

    private var getMessagesList : ((List<Message>) -> Unit)? = null
    private var getError :  ((DatabaseError) -> Unit)? = null
    lateinit var builder : ChatBuilder

    override suspend fun getChatBuilder(partnerID: String): ChatBuilder? =
        withContext(Dispatchers.IO) {
            try {
                builder = myChatRef.document(partnerID).get().await().toObject(ChatBuilder::class.java)!!
                builder
            } catch (e: Exception) {
                Log.d(TAG, "getChatBuilder: ${e.message}")
                null
            }
        }

    override suspend fun CreateChatBuilder(partnerID: String): MainChatRepoBuilderResult =
        withContext(Dispatchers.IO) {
            try {
                val builder = ChatBuilder(
                    FirebaseAuth.getInstance().currentUser?.uid!!,
                    listOf(partnerID),
                    myRealTimeChatDB.push().key!!
                )

                setMyPartnerBuilder(partnerID, builder)
                setPartnerBuilder(partnerID, builder)

                MainChatRepoBuilderResult.OnSuccess(builder)
            } catch (e: Exception) {
                MainChatRepoBuilderResult.OnError(e)
            }
        }

    override fun getMessagesList(success: (List<Message>) -> Unit, error:  ((DatabaseError) -> Unit)?) {
        this.getMessagesList = success
        this.getError = error
    }

    private suspend fun setMyPartnerBuilder(partnerID: String, builder: ChatBuilder) {
        myChatRef.document(partnerID).set(builder).await()
    }

    private suspend fun setPartnerBuilder(partnerID: String, builder: ChatBuilder) {
        FirebaseFirestore.getInstance().collection(Variables.FireStoreUsersRoot)
            .document(partnerID)
            .collection(Variables.CHAT)
            .document(FirebaseAuth.getInstance().currentUser?.uid!!).set(builder).await()
    }

    override fun initMessagesTracker(builder: ChatBuilder) {
        Log.d(TAG, "initMessagesTracker: start message Tracker")
        myRealTimeChatDB.child(builder.chatUniqueKey)
            .addValueEventListener(object  : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        try {
                            val messagesList = ArrayList<Message>()
                            for (it in snapshot.children) {
                                val sender = (it.getValue() as HashMap<*, *>)["senderID"] as String
                                val state = (it.value as HashMap<*, *>)["state"] as String
                                val text = (it.value as HashMap<*, *>)["text"] as String
                                val receiver = (it.value as HashMap<*, *>)["receiverID"] as String
                                val date = ((it.value as HashMap<*, *>)["date"] as HashMap<*, *>)["time"] as Long
                                Log.d(TAG, "onDataChange: $sender ,$state ,$text ,$receiver ,$date")
                                var vital: VitalSignMeasurement
                                var isRppg = false
                                for (child in it.children) {
                                    if (child.key == "rppg") {
                                        var rppg:RPPGResult? = null
                                        var rppgBR:BPRPPGResult? = null
                                        for (cChild in child.children) {
                                            if (cChild.key == "rppg"){
                                                rppg = cChild.getValue(RPPGResult::class.java)!!
                                            }else if(child.key == "rppgBP"){
                                                rppgBR = cChild.getValue(BPRPPGResult::class.java)!!
                                            }
                                        }
                                        val type = (child.value as HashMap<*, *>)["type"] as String
                                        val Date = Date(((child.value as HashMap<*, *>)["date"] as HashMap<*, *>)["time"] as Long)
                                        Log.d(TAG, "onDataChange: $rppg ,$date ,$type")
                                        vital =VitalSignMeasurement(rppg,rppgBR, type, Date)
                                        messagesList.add(Message(Date(date) , sender , receiver , state , text , vital))
                                        isRppg = !isRppg
                                    }
                                }

                                Log.d(TAG, "onDataChange: date: $date , sender: $sender")
                                if (!isRppg)
                                    messagesList.add(Message(Date(date) , sender , receiver , state , text))
                            }
                            getMessagesList?.invoke(messagesList)
                            Log.d(TAG, "onDataChange: $messagesList")
                        } catch (e: Exception) {
                            Log.d(TAG, "onDataChange: error: ${e.message}")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    getError?.invoke(error)
                }

            })
    }

    override suspend fun sendTextMessage(message: Message): MainChatRepoMessageSentResult =
        withContext(Dispatchers.IO){
            try{
                myRealTimeChatDB.child(builder.chatUniqueKey).updateChildren(mapOf(Pair(messageDateFormat.format(message.date) , message.apply { State = MessageState.SENT.name }))).await()
               handleSendMessageNotification(notificationApi.sendMessageNotification(

                   NotificationMessage(arrayOf(message.ReceiverID) ,
                       HashMap<String, String>().apply {
                           put("title", "message notification")
                           put(
                               "body",
                               "new Message ${FirebaseAuth.getInstance().currentUser?.displayName}"
                           )
                           put("sound", "Tri-tone")
                       },

                       message)

               ))
                MainChatRepoMessageSentResult.OnSuccess(message)
            }catch (e: Exception){
                MainChatRepoMessageSentResult.OnError(e)
            }
        }

    private fun handleSendMessageNotification(sendMessageNotification: Call<ResponseBody>) {
        sendMessageNotification.enqueue(object :Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d(TAG, "onResponse: sending message notification code: ${response.code()}, message: ${response.message()}")
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

}