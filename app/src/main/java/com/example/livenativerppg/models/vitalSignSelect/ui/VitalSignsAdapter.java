package com.example.livenativerppg.models.vitalSignSelect.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.example.livenativerppg.R;
import com.example.livenativerppg.databinding.RowListfavoriteBinding;
import com.example.livenativerppg.models.vitalSignSelect.data.model.ListfavoriteRowModel;
import com.google.android.material.imageview.ShapeableImageView;

public class VitalSignsAdapter extends RecyclerView.Adapter<VitalSignsAdapter.ViewHolder> {

    private final boolean is_top;
    private final Fragment fragment;
    private final RequestManager imageLoader;
    private onClickListener clickListener;

    public VitalSignsAdapter(Fragment fragment, boolean is_top, RequestManager imageLoader, onClickListener clickListener) {
        this.is_top = is_top;
        this.fragment = fragment;
        this.imageLoader = imageLoader;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public VitalSignsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(RowListfavoriteBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VitalSignsAdapter.ViewHolder holder, int position) {
        holder.binding.setListfavoriteRowModel(new ListfavoriteRowModel());
        holder.binding.txtTitle.setTransitionName(holder.binding.txtTitle.getTransitionName() + "_" + position);
        holder.binding.txtSubTitle.setTransitionName(holder.binding.txtSubTitle.getTransitionName() + "_" + position);
        holder.binding.img.setTransitionName(holder.binding.img.getTransitionName() + "_" + position);


        switch (position) {
            case 0:
                holder.bindHeartRate();
                holder.binding.getRoot().setOnClickListener(v -> {
                    clickListener.onCick(holder.binding.img,
                            holder.binding.txtTitle,
                            holder.binding.txtSubTitle,
                            position
                    );
                });
                break;
            case 1:
                holder.bindHeartRateVariability(fragment.requireContext());
                holder.binding.getRoot().setOnClickListener(v -> {
                    clickListener.onCick(holder.binding.img,
                            holder.binding.txtTitle,
                            holder.binding.txtSubTitle,
                            position
                    );
                });
                break;
            case 2:
                holder.bindRespiratoryRate();
                holder.binding.getRoot().setOnClickListener(v -> {
                    clickListener.onCick(holder.binding.img,
                            holder.binding.txtTitle,
                            holder.binding.txtSubTitle,
                            position
                    );
                });
                break;
            case 3:
                holder.bindOxygenSaturation();
                holder.binding.getRoot().setOnClickListener(v -> {
                    clickListener.onCick(holder.binding.img,
                            holder.binding.txtTitle,
                            holder.binding.txtSubTitle,
                            position
                    );
                });
                break;
            case 4:
                holder.bindBloodPressure(fragment.requireContext());
                holder.binding.getRoot().setOnClickListener(v -> {
                    clickListener.onCick(holder.binding.img,
                            holder.binding.txtTitle,
                            holder.binding.txtSubTitle,
                            position
                    );
                });
                break;
            case 5:
                holder.bindPulseRespiratoryQuotient();
                holder.binding.getRoot().setOnClickListener(v -> {
                    clickListener.onCick(holder.binding.img,
                            holder.binding.txtTitle,
                            holder.binding.txtSubTitle,
                            position
                    );
                });
                break;
        }


    }

    @Override
    public int getItemCount() {
        return 6;
    }

    public interface onClickListener {
        void onCick(AppCompatImageView imageView, AppCompatTextView textView1, AppCompatTextView textView2, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final RowListfavoriteBinding binding;

        public ViewHolder(RowListfavoriteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindHeartRate() {
            binding.getListfavoriteRowModel().setTxtHeartRate(fragment.getString(R.string.lbl_heart_rate));
            binding.getListfavoriteRowModel().setTxtAnormalrestin(fragment.getString(R.string.msg_a_normal_restin));
        }

        public void bindHeartRateVariability(Context context) {
            imageLoader.asBitmap().load(context.getString(R.string.heart_rate_veriability_uri)).centerCrop().into(binding.img);
            binding.getListfavoriteRowModel().setTxtHeartRate(fragment.getString(R.string.msg_heart_rate_vari3));
            binding.getListfavoriteRowModel().setTxtAnormalrestin(fragment.getString(R.string.msg_heart_rate_vari2));
        }

        public void bindRespiratoryRate() {
            //img_heartratemin_92x153
            imageLoader.asBitmap().load(R.drawable.img_heartratemin_92x153).into(binding.img);
            binding.getListfavoriteRowModel().setTxtHeartRate(fragment.getString(R.string.msg_respiratory_rat));
            binding.getListfavoriteRowModel().setTxtAnormalrestin(fragment.getString(R.string.msg_normal_respirat));
        }

        public void bindOxygenSaturation() {
            //img_image3_92x154
            imageLoader.asBitmap().load(R.drawable.img_image3_92x154).into(binding.img);
            binding.getListfavoriteRowModel().setTxtHeartRate(fragment.getString(R.string.msg_oxygen_saturati));
            binding.getListfavoriteRowModel().setTxtAnormalrestin(fragment.getString(R.string.msg_a_normal_pulse));
        }

        public void bindBloodPressure(Context context) {
            imageLoader.asBitmap().load(context.getString(R.string.blood_pressure_uri)).into(binding.img);
            binding.getListfavoriteRowModel().setTxtHeartRate(fragment.getString(R.string.lbl_blood_pressure));
            binding.getListfavoriteRowModel().setTxtAnormalrestin(fragment.getString(R.string.msg_healthy_and_unh));
        }

        public void bindPulseRespiratoryQuotient() {
            binding.getListfavoriteRowModel().setTxtHeartRate(fragment.getString(R.string.msg_pulse_respirati));
            binding.getListfavoriteRowModel().setTxtAnormalrestin(fragment.getString(R.string.msg_heart_rate_als2));
        }
    }
}
