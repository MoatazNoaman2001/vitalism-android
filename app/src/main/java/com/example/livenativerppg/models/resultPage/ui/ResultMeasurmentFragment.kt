package com.example.livenativerppg.models.resultPage.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentResultListener
import com.example.livenativerppg.R
import com.example.livenativerppg.component.natives.RPPGResult
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.FragmentResultMeasurmentBinding
import com.example.livenativerppg.models.resultPage.data.model.ResultRecycleViewmodel
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter


private const val ARG_RESULT = "result"
private const val TAG = "ResultMeasurmentFragmen"

class ResultMeasurmentFragment : Fragment() {
    private var result: RPPGResult? = null
    lateinit var user: FirebaseUser
    lateinit var adapter: ResultRecycleViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            result = it.getSerializable(ARG_RESULT) as RPPGResult?
        }
    }

    lateinit var binding: FragmentResultMeasurmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentResultMeasurmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = FirebaseAuth.getInstance().currentUser!!
        binding.resultTextView.text = "your heart rate is ${result?.mean} bpm"
        adapter = ResultRecycleViewAdapter(
            getList()
        )
        binding.resultRecycleView.adapter = adapter

    }

    private fun getList(): ArrayList<ResultRecycleViewmodel>? {
        return ArrayList<ResultRecycleViewmodel>().apply {
            add(ResultRecycleViewmodel(1, "UserName", user.displayName))
            FirebaseFirestore.getInstance().collection(Variables.FireStoreUsersRoot)
                .document(user.uid)
                .get().addOnSuccessListener {
                    if (it.exists()){
                        val info = it.toObject(UserInfo::class.java)
                        add(ResultRecycleViewmodel(2, "Email", info?.email))
                        add(ResultRecycleViewmodel(3, "Blood Type", info?.BloodType))
                        add(ResultRecycleViewmodel(4, "age", Period.between(
                            LocalDate.parse(info?.BirthDay , DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            LocalDate.now()
                        ).years.toString()))
                    }
                }

        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ResultMeasurmentFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_RESULT, param1)
                }
            }
    }
}