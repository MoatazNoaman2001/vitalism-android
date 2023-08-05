package com.example.livenativerppg.models.emr_EMR.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.livenativerppg.component.natives.RPPGResult;
import com.example.livenativerppg.databinding.ItemTimelineBinding;
import com.github.vipulasri.timelineview.TimelineView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TimeLineAdapter extends RecyclerView.Adapter<TimeLineAdapter.TimeLineViewHolder> {
    ArrayList<RPPGResult> results;
    private ViewPager2 viewPager2;
    private String date, time;

    public TimeLineAdapter(ArrayList<RPPGResult> results, String date, String time, ViewPager2 viewPager2) {
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
        return new TimeLineViewHolder(ItemTimelineBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeLineViewHolder holder, int position) {
        holder.binding.dateTextView.setText(
                new SimpleDateFormat("hh:mm:ss a',' dd-EEE").format(new Date(results.get(position).getTime()))
        );

        holder.binding.maxTxtView.setText("Max: " + Math.ceil(results.get(position).getMax()));
        holder.binding.meanTxtView.setText("Mean: " + Math.ceil(results.get(position).getMean()));
        holder.binding.minTxtView.setText("Min: " + Math.ceil(results.get(position).getMin()));

    holder.binding.getRoot().setOnClickListener(v -> {
            if (viewPager2 != null)
                ((EMRViewPagerAdapter) viewPager2.getAdapter()).addFragment(EmrSecondLevelFragment.Companion.getInstance(date, time));
        });

    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public static class TimeLineViewHolder extends RecyclerView.ViewHolder {
        ItemTimelineBinding binding;

        public TimeLineViewHolder(ItemTimelineBinding binding, int viewType) {
            super(binding.getRoot());
            binding.timeline.initLine(viewType);
            this.binding = binding;
        }
    }

}
