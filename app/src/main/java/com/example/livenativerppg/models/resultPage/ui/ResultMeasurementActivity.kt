package com.example.livenativerppg.models.resultPage.ui

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.livenativerppg.R
import com.example.livenativerppg.commons.birthDayDateFromate
import com.example.livenativerppg.component.db.models.VitalsSignType
import com.example.livenativerppg.component.natives.BPRPPGResult
import com.example.livenativerppg.component.natives.RPPGResult
import com.example.livenativerppg.component.utility.Variables
import com.example.livenativerppg.databinding.ActivityResultMeasurmentBinding
import com.example.livenativerppg.databinding.ResultMeasurmentRecycleItemBinding
import com.example.livenativerppg.models.resultPage.data.viewmodel.ResultVM
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.Gender
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.streams.toList


private const val TAG = "ResultMeasurementActivi"

@AndroidEntryPoint
class ResultMeasurementActivity : AppCompatActivity() {

    lateinit var results: ArrayList<RPPGResult>
    lateinit var binding: ActivityResultMeasurmentBinding
    lateinit var adapter: ResultRecycleAdapter

    private val viewModel: ResultVM by viewModels()

    @Inject
    lateinit var myInfo: Task<DocumentSnapshot>
    lateinit var infoList: ArrayList<String>

    @Inject
    lateinit var sharedPreferences: SharedPreferences
    lateinit var mainUserInfo: UserInfo
    private var isBP = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultMeasurmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = ResultRecycleAdapter()
        infoList = ArrayList()
        binding.resultRecycleView.adapter = adapter
        mainUserInfo = Gson().fromJson(
            sharedPreferences.getString(Variables.USER_INFO, ""),
            UserInfo::class.java
        )

        results = intent.getParcelableArrayListExtra("results")!!
        isBP = intent.getBooleanExtra("isBP", false)
        Log.d(TAG, "onCreate: ${results.size}")

