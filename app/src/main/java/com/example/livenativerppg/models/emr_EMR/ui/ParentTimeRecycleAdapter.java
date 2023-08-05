package com.example.livenativerppg.models.emr_EMR.ui;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.livenativerppg.component.natives.RPPGResult;
import com.example.livenativerppg.databinding.DateStingTreeViewEmrRecycleViewItemBinding;
import com.github.vipulasri.timelineview.TimelineView;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;

public class ParentTimeRecycleAdapter extends RecyclerView.Adapter<ParentTimeRecycleAdapter.ViewHolder> {
    private static final String TAG = "ParentTimeRecycleAdapte";
    ArrayList<ImmutablePair<String, ArrayList<RPPGResult>>> pairs;
    private String date;
    private ViewPager2 viewPager2;

    public ParentTimeRecycleAdapter(ArrayList<ImmutablePair<String, ArrayList<RPPGResult>>> right, String date, ViewPager2 viewPager2) {
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
    public ParentTimeRecycleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(DateStingTreeViewEmrRecycleViewItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ParentTimeRecycleAdapter.ViewHolder holder, int position) {
        holder.binding.dateStringTxtView.setText(pairs.get(position).left);
        try {
            RPPGResult rppgResult = new RPPGResult(
                    pairs.get(position).right.get(pairs.get(position).right.size() - 1).getTime(),
                    pairs.get(position).right.stream().mapToDouble(RPPGResult::getMean).average().getAsDouble(),
                    pairs.get(position).right.stream().mapToDouble(RPPGResult::getMin).average().getAsDouble(),
                    pairs.get(position).right.stream().mapToDouble(RPPGResult::getMax).average().getAsDouble()
            );
            holder.binding.timeRecycleView.setAdapter(new TimeLineAdapter(new ArrayList<RPPGResult>() {{
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
