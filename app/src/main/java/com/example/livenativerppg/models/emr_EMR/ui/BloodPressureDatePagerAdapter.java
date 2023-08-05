package com.example.livenativerppg.models.emr_EMR.ui;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagingData;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.livenativerppg.component.natives.BPRPPGResult;
import com.example.livenativerppg.component.natives.RPPGResult;
import com.example.livenativerppg.databinding.DateStingTreeViewEmrRecycleViewItemBinding;
import com.github.vipulasri.timelineview.TimelineView;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;

public class BloodPressureDatePagerAdapter extends PagingDataAdapter<ImmutablePair<String, ArrayList<ImmutablePair<String, ArrayList<BPRPPGResult>>>>, BloodPressureDatePagerAdapter.BloodPressureDatePagerViewHolder> {
    private static final String TAG = "DateStringEMRRecycleAda";
    //    private Flowable<PagingData<ImmutablePair<String, ArrayList<ImmutablePair<String, ArrayList<RPPGResult>>>>> flowable = Flowable.empty();
    private ViewPager2 viewPager2;

    public BloodPressureDatePagerAdapter(ViewPager2 viewPager2) {
        super(new BloodPressureDateItemDiffUtils());
        this.viewPager2 = viewPager2;
    }

    void setData(PagingData<ImmutablePair<String, ArrayList<ImmutablePair<String, ArrayList<RPPGResult>>>>> data){

    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position, getItemCount());
    }


    @NonNull
    @Override
    public BloodPressureDatePagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BloodPressureDatePagerViewHolder(DateStingTreeViewEmrRecycleViewItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull BloodPressureDatePagerViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: " + getItem(position));
        if (getItem(position) == null) return;
        holder.binding.dateStringTxtView.setText(getItem(position).left.split(" ")[1] + " " + getItem(position).left.split(" ")[0]);
        holder.binding.timeRecycleView.setAdapter(new BloodPressureParentTimeRecycleAdapter(getItem(position).right , getItem(position).left , viewPager2));
    }

    public static class BloodPressureDatePagerViewHolder extends RecyclerView.ViewHolder {
        DateStingTreeViewEmrRecycleViewItemBinding binding;

        public BloodPressureDatePagerViewHolder(DateStingTreeViewEmrRecycleViewItemBinding binding, int viewType) {
            super(binding.getRoot());
            binding.timeline.initLine(viewType);
            this.binding = binding;
        }
    }


}

class BloodPressureDateItemDiffUtils extends DiffUtil.ItemCallback<ImmutablePair<String, ArrayList<ImmutablePair<String, ArrayList<BPRPPGResult>>>>> {
    private static final String TAG = "ItemDiffUtils";

    @Override
    public boolean areItemsTheSame(@NonNull ImmutablePair<String, ArrayList<ImmutablePair<String, ArrayList<BPRPPGResult>>>> oldItem, @NonNull ImmutablePair<String, ArrayList<ImmutablePair<String, ArrayList<BPRPPGResult>>>> newItem) {
        Log.d(TAG, "areItemsTheSame: oldItem left: " + oldItem.left + "  newItem left: " + newItem.left + "  is same: " + oldItem.left.equals(newItem.left));
        return oldItem.left.equals(newItem.left);
    }

    @Override
    public boolean areContentsTheSame(@NonNull ImmutablePair<String, ArrayList<ImmutablePair<String, ArrayList<BPRPPGResult>>>> oldItem, @NonNull ImmutablePair<String, ArrayList<ImmutablePair<String, ArrayList<BPRPPGResult>>>> newItem) {
        Log.d(TAG, "areItemsTheSame: oldItem right: " + oldItem.right + "  newItem right: " + newItem.right + "  is same: " + oldItem.right.equals(newItem.right));
        return true;
    }
}