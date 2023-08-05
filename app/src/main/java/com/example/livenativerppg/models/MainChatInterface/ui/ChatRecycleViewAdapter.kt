package com.example.livenativerppg.models.MainChatInterface.ui

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.livenativerppg.R
import com.example.livenativerppg.commons.rppgDateFormat
import com.example.livenativerppg.commons.rppgTimeDateFormat
import com.example.livenativerppg.component.db.models.VitalSignMeasurement
import com.example.livenativerppg.databinding.RppgChatMsgRecycleItemBinding
import com.example.livenativerppg.databinding.TextMsgRecycleItemBinding
import com.example.livenativerppg.models.MainChatInterface.data.model.Message
import com.example.livenativerppg.models.MainChatInterface.data.model.MessageState
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import kotlin.math.roundToInt

class ChatRecycleViewAdapter : ListAdapter<Message, ChatRecycleViewAdapter.ViewHolder>(object :
    DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.date == newItem.date
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }

}) {
    class ViewHolder : RecyclerView.ViewHolder {
        lateinit var textBinding: TextMsgRecycleItemBinding
        lateinit var rppBinding: RppgChatMsgRecycleItemBinding

        constructor(binding: TextMsgRecycleItemBinding) : super(binding.root) {
            this.textBinding = binding
        }


        constructor(binding: RppgChatMsgRecycleItemBinding) : super(binding.root) {
            this.rppBinding = binding
        }


    }

    override fun getItemViewType(position: Int): Int {
        return if(getItem(position).rppg?.rppgHr != null || getItem(position).rppg?.rppgBP != null){
            5
        }else{
            0
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if(viewType == 0)
            ViewHolder(
                TextMsgRecycleItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        else
            ViewHolder(
                RppgChatMsgRecycleItemBinding.inflate(
                    LayoutInflater.from(parent.context) ,
                    parent ,
                    false
                )
            )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        if (holder.itemViewType == 0) {
            holder.textBinding.textMsg.text = item.Text
            if (item.SenderID != FirebaseAuth.getInstance().currentUser?.uid) {
                ViewCompat.setBackgroundTintList(
                    holder.textBinding.textMsg,
                    ColorStateList.valueOf(
                        ResourcesCompat.getColor(
                            holder.textBinding.root.resources,
                            R.color.blue_A200,
                            holder.textBinding.root.context.theme
                        )
                    )
                )

                ViewCompat.setBackgroundTintList(
                    holder.textBinding.extraView,
                    ColorStateList.valueOf(
                        ResourcesCompat.getColor(
                            holder.textBinding.root.resources,
                            R.color.blue_A200,
                            holder.textBinding.root.context.theme
                        )
                    )
                )

                holder.textBinding.textMsg.setTextColor(
                    ResourcesCompat.getColor(
                        holder.textBinding.root.resources,
                        R.color.white_A700,
                        holder.textBinding.root.context.theme
                    )
                )
            }
            when (item.State) {
                MessageState.WAIT.name -> {
                    Glide.with(holder.textBinding.stateImg.context)
                        .load(R.drawable.hourglass_top)
                        .into(holder.textBinding.stateImg)
                }
                else -> {
                    Glide.with(holder.textBinding.stateImg.context)
                        .load(R.drawable.ic_round_check_24)
                        .into(holder.textBinding.stateImg)
                }
            }
        }else{
            holder.rppBinding.textMsg.text = item.Text
            holder.rppBinding.rppgLayout.DateTextView.text = rppgDateFormat.format(item.rppg?.Date)
            holder.rppBinding.rppgLayout.TimeTextView.text = rppgTimeDateFormat.format(item.rppg?.Date)
            if (item.rppg?.rppgHr != null) {
                holder.rppBinding.rppgLayout.meanBeat.text =
                    (((item.rppg?.rppgHr?.mean?.times(100))?.roundToInt() ?: 0) / 100).toString()
                holder.rppBinding.rppgLayout.MaxBeat.text =
                    (((item.rppg?.rppgHr?.max?.times(100))?.roundToInt() ?: 0) / 100).toString()
                holder.rppBinding.rppgLayout.minBeat.text =
                    (((item.rppg?.rppgHr?.min?.times(100))?.roundToInt() ?: 0) / 100).toString()
            }else{
                holder.rppBinding.rppgLayout.meanBeat.text ="${item.rppg?.rppgBP?.sp}/${item.rppg?.rppgBP?.dp}"
                holder.rppBinding.rppgLayout.MaxBeat.isVisible = false
                holder.rppBinding.rppgLayout.minBeat.isVisible = false
            }
            if (item.SenderID != FirebaseAuth.getInstance().currentUser?.uid) {
                ViewCompat.setBackgroundTintList(
                    holder.rppBinding.textLayout,
                    ColorStateList.valueOf(
                        ResourcesCompat.getColor(
                            holder.rppBinding.root.resources,
                            R.color.blue_A200,
                            holder.rppBinding.root.context.theme
                        )
                    )
                )

                ViewCompat.setBackgroundTintList(
                    holder.rppBinding.extraView,
                    ColorStateList.valueOf(
                        ResourcesCompat.getColor(
                            holder.rppBinding.root.resources,
                            R.color.blue_A200,
                            holder.rppBinding.root.context.theme
                        )
                    )
                )

                holder.rppBinding.textMsg.setTextColor(
                    ResourcesCompat.getColor(
                        holder.rppBinding.root.resources,
                        R.color.white_A700,
                        holder.rppBinding.root.context.theme
                    )
                )
            }
            when (item.State) {
                MessageState.WAIT.name -> {
                    Glide.with(holder.rppBinding.stateImg.context)
                        .load(R.drawable.hourglass_top)
                        .into(holder.rppBinding.stateImg)
                }
                else -> {
                    Glide.with(holder.rppBinding.stateImg.context)
                        .load(R.drawable.ic_round_check_24)
                        .into(holder.rppBinding.stateImg)
                }
            }
        }
    }
}