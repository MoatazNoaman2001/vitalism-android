package com.example.livenativerppg.models.UserFollowedDetails.ui;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagingData;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.livenativerppg.component.natives.RPPGResult;
import com.example.livenativerppg.databinding.DateStingTreeViewEmrRecycleViewItemBinding;
import com.example.livenativerppg.models.emr_EMR.ui.ParentTimeRecycleAdapter;
import com.github.vipulasri.timelineview.TimelineView;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;


public class DateStringEMRRecycleListAdapter extends ListAdapter<ImmutablePair<String, ArrayList<ImmutablePair<String, ArrayList<RPPGResult>>>>, DateStringEMRRecycleListAdapter.DateStringEMRRecycleViewViewHolder> {
    private static final String TAG = "DateStringEMRRecycleAda";
//    private Flowable<PagingData<ImmutablePair<String, ArrayList<ImmutablePair<String, ArrayList<RPPGResult>>>>> flowable = Flowable.empty();
    private ViewPager2 viewPager2;

    public DateStringEMRRecycleListAdapter(ViewPager2 viewPager2) {
        super(new DateItemDiffUtils());
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
    public DateStringEMRRecycleViewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DateStringEMRRecycleViewViewHolder(DateStingTreeViewEmrRecycleViewItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull DateStringEMRRecycleViewViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: " + getItem(position));

        if (getItem(position) == null) return;
        holder.binding.dateStringTxtView.setText(getItem(position).left.split(" ")[1] + " " + getItem(position).left.split(" ")[0]);
        holder.binding.timeRecycleView.setAdapter(new ParentTimeRecycleAdapter(getItem(position).right , getItem(position).left , viewPager2));
    }


    public static class DateStringEMRRecycleViewViewHolder extends RecyclerView.ViewHolder {
        DateStingTreeViewEmrRecycleViewItemBinding binding;

        public DateStringEMRRecycleViewViewHolder(DateStingTreeViewEmrRecycleViewItemBinding binding, int viewType) {
            super(binding.getRoot());
            binding.timeline.initLine(viewType);
            this.binding = binding;
        }
    }


}
class DateItemDiffUtils extends DiffUtil.ItemCallback<ImmutablePair<String, ArrayList<ImmutablePair<String, ArrayList<RPPGResult>>>>> {
    private static final String TAG = "ItemDiffUtils";

    @Override
    public boolean areItemsTheSame(@NonNull ImmutablePair<String, ArrayList<ImmutablePair<String, ArrayList<RPPGResult>>>> oldItem, @NonNull ImmutablePair<String, ArrayList<ImmutablePair<String, ArrayList<RPPGResult>>>> newItem) {
        Log.d(TAG, "areItemsTheSame: oldItem left: " + oldItem.left + "  newItem left: " + newItem.left + "  is same: " + oldItem.left.equals(newItem.left));
        return oldItem.left.equals(newItem.left);
    }

    @Override
    public boolean areContentsTheSame(@NonNull ImmutablePair<String, ArrayList<ImmutablePair<String, ArrayList<RPPGResult>>>> oldItem, @NonNull ImmutablePair<String, ArrayList<ImmutablePair<String, ArrayList<RPPGResult>>>> newItem) {
        Log.d(TAG, "areItemsTheSame: oldItem right: " + oldItem.right + "  newItem right: " + newItem.right + "  is same: " + oldItem.right.equals(newItem.right));
        return true;
    }
}