        myInfo.addOnSuccessListener {
            if (it.exists()) {
                val info = it.toObject(UserInfo::class.java)

                assert(info != null)
                infoList.add(info?.name!!)
                infoList.add(info.email)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    infoList.add(
                        Period.between(
                            Instant.ofEpochMilli(
                                SimpleDateFormat("dd/MM/yyyy").parse(
                                    info.BirthDay
                                ).time
                            ).atZone(ZoneId.systemDefault()).toLocalDate(), LocalDate.now()
                        ).years.toString()
                    )
                }
                infoList.add(info.gender)
                infoList.add(info.BloodType.toString())
                infoList.add(info.phoneNumber.toString())
                infoList.add(info.country!!.split("  ")[0])

                adapter.submitList(infoList)
            }
        }


        if (results.map { Collections.frequency(results.map { ceil(it.mean) }, ceil(it.mean)) }
                .last() > 7) {
            val lst = results.stream().sorted(compareBy { t: RPPGResult -> t.mean }).toList()
            if (isBP) {
                val res = lst.map {
                    val Beats = it.mean
                    val Wei = mainUserInfo.weight.toDouble()
                    val Hei = mainUserInfo.height.toDouble()
                    val Agg = Period.between(
                        birthDayDateFromate.parse(mainUserInfo.BirthDay).toInstant().atZone(
                            ZoneId.systemDefault()
                        ).toLocalDate(), LocalDate.now()
                    ).years.toFloat()
                    val Q = if (mainUserInfo.gender == Gender.male.name) 5.0 else 4.5

                    val ROB = 18.5
                    val ET = 364.5 - 1.23 * Beats
                    val BSA = 0.007184 * Math.pow(Wei, 0.425) * Math.pow(Hei, 0.725)
                    val SV = -6.6 + 0.25 * (ET - 35) - 0.62 * Beats + 40.4 * BSA - 0.51 * Agg
                    val PP = SV / (0.013 * Wei - 0.007 * Agg - 0.004 * Beats + 1.307)
                    val MPP = Q * ROB

                    val SP = (MPP + 3 / 2 * PP).toInt()
                    val DP = (MPP - PP / 3).toInt()
                    val date = Calendar.getInstance().time
                    BPRPPGResult(SP, DP, date.time)
                }.last()
                binding.resultTextView.text =
                    "your ${if (isBP) "Blood Pressure" else "Heart rate"} is ${res.sp}/${res.dp}  ${if (isBP) "BP" else "BPM"} "
            } else {
                binding.resultTextView.text =
                    "your ${if (isBP) "Blood Pressure" else "Heart rate"} is ${results.map { it.mean }.average().roundToInt()}  ${if (isBP) "BP" else "BPM"} "
            }
        } else {
            if (isBP) {
                val res = results.map {
                    val Beats = it.mean
                    val Wei = mainUserInfo.weight.toDouble()
                    val Hei = mainUserInfo.height.toDouble()
                    val Agg = Period.between(
                        birthDayDateFromate.parse(mainUserInfo.BirthDay).toInstant().atZone(
                            ZoneId.systemDefault()
                        ).toLocalDate(), LocalDate.now()
                    ).years.toFloat()
                    val Q = if (mainUserInfo.gender == Gender.male.name) 5.0 else 4.5

                    val ROB = 18.5
                    val ET = 364.5 - 1.23 * Beats
                    val BSA = 0.007184 * Math.pow(Wei, 0.425) * Math.pow(Hei, 0.725)
                    val SV = -6.6 + 0.25 * (ET - 35) - 0.62 * Beats + 40.4 * BSA - 0.51 * Agg
                    val PP = SV / (0.013 * Wei - 0.007 * Agg - 0.004 * Beats + 1.307)
                    val MPP = Q * ROB

                    val SP = (MPP + 3 / 2 * PP).toInt()
                    val DP = (MPP - PP / 3).toInt()
                    val date = Calendar.getInstance().time
                    BPRPPGResult(SP, DP, date.time)
                }.last()
                binding.resultTextView.text =
                    "your ${if (isBP) "Blood Pressure" else "Heart rate"} is ${res.sp}/${res.dp}  ${if (isBP) "BP" else "BPM"} "
            } else {
                binding.resultTextView.text =
                    "your ${if (isBP) "Blood Pressure" else "Heart rate"} is ${results.map { it.mean }.average().roundToInt()}  ${if (isBP) "BP" else "BPM"} "
            }
        }
        binding.emrBtn.setOnClickListener {
            finish()
        }

        val bp_list = results.map {
            val Beats = it.mean
            val Wei = mainUserInfo.weight.toDouble()
            val Hei = mainUserInfo.height.toDouble()
            val Agg = Period.between(
                birthDayDateFromate.parse(mainUserInfo.BirthDay).toInstant().atZone(
                    ZoneId.systemDefault()
                ).toLocalDate(), LocalDate.now()
            ).years.toFloat()
            val Q = if (mainUserInfo.gender == Gender.male.name) 5.0 else 4.5

            val ROB = 18.5
            val ET = 364.5 - 1.23 * Beats
            val BSA = 0.007184 * Math.pow(Wei, 0.425) * Math.pow(Hei, 0.725)
            val SV = -6.6 + 0.25 * (ET - 35) - 0.62 * Beats + 40.4 * BSA - 0.51 * Agg
            val PP = SV / (0.013 * Wei - 0.007 * Agg - 0.004 * Beats + 1.307)
            val MPP = Q * ROB

            val SP = (MPP + 3 / 2 * PP).toInt()
            val DP = (MPP - PP / 3).toInt()
            val date = Calendar.getInstance().time
            BPRPPGResult(SP, DP, date.time)
        }.toList()

        if (!isBP) {
            viewModel.sendMeasureNotificationHr(
                rppgResult = RPPGResult(
                    results.last().time,
                    results.map { it.mean }.average(),
                    results.map { it.min }.average(),
                    results.map { it.max }.average()
                ),
                VitalsSignType.HR.name
            )
        } else {
            viewModel.sendMeasureNotificationBP(
                BPRPPGResult(
                    bp_list.map { it.sp }.average().toInt(),
                    bp_list.map { it.dp }.average().toInt(),
                    bp_list.map { it.date }.last(),
                ),
                VitalsSignType.BP.name
            )
        }
        binding.vitalSignTextCondition.text =
            if (isBP) "Blood Pressure Condition" else "Heart Rate Condition"
        if (!isBP) {
            val mean: Int = (results.sumOf { it.mean } / results.map { it.mean }.size).toInt()
            when (mean) {
                in 60..100 -> {
                    binding.ConditionIcon.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.img_checkmark_66x66,
                            theme
                        )
                    )
                    binding.vitalSignTextCondition.setTextColor(Color.GREEN)
                    binding.FullResultCheckImage.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.img_checkmark_66x66,
                            theme
                        )
                    )

                }
                else -> {

                    binding.ConditionIcon.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_bad_condition_check_mark,
                            theme
                        )
                    )
                    binding.FullResultCheckImage.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_bad_condition_check_mark,
                            theme
                        )
                    )
                    binding.vitalSignTextCondition.setTextColor(Color.RED)
                }
            }
        } else {
            val sp = bp_list.map { it.sp }.average().toInt()
            val dp = bp_list.map { it.dp }.average().toInt()
            when (sp) {
                in 120..140 -> {
                    binding.ConditionIcon.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.img_checkmark_66x66,
                            theme
                        )
                    )
                    binding.vitalSignTextCondition.setTextColor(Color.GREEN)
                    binding.FullResultCheckImage.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.img_checkmark_66x66,
                            theme
                        )
                    )

                }
                else -> {

                    binding.ConditionIcon.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_bad_condition_check_mark,
                            theme
                        )
                    )
                    binding.FullResultCheckImage.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_bad_condition_check_mark,
                            theme
                        )
                    )
                    binding.vitalSignTextCondition.setTextColor(Color.RED)
                }
            }
            when (dp) {
                in 70..80 -> {
                    binding.ConditionIcon.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.img_checkmark_66x66,
                            theme
                        )
                    )
                    binding.vitalSignTextCondition.setTextColor(Color.GREEN)
                    binding.FullResultCheckImage.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.img_checkmark_66x66,
                            theme
                        )
                    )

                }
                else -> {

                    binding.ConditionIcon.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_bad_condition_check_mark,
                            theme
                        )
                    )
                    binding.FullResultCheckImage.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_bad_condition_check_mark,
                            theme
                        )
                    )
                    binding.vitalSignTextCondition.setTextColor(Color.RED)
                }
            }
        }
    }

    inner class ResultRecycleAdapter : ListAdapter<String, ResultRecycleAdapter.ViewHolder>(object :
        DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return true
        }

    }) {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): ResultRecycleAdapter.ViewHolder {
            return ViewHolder(
                ResultMeasurmentRecycleItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ResultRecycleAdapter.ViewHolder, position: Int) {
            when (position) {
                0 -> {
                    holder.binding.txtUsername.text = getString(R.string.name)
                    holder.binding.txtMahmoudalyosifyOne.text = getItem(position).toString()
                    holder.binding.txtOne.text = position.toString()
                }
                1 -> {
                    holder.binding.txtUsername.text = getString(R.string.email)
                    holder.binding.txtMahmoudalyosifyOne.text = getItem(position).toString()
                    holder.binding.txtOne.text = position.toString()
                }
                2 -> {
                    holder.binding.txtUsername.text = getString(R.string.lbl_age)
                    holder.binding.txtMahmoudalyosifyOne.text = getItem(position).toString()
                    holder.binding.txtOne.text = position.toString()
                }
                3 -> {
                    holder.binding.txtUsername.text = "gender"
                    holder.binding.txtMahmoudalyosifyOne.text = getItem(position).toString()
                    holder.binding.txtOne.text = position.toString()
                }
                4 -> {
                    holder.binding.txtUsername.text = "Blood type"
                    holder.binding.txtMahmoudalyosifyOne.text = getItem(position).toString()
                    holder.binding.txtOne.text = position.toString()
                }
                5 -> {
                    holder.binding.txtUsername.text = "phone number"
                    holder.binding.txtMahmoudalyosifyOne.text = getItem(position).toString()
                    holder.binding.txtOne.text = position.toString()
                }
                6 -> {
                    holder.binding.txtUsername.text = "country"
                    holder.binding.txtMahmoudalyosifyOne.text = getItem(position).toString()
                    holder.binding.txtOne.text = position.toString()
                }
            }
        }

        inner class ViewHolder(val binding: ResultMeasurmentRecycleItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

        }

    }
}