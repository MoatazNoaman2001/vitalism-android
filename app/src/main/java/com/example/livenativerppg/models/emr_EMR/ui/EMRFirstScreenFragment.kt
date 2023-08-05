package com.example.livenativerppg.models.emr_EMR.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.Dimension.Companion.SP
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.transition.TransitionInflater
import com.example.livenativerppg.R
import com.example.livenativerppg.component.base.BaseFragment
import com.example.livenativerppg.databinding.FragmentEMRFirstScreenBinding
import com.example.livenativerppg.models.emr_EMR.data.viewModel.EMRViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.stream.Stream
import kotlin.streams.toList


private const val TAG = "EMRFirstScreenFragment"

@AndroidEntryPoint
class EMRFirstScreenFragment :
    BaseFragment<FragmentEMRFirstScreenBinding>(R.layout.fragment_e_m_r_first_screen) {

    private var user: FirebaseUser? = null
    private var adapter: DateStringEMRRecycleAdapter? = null
    lateinit var bloodPressureAdapter: BloodPressureDatePagerAdapter
    private val viewModel: EMRViewModel by viewModels()

    private val VITAL_SIGN_TYPE = "type";
    lateinit var dateRangePacker: MaterialDatePicker<androidx.core.util.Pair<Long, Long>>
    lateinit var datePacker: MaterialDatePicker<Long>


    lateinit var type: String;

    companion object {
        fun getInstance(type: String) = EMRFirstScreenFragment().apply {
            arguments = Bundle().apply {
                this.putString(VITAL_SIGN_TYPE, type);
            }
        }
    }


    override fun onInitialized() {
        super.onInitialized()
        if (requireArguments().getString(VITAL_SIGN_TYPE) == null)
            return
        type = requireArguments().getString(VITAL_SIGN_TYPE)!!
        viewModel.type = type
        if (type == "hr") {
            requireActivity().findViewById<MaterialToolbar>(R.id.toolbar).apply {
                subtitle = "heart rate"
            }

            adapter = DateStringEMRRecycleAdapter(requireActivity().findViewById(R.id.viewPager))
            binding.recycleView.adapter = adapter
        } else if (type == "bp") {
            requireActivity().findViewById<MaterialToolbar>(R.id.toolbar).apply {
                subtitle = "blood pressure"
            }

            bloodPressureAdapter =
                BloodPressureDatePagerAdapter(requireActivity().findViewById(R.id.viewPager))
            binding.recycleView.adapter = bloodPressureAdapter
        }



        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            binding.CalenderSelectionText,
            9,
            16,
            1,
            SP
        )
        val transition =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        sharedElementEnterTransition = transition

        binding.CalenderSelectionText.text = "Current Date: all"
        dateRangePacker =
            MaterialDatePicker.Builder.dateRangePicker().setTitleText("select day range").build()
        datePacker =
            MaterialDatePicker.Builder.datePicker().setTitleText("select day range").build()

        dateRangePacker.addOnPositiveButtonClickListener {
            val startDate = Date(it.first)
            val endDate = Date(it.second)
            Log.d(TAG, "onInitialized: first: $startDate , second: $endDate")

            binding.CalenderSelectionText.text =
                "Start at: ${SimpleDateFormat("dd/MM").format(startDate)}" +
                        " End at: ${SimpleDateFormat("dd/MM").format(endDate)}"

            if (Build.VERSION.SDK_INT >= 26) {
                val interval = ChronoUnit.DAYS.between(startDate.toInstant(), endDate.toInstant())
                val dateList = Stream.iterate(
                    startDate.toInstant()
                ) { it.plusSeconds(60 * 60 * 24) }
                    .limit(interval)
                    .toList()

                viewModel.intervalList = ArrayList<Date>().apply {
                    for (instant in dateList) {
                        add(Date(instant.toEpochMilli()))
                    }
                }
                adapter?.refresh()
                Log.d(
                    TAG, "onInitialized: days between:${
                        ArrayList<Date>().apply {
                            for (instant in dateList) {
                                add(Date(instant.toEpochMilli()))
                            }
                        }
                    }"
                )

            }

        }
        datePacker.addOnPositiveButtonClickListener {
            val date = Date(it)
            Log.d(TAG, "onInitialized: date: $date")
            binding.CalenderSelectionText.text =
                "Current Date: ${SimpleDateFormat("dd/MM").format(date)}"
            viewModel.intervalList = ArrayList<Date>().apply { add(date) }
            adapter?.refresh()
        }
        dateRangePacker.addOnDismissListener {

        }

        dateRangePacker.addOnNegativeButtonClickListener {

        }
    }

    override fun addObservers() {
        super.addObservers()
        user = FirebaseAuth.getInstance().currentUser
        if (type == "hr")
            viewModel.HRflow().observe(viewLifecycleOwner) {
                CoroutineScope(lifecycleScope.coroutineContext).launch {
                    adapter?.submitData(it)
                }

                adapter?.addLoadStateListener {
                    binding.emptylayout.isVisible = true
                    when (it.append) {
                        is LoadState.Error -> {
                            (it.append as LoadState.Error).error.printStackTrace()
                        }
                        is LoadState.Loading -> {
                            binding.emptyTextView.text = getString(R.string.loadin_your_emr_data)
                            binding.lottieView.setAnimation(R.raw.loading)
                        }
                        is LoadState.NotLoading -> {
                            if (adapter!!.itemCount == 0) {
                                binding.emptyTextView.text = getString(R.string.no_emr_data)
                                binding.lottieView.setAnimation(R.raw.no_data)
                            } else
                                binding.emptylayout.isVisible = false

                        }
                    }
                }
            }
        else {
            viewModel.BPflow().observe(viewLifecycleOwner){
                CoroutineScope(lifecycleScope.coroutineContext).launch {
                    bloodPressureAdapter.submitData(it)
                }

                bloodPressureAdapter.addLoadStateListener {
                    binding.emptylayout.isVisible = true
                    when (it.append) {
                        is LoadState.Error -> {
                            (it.append as LoadState.Error).error.printStackTrace()
                        }
                        is LoadState.Loading -> {
                            binding.emptyTextView.text = getString(R.string.loadin_your_emr_data)
                            binding.lottieView.setAnimation(R.raw.loading)
                        }
                        is LoadState.NotLoading -> {
                            if (bloodPressureAdapter.itemCount == 0) {
                                binding.emptyTextView.text = getString(R.string.no_emr_data)
                                binding.lottieView.setAnimation(R.raw.no_data)
                            } else
                                binding.emptylayout.isVisible = false

                        }
                    }
                }
            }
        }
    }

    override fun setUpClicks() {
        binding.calenderRangeIcon.setOnClickListener {
            dateRangePacker.show(childFragmentManager, "dateRangePacking")
        }
        binding.calenderSignalDayIcon.setOnClickListener {
            datePacker.show(childFragmentManager, "dateSignalPacking")
        }
    }


}