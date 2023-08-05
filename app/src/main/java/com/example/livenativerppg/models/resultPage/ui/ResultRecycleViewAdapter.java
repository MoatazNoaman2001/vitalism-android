package com.example.livenativerppg.models.resultPage.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.livenativerppg.databinding.ResultMeasurmentRecycleItemBinding;
import com.example.livenativerppg.models.resultPage.data.model.ResultRecycleViewmodel;

import java.util.ArrayList;

public class ResultRecycleViewAdapter extends RecyclerView.Adapter<ResultRecycleViewAdapter.ViewHolder> {
    private ArrayList<ResultRecycleViewmodel> resultRecycleViewmodels;

    public ResultRecycleViewAdapter(ArrayList<ResultRecycleViewmodel> resultRecycleViewmodels) {
        this.resultRecycleViewmodels = resultRecycleViewmodels;
    }

    @NonNull
    @Override
    public ResultRecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ResultMeasurmentRecycleItemBinding.inflate(LayoutInflater.from(parent.getContext())
                , parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ResultRecycleViewAdapter.ViewHolder holder, int position) {
        holder.bindView(resultRecycleViewmodels.get(position));
    }

    @Override
    public int getItemCount() {
        return resultRecycleViewmodels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ResultMeasurmentRecycleItemBinding binding;

        public ViewHolder(ResultMeasurmentRecycleItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindView(ResultRecycleViewmodel resultRecycleViewmodel) {
            binding.txtOne.setText(String.valueOf(resultRecycleViewmodel.getNum()));
            binding.txtUsername.setText(resultRecycleViewmodel.getText1());
            binding.txtMahmoudalyosifyOne.setText(resultRecycleViewmodel.getText2());
        }
    }
}
