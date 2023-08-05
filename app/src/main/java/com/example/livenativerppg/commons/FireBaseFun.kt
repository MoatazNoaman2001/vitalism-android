package com.example.livenativerppg.commons

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.livenativerppg.component.utility.Variables
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.HashMap


fun getPartnerFollowerToMeInstance(uid:String , me:String=FirebaseAuth.getInstance().currentUser?.uid!!): DocumentReference{
    return FirebaseFirestore.getInstance().collection(Variables.FireStoreUsersRoot).document(uid)
        .collection(Variables.FOLLOWERS).document(me)
}
fun getPartnerRequestToMeInstance(uid:String, me:String=FirebaseAuth.getInstance().currentUser?.uid!!): DocumentReference{
    return FirebaseFirestore.getInstance().collection(Variables.FireStoreUsersRoot).document(uid)
        .collection(Variables.REQUEST).document(me)
}

val startingAccountFirstTime = FirebaseFirestore.getInstance()
    .collection(Variables.FireStoreUsersRoot)
    .document(FirebaseAuth.getInstance().currentUser?.uid!!)
    .collection(Variables.PARMS)
    .document(Variables.STARTINGS)

fun accountActivations(id: String = FirebaseAuth.getInstance().currentUser?.uid!!) = FirebaseFirestore.getInstance()
    .collection(Variables.FireStoreUsersRoot)
    .document(id)
    .collection(Variables.PARMS)
    .document(Variables.Activations)

fun updateUserIsActive(isActive:Boolean){
    accountActivations().get().addOnSuccessListener {
        if (it.exists()) {
            val bool = it.getBoolean(Variables.RPPG_ACTIVE)!!
            accountActivations().set(HashMap<String, Boolean>().apply {
                put(Variables.isUserActive, isActive)
                put(Variables.RPPG_ACTIVE, bool)
            })
        }else{
            accountActivations().set(HashMap<String, Boolean>().apply {
                put(Variables.isUserActive, isActive)
                put(Variables.RPPG_ACTIVE, false)
            })
        }
    }
}
suspend fun checkUserActiveById(id:String): Boolean{
    return accountActivations(id).get().await().getBoolean(Variables.isUserActive)!!
}

fun ErrorLoginHandler(it:Exception , context:Context){
    if (it is FirebaseNetworkException)
        makeToast(context , "you currently offline")
    else if (it is FirebaseAuthEmailException) {
        makeToast(context , "you don't have account")
        Log.d("TAG", "onViewCreated: " + it.message)
    } else if ((it is FirebaseAuthException) && (it as FirebaseAuthException).errorCode.equals("ERROR_WRONG_PASSWORD"))
        makeToast(context , "wrong password")
    else
        makeToast(context ,"seems strange error")
    it.printStackTrace()
}