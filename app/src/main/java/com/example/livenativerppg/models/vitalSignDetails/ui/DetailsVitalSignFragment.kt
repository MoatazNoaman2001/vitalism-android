package com.example.livenativerppg.models.vitalSignDetails.ui

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.material.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.example.livenativerppg.R
import com.example.livenativerppg.commons.makeToast
import com.example.livenativerppg.component.base.BaseFragment
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.FragmentDetailsVitalSignBinding
import com.example.livenativerppg.models.faceDetectYNCreated.ui.FeceDetectorYNCreatedActivity
import com.example.livenativerppg.models.heartRateProcessing.ui.HeartRateProcessingPPGActivity
import com.example.livenativerppg.models.heartRateProcessing.ui.HeartRateProcessingRPPGFragment
import com.example.livenativerppg.models.mainRun.vw.MainActivity
import com.example.livenativerppg.models.respiratoryRate.ui.RespiratoryRatePPGFragment
import com.example.livenativerppg.models.vitalSignDetails.data.viewmodel.DetailsVM
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.type.Color
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject
import javax.inject.Named
import kotlin.properties.Delegates

private const val TAG = "DetailsVitalSignFragment"

@AndroidEntryPoint
class DetailsVitalSignFragment :
    BaseFragment<FragmentDetailsVitalSignBinding>(R.layout.fragment_details_vital_sign)
