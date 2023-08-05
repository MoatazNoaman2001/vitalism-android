package com.example.livenativerppg.component.di

import androidx.fragment.app.Fragment
import com.example.livenativerppg.component.utility.Variables
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped
import org.koin.core.component.getScopeName
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(FragmentComponent::class)
object FragmentScope {

//    @Provides
//    @FragmentScoped
//    fun provideQueryRPPGResults(): Query = FirebaseDatabase.getInstance()
//        .getReference(Variables.FireStoreUsersRoot)
//        .child(FirebaseAuth.getInstance().currentUser!!.uid)
//        .orderByKey()
//        .limitToFirst(Variables.EMR_PAGER_LIMIT)

    @Provides
    @FragmentScoped
    fun ProvideFragmentName(fragment:Fragment) :String = "fragment!! ${fragment.getScopeName()}"


    @Provides
    @FragmentScoped
    fun observeMainUserInfo(): CollectionReference = FirebaseFirestore.getInstance()
        .collection(Variables.FireStoreUsersRoot)



    @Provides
    @FragmentScoped
    @Named(value = "emr")
    fun getMeasurementRef(): DatabaseReference = FirebaseDatabase.getInstance()
        .getReference(Variables.FireStoreUsersRoot)
        .child(FirebaseAuth.getInstance().uid!!)
        .child(Variables.MEASUREMENT)
        .child(Variables.HR)

    @Provides
    @FragmentScoped
    @Named(value = "BP_rppg")
    fun getBPMeasurementRPPGRef(): DatabaseReference = FirebaseDatabase.getInstance()
        .getReference(Variables.FireStoreUsersRoot)
        .child(FirebaseAuth.getInstance().uid!!)
        .child(Variables.MEASUREMENT)
        .child(Variables.BP)


    @Provides
    @Named("followers")
    @FragmentScoped
    fun getMyFollowRefNamed() =
        FirebaseFirestore.getInstance()
            .collection(Variables.FireStoreUsersRoot)
            .document(FirebaseAuth.getInstance().currentUser?.uid!!)
            .collection(Variables.FOLLOWERS)

    @Provides
    @Named("requests")
    @FragmentScoped
    fun getMYRequestsRefNamed() =
        FirebaseFirestore.getInstance()
            .collection(Variables.FireStoreUsersRoot)
            .document(FirebaseAuth.getInstance().currentUser?.uid!!)
            .collection(Variables.REQUEST)

    @Provides
    @Named(Variables.Activations)
    @FragmentScoped
    fun getMyActivationUtils() =
        FirebaseFirestore.getInstance()
            .collection(Variables.FireStoreUsersRoot)
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection(Variables.PARMS)
            .document(Variables.Activations)

    @Provides
    @FragmentScoped
    @Named(Variables.PPG_RF_PATH)
    fun PPGRfDocument() = FirebaseDatabase.getInstance().getReference(Variables.FireStoreUsersRoot)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(Variables.MEASUREMENT)
            .child(Variables.PPG)
            .child(Variables.RF)

    @Provides
    @FragmentScoped
    @Named(Variables.PPG_Bp_PATH)
    fun PPGBpDocument() = FirebaseDatabase.getInstance().getReference(Variables.FireStoreUsersRoot)
        .child(FirebaseAuth.getInstance().currentUser!!.uid)
        .child(Variables.MEASUREMENT)
        .child(Variables.PPG)
        .child(Variables.BP)
    @Provides
    @FragmentScoped
    @Named(Variables.PPG_o2_PATH)
    fun PPGSpo2Document() = FirebaseDatabase.getInstance().getReference(Variables.FireStoreUsersRoot)
        .child(FirebaseAuth.getInstance().currentUser!!.uid)
        .child(Variables.MEASUREMENT)
        .child(Variables.PPG)
        .child(Variables.O2)
}