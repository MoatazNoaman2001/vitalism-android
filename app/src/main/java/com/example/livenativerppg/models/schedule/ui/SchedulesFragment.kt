package com.example.livenativerppg.models.schedule.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.livenativerppg.R
import com.example.livenativerppg.component.base.BaseFragment
import com.example.livenativerppg.databinding.FragmentMedicineBinding
import com.example.livenativerppg.databinding.MedicineListItemBinding
import com.example.livenativerppg.models.schedule.data.model.Medicine
import com.example.livenativerppg.models.schedule.data.viewmodel.ScheduleVM
import com.michalsvec.singlerowcalendar.calendar.CalendarChangesObserver
import com.michalsvec.singlerowcalendar.calendar.CalendarViewManager
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendarAdapter
import com.michalsvec.singlerowcalendar.selection.CalendarSelectionManager
import dagger.hilt.android.AndroidEntryPoint
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DateUtils
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "SchedulesFragment"
@AndroidEntryPoint
class SchedulesFragment : BaseFragment<FragmentMedicineBinding>(R.layout.fragment_medicine) {
    lateinit var controller: NavController
    private val viewModel: ScheduleVM by viewModels()
    lateinit var adapter: ScheduleMedicineAdapter
    lateinit var selectedDate: Date

    override fun onInitialized() {
        super.onInitialized()
        binding.scheduleVM = viewModel
        controller = Navigation.findNavController(requireView());
        adapter = ScheduleMedicineAdapter()
        binding.schedulRecycleView.adapter = adapter;
        NavigationUI.setupWithNavController(binding.schedulerToolbar , controller)
        binding.schedulerToolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24)
        binding.schedulerToolbar.setNavigationIconTint(ResourcesCompat.getColor(resources , R.color.blue_A200, requireActivity().theme))
        binding.measuredVitalSignsTextView.isVisible = false

        selectedDate = Calendar.getInstance().time

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val myCalendarViewManager = object : CalendarViewManager {
            override fun setCalendarViewResourceId(
                position: Int,
                date: Date,
                isSelected: Boolean,
            ): Int {
                return if (isSelected)
                    R.layout.selected_day_layout
                else
                    R.layout.unselected_day_layout
            }

            override fun bindDataToCalendarView(
                holder: SingleRowCalendarAdapter.CalendarViewHolder,
                date: Date,
                position: Int,
                isSelected: Boolean,
            ) {
//                DateUtils.get()
//                val current = Calendar.getInstance().time
//                if (SimpleDateFormat("EEE MM/yyyy").format(current).equals(
//                        SimpleDateFormat("EEE MM/yyyy").format(date)
//                ))
//                    isSelected =true
                if (!isSelected) {
                    holder.itemView.findViewById<AppCompatTextView>(R.id.DayAbbriv).text =
                        SimpleDateFormat("EEE").format(date)
                    holder.itemView.findViewById<AppCompatTextView>(R.id.DayInNum).text =
                        SimpleDateFormat("dd").format(date)
                }else{
                    holder.itemView.findViewById<AppCompatTextView>(R.id.DayAbbriv).text =
                        SimpleDateFormat("EEE").format(date)
                    holder.itemView.findViewById<AppCompatTextView>(R.id.DayInNum).text =
                        SimpleDateFormat("dd").format(date)

                }

            }
        }

        val mySelectionManager = object : CalendarSelectionManager {
            override fun canBeItemSelected(position: Int, date: Date): Boolean {
                return true
            }
        }

        val myCalendarChangesObserver = object : CalendarChangesObserver {
            override fun whenWeekMonthYearChanged(
                weekNumber: String,
                monthNumber: String,
                monthName: String,
                year: String,
                date: Date,
            ) {
                super.whenWeekMonthYearChanged(weekNumber, monthNumber, monthName, year, date)
            }

            override fun whenSelectionChanged(isSelected: Boolean, position: Int, date: Date) {
                super.whenSelectionChanged(isSelected, position, date)
                binding.currentDateTextView.text = SimpleDateFormat("dd MMMM").format(date)
                selectedDate = date

                viewModel.medicines.observe(viewLifecycleOwner){
                    Log.d(TAG, "onViewCreated: ${it.joinToString { it.toString() }}")
                    val currentDay = SimpleDateFormat("EEEE").format(selectedDate)
                    Log.d(TAG, "whenSelectionChanged:current day: $currentDay , days: ${it.map { it.days.joinToString { it } }}")
                    val currentDayList = it.filter { it.days.any { StringUtils.equalsAnyIgnoreCase(it , currentDay) } || it.days.size == 7 }.toList()
                    adapter.submitList(currentDayList)
                }
            }

            override fun whenCalendarScrolled(dx: Int, dy: Int) {
                super.whenCalendarScrolled(dx, dy)
            }

            override fun whenSelectionRestored() {
                super.whenSelectionRestored()
            }

            override fun whenSelectionRefreshed() {
                super.whenSelectionRefreshed()
            }

        }

        val singleRowCalendar = binding.mainSingleRowCalendar.apply {
            calendarViewManager = myCalendarViewManager
            calendarChangesObserver = myCalendarChangesObserver
            calendarSelectionManager = mySelectionManager
            futureDaysCount = 30
            pastDaysCount = 30
            includeCurrentDate = true
            init()
        }

        singleRowCalendar.select(30)
        singleRowCalendar.scrollToPosition(30)
    }

    override fun addObservers() {
        super.addObservers()

    }

    override fun setUpClicks() {
        binding.schedulerToolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.AddSchedulesFragment){
                controller.navigate(R.id.action_scheduleFragment_to_addScheduleFragment)
            }
            true
        }
    }

    inner class ScheduleMedicineAdapter : ListAdapter<Medicine, ScheduleMedicineAdapter.ViewHolder>(ScheduleMedicineItemDiff()) {
        inner class ViewHolder(val binding: MedicineListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): ScheduleMedicineAdapter.ViewHolder {
            return ViewHolder(MedicineListItemBinding.inflate(LayoutInflater.from(parent.context) , parent, false))
        }

        override fun onBindViewHolder(holder: ScheduleMedicineAdapter.ViewHolder, position: Int) {
            val medicine = getItem(position);
            holder.binding.txtName.text = medicine.name
            holder.binding.txtDisc.text = medicine.disc
            holder.binding.txtDate.text = "${SimpleDateFormat("dd MMMM yyyy").format(Calendar.getInstance().time)} ${medicine.hours}:${medicine.minute}"
        }

    }

    inner class ScheduleMedicineItemDiff: DiffUtil.ItemCallback<Medicine>(){
        override fun areItemsTheSame(oldItem: Medicine, newItem: Medicine): Boolean {
            return oldItem.name.equals(newItem.name)
        }

        override fun areContentsTheSame(oldItem: Medicine, newItem: Medicine): Boolean {
            return true
        }

    }
}