//    BaseFragment<DetailsVitalSignFragment>(R.layout.fragment_details_vital_sign)
{
    private val viewModel by viewModels<DetailsVM>()

    lateinit var controller: NavController
    var value by Delegates.notNull<Int>()
    private var dialog: AlertDialog? = null
    private val oxygenSaturationUri: String =
        "https://firebasestorage.googleapis.com/v0/b/vitalism-solution.appspot.com/o/app%2Futilis%2Fphotos%2Fmousa_ashraf_oxygen_molcules_saturation_in_human_body_flows_int_00bec61a-5b79-472e-8483-57ef5c8cd367.png?alt=media&token=9bf652c8-e89b-485a-a10f-e6c6a12594a4"

    @Inject
    @Named(Variables.Activations)
    lateinit var myActivationUtils: DocumentReference

    override fun onInitialized() {
        super.onInitialized()
        controller = Navigation.findNavController(requireView())
        viewModel.navArguments = arguments
        value = viewModel.navArguments!!.getInt("value")
        Log.d(TAG, "onInitialized: $value")
        binding.detailsVM = viewModel

        binding.btnArrowleft.setOnClickListener {
            controller.popBackStack()
        }
        binding.btnRectangle815.setOnClickListener {
            Toast.makeText(requireContext(), "will be marked as prefered", Toast.LENGTH_SHORT)
                .show()
        }


        viewModel.detailsModel.observe(viewLifecycleOwner) {
            when (value) {
                1 -> {
                    it.txtHeartRate = "Heart Rate Variability"
                    it.txtDescription = getString(R.string.msg_heart_rate_vari4)
                    binding.btnMeasureYourHeartRate.text = "Measure your HRV"
                    Glide.with(requireContext())
                        .load(getString(R.string.heart_rate_veriability_uri))
                        .centerCrop()
                        .into(binding.backImg)
                }
                2 -> {
                    it.txtHeartRate = getString(R.string.msg_respiratory_rat)
                    it.txtDescription = getString(R.string.msg_respiratory_rat_discription)
                    binding.btnMeasureYourHeartRate.text = "Measure your Respiratory rate"
                    Glide.with(requireContext())
                        .load(getString(R.string.respiratory_rat_details_uri))
                        .centerCrop()
                        .into(binding.backImg)
                }
                3 -> {
                    it.txtHeartRate = getString(R.string.msg_oxygen_saturati)
                    it.txtDescription = getString(R.string.msg_oxygen_saturati_discription)
                    binding.btnMeasureYourHeartRate.text = "Measure your Oxygen Saturation"
                    Glide.with(requireContext())
                        .load(oxygenSaturationUri)
                        .into(binding.backImg)
                }

                4 -> {
                    it.txtHeartRate = getString(R.string.lbl_blood_pressure)
                    it.txtDescription = getString(R.string.lbl_blood_pressure_details)
                    binding.btnMeasureYourHeartRate.text = "Measure your Blood Pressure"
                    Glide.with(requireContext())
                        .load(getString(R.string.blood_pressure_details_uri))
                        .into(binding.backImg)
                }
                5 -> {
                    it.txtHeartRate = getString(R.string.msg_pulse_respirati)
                    it.txtDescription = getString(R.string.lbl_pulse_respiration_quotient_details)
                    binding.btnMeasureYourHeartRate.text = "Measure your pulse respiration quotient"
                    Glide.with(requireContext())
                        .load(getString(R.string.respiratory_rat_details_uri))
                        .into(binding.backImg)
                }
            }
        }


        val transitions =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        sharedElementEnterTransition = transitions
        sharedElementReturnTransition = transitions

//        postponeEnterTransition(250 , TimeUnit.MILLISECONDS)

    }


    override fun setUpClicks() {
        binding.btnMeasureYourHeartRate.setOnClickListener {
            when (value) {
                0 -> {
                    myActivationUtils.get().continueWithTask {
                        val builder = AlertDialog.Builder(requireContext())
                            .setView(R.layout.loading_check_availablity);
                        dialog = builder.create()
                        dialog!!.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
                        dialog!!.show()
                        return@continueWithTask it
                    }.addOnSuccessListener {
                            if (dialog != null) {
                                dialog!!.dismiss()
                            }
                            val builder: AlertDialog.Builder?
                            var type = 0
                            builder = if (it.exists()) {
                                if (!it.getBoolean(Variables.RPPG_ACTIVE)!!) {
                                    AlertDialog.Builder(requireContext())
                                        .setView(R.layout.ppg_or_rppg_dialog_layout)
                                } else {
                                    type = 1
                                    AlertDialog.Builder(requireContext())
                                        .setView(R.layout.rppg_not_available_layout)
                                }
                            } else {
                                AlertDialog.Builder(requireContext())
                                    .setView(R.layout.ppg_or_rppg_dialog_layout)
                            }

                            dialog = builder.create()
                            dialog!!.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
                            dialog!!.show()

                            if (type == 0) {
                                dialog!!.findViewById<AppCompatButton>(R.id.ppgBtn)!!
                                    .setOnClickListener {
                                        requireActivity().startActivity(
                                            Intent(
                                                requireActivity(),
                                                HeartRateProcessingPPGActivity::class.java
                                            )
                                        )
                                        dialog?.dismiss()
                                    }
                                dialog!!.findViewById<AppCompatButton>(R.id.rppgBtn)!!
                                    .setOnClickListener {
                                        controller.navigate(
                                            R.id.action_detailsVitalSignFragment_to_heartRateProcessingRPPGFragment,
                                            HeartRateProcessingRPPGFragment.getInstance(
                                                false,
                                                false
                                            ).arguments
                                        )
                                        dialog?.dismiss()
                                    }
                                dialog!!.findViewById<AppCompatButton>(R.id.rppgBtnFacemesh)!!
                                    .setOnClickListener {
                                        controller.navigate(
                                            R.id.action_detailsVitalSignFragment_to_heartRateProcessingRPPGFragment,
                                            HeartRateProcessingRPPGFragment.getInstance(true, false)
                                                .requireArguments()
                                        )
                                        dialog?.dismiss()
                                    }
                            }
                        }

                }
                1 -> {

                    makeToast(requireContext() , "heart rate variability still in maintain")
//                    controller.navigate(
//                        R.id.action_detailsVitalSignFragment_to_heartRateProcessingRPPGFragment,
//                        HeartRateProcessingRPPGFragment.getInstance(false, false).requireArguments()
//                    )
//                    requireActivity().startActivity(Intent(requireActivity() , FeceDetectorYNCreatedActivity::class.java))
                }
                2 -> {
                    controller.navigate(R.id.action_detailsVitalSignFragment_to_respiratoryRatePPGFragment)
                }
                3 -> {
                    controller.navigate(R.id.action_detailsVitalSignFragment_to_oxygenSaturationPPGFragment)
                }
                4 -> {
//                    controller.navigate(R.id.action_detailsVitalSignFragment_to_bloodPressurePPGFragment)

                    myActivationUtils.get().continueWithTask {
                        val builder = AlertDialog.Builder(requireContext())
                            .setView(R.layout.loading_check_availablity);
                        dialog = builder.create()
                        dialog!!.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
                        dialog!!.show()
                        return@continueWithTask it
                    }.addOnSuccessListener {
                        if (dialog != null) {
                            dialog!!.dismiss()
                        }
                        val builder: AlertDialog.Builder?
                        var type = 0
                        builder = if (it.exists()) {
                            if (!it.getBoolean(Variables.RPPG_ACTIVE)!!) {
                                AlertDialog.Builder(requireContext())
                                    .setView(R.layout.ppg_or_rppg_dialog_layout)
                            } else {
                                type = 1
                                AlertDialog.Builder(requireContext())
                                    .setView(R.layout.rppg_not_available_layout)
                            }
                        } else {
                            AlertDialog.Builder(requireContext())
                                .setView(R.layout.ppg_or_rppg_dialog_layout)
                        }

                        dialog = builder.create()
                        dialog!!.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
                        dialog!!.show()

                        if (type == 0) {
                            dialog!!.findViewById<AppCompatTextView>(R.id.label)!!.text = "Blood pressure monitoring"
                            dialog!!.findViewById<AppCompatButton>(R.id.ppgBtn)!!.setOnClickListener {
                                    controller.navigate(R.id.action_detailsVitalSignFragment_to_bloodPressurePPGFragment)
                                    dialog?.dismiss()
                            }
                            dialog!!.findViewById<AppCompatButton>(R.id.rppgBtn)!!.setOnClickListener {
                                    controller.navigate(R.id.action_detailsVitalSignFragment_to_heartRateProcessingRPPGFragment, HeartRateProcessingRPPGFragment.getInstance(
                                            false,
                                            true
                                        ).arguments
                                    )
                                    dialog?.dismiss()
                            }
                            dialog!!.findViewById<AppCompatButton>(R.id.rppgBtnFacemesh)!!.isVisible  = false
                        }
                    }

                }
                5 -> {
                    controller.navigate(
                        R.id.action_detailsVitalSignFragment_to_respiratoryRatePPGFragment,
                        RespiratoryRatePPGFragment.getInstance("pulse_respiration_quotient")
                            .requireArguments()
                    )
                }
                else -> requireActivity().startActivity(
                    Intent(
                        requireActivity(),
                        MainActivity::class.java
                    )
                )
            }
        }
        binding.linearRowarrowleft.setOnClickListener {
            controller.popBackStack()
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(value: Int) = DetailsVitalSignFragment().apply {
            arguments = Bundle().apply {
                putInt("value", value)
            }
        }
    }

}