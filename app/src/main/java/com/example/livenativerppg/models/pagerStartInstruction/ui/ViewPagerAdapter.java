package com.example.livenativerppg.models.pagerStartInstruction.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.livenativerppg.databinding.FourthScreenBinding;
import com.example.livenativerppg.databinding.ScreenOneBinding;
import com.example.livenativerppg.databinding.SecondScreenBinding;
import com.example.livenativerppg.databinding.ThirdScreenBinding;

public class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewHolder> {


    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public ViewPagerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                return new ViewHolder(ScreenOneBinding.inflate(LayoutInflater.from(parent.getContext())
                        , parent, false));

            case 1:
                return new ViewHolder(SecondScreenBinding.inflate(LayoutInflater.from(parent.getContext())
                        , parent, false));

            case 2:
                return new ViewHolder(ThirdScreenBinding.inflate(LayoutInflater.from(parent.getContext())
                        , parent, false));
            default:
                return new ViewHolder(FourthScreenBinding.inflate(LayoutInflater.from(parent.getContext())
                        , parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewPagerAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 4;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(ScreenOneBinding binding) {
            super(binding.getRoot());
        }
        public ViewHolder(SecondScreenBinding binding) {
            super(binding.getRoot());
        }

        public ViewHolder(ThirdScreenBinding binding) {
            super(binding.getRoot());
        }
        public ViewHolder(FourthScreenBinding binding) {
            super(binding.getRoot());
        }

    }
}
