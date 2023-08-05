package com.example.livenativerppg.models.emr_EMR.ui;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.livenativerppg.component.natives.BPRPPGResult;
import com.example.livenativerppg.component.natives.RPPGResult;
import com.example.livenativerppg.databinding.DateStingTreeViewEmrRecycleViewItemBinding;
import com.github.vipulasri.timelineview.TimelineView;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;

public class BloodPressureParentTimeRecycleAdapter extends RecyclerView.Adapter<BloodPressureParentTimeRecycleAdapter.ViewHolder> {
    private static final String TAG = "ParentTimeRecycleAdapte";
    ArrayList<ImmutablePair<String, ArrayList<BPRPPGResult>>> pairs;
    private String date;
    private ViewPager2 viewPager2;

    public BloodPressureParentTimeRecycleAdapter(ArrayList<ImmutablePair<String, ArrayList<BPRPPGResult>>> right, String date, ViewPager2 viewPager2) {
        pairs = right;
        this.date = date;
        this.viewPager2 = viewPager2;
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position, getItemCount());
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(DateStingTreeViewEmrRecycleViewItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull BloodPressureParentTimeRecycleAdapter.ViewHolder holder, int position) {
        holder.binding.dateStringTxtView.setText(pairs.get(position).left);
        try {
            BPRPPGResult rppgResult = new BPRPPGResult(
                    (int) pairs.get(position).right.stream().mapToInt(it -> it.getSp()).average().getAsDouble(),
                    (int) pairs.get(position).right.stream().mapToInt(it -> it.getDp()).average().getAsDouble(),
                    pairs.get(position).right.stream().mapToLong(it -> it.getDate()).max().getAsLong()
            );
            holder.binding.timeRecycleView.setAdapter(new BloodPressureTimeLineAdapter(new ArrayList<BPRPPGResult>() {{
                add(rppgResult);
            }}, date, pairs.get(position).left, viewPager2));

        } catch (Exception e) {
            Log.d(TAG, "onBindViewHolder: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return pairs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        DateStingTreeViewEmrRecycleViewItemBinding binding;

        public ViewHolder(DateStingTreeViewEmrRecycleViewItemBinding binding, int viewType) {
            super(binding.getRoot());
            this.binding = binding;
            binding.timeline.initLine(viewType);
        }
    }
}