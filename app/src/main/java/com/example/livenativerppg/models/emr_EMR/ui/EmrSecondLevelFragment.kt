package com.example.livenativerppg.models.emr_EMR.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.livenativerppg.R
import com.example.livenativerppg.component.base.BaseFragment
import com.example.livenativerppg.component.natives.RPPGResult
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.EmrSearchRecycleViewItemBinding
import com.example.livenativerppg.databinding.FragmentEmrSecondLevekBinding
import com.example.livenativerppg.databinding.ItemTimelineBinding
import com.github.vipulasri.timelineview.TimelineView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "EmrSecondLevekFragment"
private const val DATE = "date"
private const val TIME = "time"

@AndroidEntryPoint
class EmrSecondLevelFragment :
    BaseFragment<FragmentEmrSecondLevekBinding>(R.layout.fragment_emr_second_levek) {
    private var time: String? = ""
    private var date: String? = ""
    lateinit var liveHr: DatabaseReference
    lateinit var adapter: RealTimeRPPPgRecycleViewAdapter

    override fun onInitialized() {
        super.onInitialized()
        time = requireArguments().getString(TIME).toString()
        date = requireArguments().getString(DATE).toString()
        liveHr = FirebaseDatabase.getInstance().getReference(Variables.FireStoreUsersRoot)
            .child(FirebaseAuth.getInstance().currentUser?.uid!!)
            .child(Variables.MEASUREMENT)
            .child(Variables.HR)
            .child(date!!)
            .child(time!!)

        adapter = RealTimeRPPPgRecycleViewAdapter()
        binding.realTimeRecycleView.adapter = adapter

    }

    override fun addObservers() {
        super.addObservers()

        liveHr.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rppgs = snapshot.children.map { it.getValue(RPPGResult::class.java) }
                    .map {
                        it?.time = SimpleDateFormat("hh-MM-yyyy EEE  hh:mm").parse("$date  $time").time
                        it
                    }.toList()
                Log.d(TAG, "onChildAdded: ${rppgs.joinToString { it.toString() }}")
                adapter.submitList(rppgs)
                binding.realTimeRecycleView.layoutManager?.scrollToPosition(rppgs.lastIndex)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun setUpClicks() {

    }

    companion object {
        fun getInstance(date: String, time: String) = EmrSecondLevelFragment().apply {
            arguments = Bundle().apply {
                putString(DATE, date)
                putString(TIME, time)
            }
        }
    }

    inner class RealTimeRPPPgRecycleViewAdapter :
        ListAdapter<RPPGResult, RealTimeRPPPgRecycleViewAdapter.ViewHolder>(RealTimeDiffUtil()) {

        override fun getItemViewType(position: Int): Int {
            return TimelineView.getTimeLineViewType(position, itemCount)
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): RealTimeRPPPgRecycleViewAdapter.ViewHolder {
            return ViewHolder(
                EmrSearchRecycleViewItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), viewType
            )
        }

        override fun onBindViewHolder(
            holder: RealTimeRPPPgRecycleViewAdapter.ViewHolder,
            position: Int,
        ) {
            holder.binding.DateTextView.text = SimpleDateFormat("dd-EEEE").format(Date(getItem(position).time))
            holder.binding.TimeTextView.text = SimpleDateFormat("hh:mm:ss a").format(Date(getItem(position).time))

            holder.binding.MaxBeat.text = "Max: " + Math.ceil(getItem(position).max)
            holder.binding.meanBeat.text = "Mean: " + Math.ceil(getItem(position).mean)
            holder.binding.minBeat.text = "Min: " + Math.ceil(getItem(position).min)

        }

        inner class ViewHolder(val binding: EmrSearchRecycleViewItemBinding, viewType: Int) :
            RecyclerView.ViewHolder(binding.root) {
            init {
                binding.timeline.initLine(viewType)
            }
        }

    }

    inner class RealTimeDiffUtil : DiffUtil.ItemCallback<RPPGResult>() {
        override fun areContentsTheSame(oldItem: RPPGResult, newItem: RPPGResult): Boolean {
            return newItem.equals(oldItem)
        }

        override fun areItemsTheSame(oldItem: RPPGResult, newItem: RPPGResult): Boolean {
            return newItem.time.equals(oldItem.time)
        }

    }

}