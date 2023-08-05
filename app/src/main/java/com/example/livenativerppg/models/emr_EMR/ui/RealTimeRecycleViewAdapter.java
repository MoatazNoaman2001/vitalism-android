package com.example.livenativerppg.models.emr_EMR.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.livenativerppg.component.natives.RPPGResult;
import com.example.livenativerppg.databinding.ItemTimelineBinding;
import com.github.vipulasri.timelineview.TimelineView;

public class RealTimeRecycleViewAdapter extends ListAdapter<RPPGResult , RealTimeRecycleViewAdapter.ViewHolder> {
    protected RealTimeRecycleViewAdapter(@NonNull DiffUtil.ItemCallback<RPPGResult> diffCallback) {
        super(new RealTimeItemDiffUtil());
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position, getItemCount());
    }

    @NonNull
    @Override
    public RealTimeRecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemTimelineBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RealTimeRecycleViewAdapter.ViewHolder holder, int position) {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemTimelineBinding binding;
        public ViewHolder(ItemTimelineBinding binding , int viewType) {
            super(binding.getRoot());
            binding.timeline.initLine(viewType);
            this.binding = binding;
        }
    }


}
class RealTimeItemDiffUtil extends DiffUtil.ItemCallback<RPPGResult>{

    @Override
    public boolean areItemsTheSame(@NonNull RPPGResult oldItem, @NonNull RPPGResult newItem) {
        return oldItem.equals(newItem);
    }

    @Override
    public boolean areContentsTheSame(@NonNull RPPGResult oldItem, @NonNull RPPGResult newItem) {
        return false;
    }
}
