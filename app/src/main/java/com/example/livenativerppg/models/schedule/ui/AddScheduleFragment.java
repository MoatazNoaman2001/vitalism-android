package com.example.livenativerppg.models.schedule.ui;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.dpro.widgets.WeekdaysPicker;
import com.example.livenativerppg.R;
import com.example.livenativerppg.component.base.BaseFragment;
import com.example.livenativerppg.databinding.FragmentAddScheduleBinding;
import com.example.livenativerppg.models.schedule.data.model.Medicine;
import com.example.livenativerppg.models.schedule.data.viewmodel.ScheduleVM;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;



@AndroidEntryPoint
public class AddScheduleFragment extends BaseFragment<FragmentAddScheduleBinding> {
    private static final String TAG = "AddScheduleFragment";
    private ScheduleVM scheduleVM;
    private List<String> selectedDays;
    private int hours , min;
    private String emergency , medicine;
    private NavController controller;
    public AddScheduleFragment() {
        super(R.layout.fragment_add_schedule);
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        selectedDays = new ArrayList<>();
        scheduleVM = new ViewModelProvider(this).get(ScheduleVM.class);
        controller = Navigation.findNavController(requireView());

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                new ArrayList<String>(){{add("low");add("medium");add("high");}}
        );

        binding.emergecyEditText.setAdapter(arrayAdapter);

    }

    @Override
    public void addObservers() {
        super.addObservers();

        binding.timePacker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            Log.d(TAG, "addObservers: " + view.getHour() + "  " + hourOfDay);
            hours = view.getHour();
            min = view.getMinute();
        });
        binding.weekDays.setOnWeekdaysChangeListener((view, clickedDayOfWeek, selectedDays) -> {
            binding.weekDayTextView.setText(binding.weekDays.getSelectedDaysText().stream().collect(Collectors.joining(", ")));
        });
        binding.weekDays.setOnWeekRecurrenceChangeListener((view, selectedDays, even_week) -> {
            switch (even_week) {
                case WeekdaysPicker.ALL:
                    binding.weekDayTextView.setText("Every Day");
                    break;
            }
        });
    }

    @Override
    public void setUpClicks() {
        binding.addBtn.setOnClickListener(v -> {
            medicine = binding.MedicineTextInputLayout.getEditText().getText().toString();
            if (medicine.isEmpty()){
                Toast.makeText(requireContext(), "must type medicine name", Toast.LENGTH_SHORT).show();
                return;
            }else if (hours == 0 && min == 0){
                Toast.makeText(requireContext(), "must select period to take", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedDays = binding.weekDays.getSelectedDaysText();
            emergency = binding.EmergencyLevelTextInputLayout.getEditText().getText().toString();

            Medicine medicine1 = new Medicine(
                    medicine ,0.0f , "" , new ArrayList<>(selectedDays) , hours , min , new Date(0) , new Date(0), 1
            );

            scheduleVM.insertMedicine(medicine1, controller );

        });
    }
}