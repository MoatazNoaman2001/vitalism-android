package com.example.livenativerppg.models.emr_EMR.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.livenativerppg.component.natives.BPRPPGResult;
import com.example.livenativerppg.component.natives.RPPGResult;
import com.example.livenativerppg.databinding.BloodPressureItemTimlineBinding;
import com.example.livenativerppg.databinding.ItemTimelineBinding;
import com.github.vipulasri.timelineview.TimelineView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BloodPressureTimeLineAdapter extends RecyclerView.Adapter<BloodPressureTimeLineAdapter.TimeLineViewHolder> {
    ArrayList<BPRPPGResult> results;
    private ViewPager2 viewPager2;
    private String date, time;

    public BloodPressureTimeLineAdapter(ArrayList<BPRPPGResult> results, String date, String time, ViewPager2 viewPager2) {
        this.results = results;
        this.viewPager2 = viewPager2;
        this.date = date;
        this.time = time;
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position, getItemCount());
    }

    @NonNull
    @Override
    public TimeLineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TimeLineViewHolder(BloodPressureItemTimlineBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull BloodPressureTimeLineAdapter.TimeLineViewHolder holder, int position) {
        holder.binding.dateTextView.setText(
                new SimpleDateFormat("hh:mm:ss a',' dd-EEE").format(new Date(results.get(position).getDate()))
        );

        BPRPPGResult result = results.get(position);
        holder.binding.resultTxtView.setText("result: " + result.getSp() + "/" + result.getDp() + " BP");

        holder.binding.getRoot().setOnClickListener(v -> {
//            if (viewPager2 != null)
//                ((EMRViewPagerAdapter) viewPager2.getAdapter()).addFragment(EmrSecondLevelFragment.Companion.getInstance(date, time));
        });

    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public static class TimeLineViewHolder extends RecyclerView.ViewHolder {
        BloodPressureItemTimlineBinding binding;

        public TimeLineViewHolder(BloodPressureItemTimlineBinding binding, int viewType) {
            super(binding.getRoot());
            binding.timeline.initLine(viewType);
            this.binding = binding;
        }
    }

}