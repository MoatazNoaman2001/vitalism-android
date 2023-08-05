package com.example.livenativerppg.models.searchMain.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagingData;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.livenativerppg.component.natives.RPPGResult;
import com.example.livenativerppg.databinding.DateStingTreeViewEmrRecycleViewItemBinding;
import com.example.livenativerppg.databinding.EmrSearchRecycleViewItemBinding;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.ArrayList;

import kotlinx.coroutines.flow.Flow;

public class SearchEmrRecycleViewAdapter extends PagingDataAdapter<ImmutableTriple<String , String , ArrayList<RPPGResult>> , SearchEmrRecycleViewAdapter.ViewHolder> {

    public SearchEmrRecycleViewAdapter() {
        super(new DiffUtil.ItemCallback<ImmutableTriple<String , String , ArrayList<RPPGResult>>>() {
            @Override
            public boolean areItemsTheSame(@NonNull ImmutableTriple<String , String , ArrayList<RPPGResult>> oldItem, @NonNull ImmutableTriple<String , String , ArrayList<RPPGResult>> newItem) {
                return oldItem.left.equals(newItem.left);

            }

            @Override
            public boolean areContentsTheSame(@NonNull ImmutableTriple<String , String , ArrayList<RPPGResult>> oldItem, @NonNull ImmutableTriple<String , String , ArrayList<RPPGResult>> newItem) {
                return true;
            }
        });
    }

    @NonNull
    @Override
    public SearchEmrRecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(EmrSearchRecycleViewItemBinding.inflate(LayoutInflater.from(parent.getContext()) , parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SearchEmrRecycleViewAdapter.ViewHolder holder, int position) {
        ImmutableTriple<String , String , ArrayList<RPPGResult>> emr_res = getItem(position);
        holder.binding.DateTextView.setText(emr_res.left.split(" ")[1] + " " + emr_res.left.split(" ")[0]);
        holder.binding.TimeTextView.setText(emr_res.middle);
        holder.binding.MaxBeat.setText(String.valueOf(emr_res.right.stream().mapToInt(rppgResult -> (int) rppgResult.getMax()).average().getAsDouble()));
        holder.binding.meanBeat.setText(String.valueOf(emr_res.right.stream().mapToInt(rppgResult -> (int) rppgResult.getMean()).average().getAsDouble()));
        holder.binding.minBeat.setText(String.valueOf(emr_res.right.stream().mapToInt(rppgResult -> (int) rppgResult.getMin()).average().getAsDouble()));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        EmrSearchRecycleViewItemBinding binding;
        public ViewHolder(EmrSearchRecycleViewItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
