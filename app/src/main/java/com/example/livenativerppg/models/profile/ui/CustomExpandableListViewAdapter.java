package com.example.livenativerppg.models.profile.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.livenativerppg.R;
import com.example.livenativerppg.databinding.ExpandableProfileListItemBinding;
import com.example.livenativerppg.models.profile.data.model.ExpandableItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomExpandableListViewAdapter extends RecyclerView.Adapter<CustomExpandableListViewAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ExpandableItem> titles;
    private ArrayList<ExpandableItem> details;
    private OnItemClickListener clickListener;

    public CustomExpandableListViewAdapter(Context context, ArrayList<ExpandableItem> details, OnItemClickListener clickListener) {
        this.context = context;
        this.details = details;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public CustomExpandableListViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ExpandableProfileListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CustomExpandableListViewAdapter.ViewHolder holder, int position) {
        holder.binding.expandableTexView.setText(details.get(position).getTitle());
        holder.binding.expandableTexView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                ContextCompat.getDrawable(context, details.get(position).getStartIcon()),
                null,
                tintDrawable(ContextCompat.getDrawable(context, details.get(position).getEndIcon())),
                null
        );

        holder.binding.getRoot().setOnClickListener(v -> {
            holder.binding.expandableTexView.setTransitionName(
                    holder.binding.expandableTexView.getTransitionName() + "_" + position
            );
            clickListener.clickOn(holder.binding.expandableTexView, position);
        });
    }

    private Drawable tintDrawable(Drawable drawable) {
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(context, R.color.gray_500));
        return drawable;
    }

    @Override
    public int getItemCount() {
        return details.size();
    }

    interface OnItemClickListener {
        public void clickOn(AppCompatTextView textView , int pos);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ExpandableProfileListItemBinding binding;

        public ViewHolder(ExpandableProfileListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

//    @Override
//    public int getGroupCount() {
//        return titles.size();
//    }
//
//    @Override
//    public int getChildrenCount(int groupPosition) {
//        return details.get(titles.get(groupPosition)).size();
//    }
//
//    @Override
//    public Object getGroup(int groupPosition) {
//        return titles.get(groupPosition);
//    }
//
//    @Override
//    public Object getChild(int groupPosition, int childPosition) {
//        return details.get(titles.get(groupPosition)).get(childPosition);
//    }
//
//    @Override
//    public long getGroupId(int groupPosition) {
//        return groupPosition;
//    }
//
//    @Override
//    public long getChildId(int groupPosition, int childPosition) {
//        return childPosition;
//    }
//
//    @Override
//    public boolean hasStableIds() {
//        return false;
//    }
//
//    @Override
//    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
//        ExpandableItem item = (ExpandableItem) getGroup(groupPosition);
//        if (convertView == null) {
//            LayoutInflater layoutInflater = (LayoutInflater) this.context.
//                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            convertView = layoutInflater.inflate(R.layout.expandable_profile_list_item, null);
//        }
//        AppCompatTextView listTitleTextView = convertView
//                .findViewById(R.id.expandableTexView);
//
//        listTitleTextView.setTypeface(null, Typeface.BOLD);
//        listTitleTextView.setText(item.getTitle());
//        listTitleTextView.setCompoundDrawablesWithIntrinsicBounds(
//                ContextCompat.getDrawable(context, item.getStartIcon()),
//                null,
//                null,
////                tintDrawable(ContextCompat.getDrawable(context, item.getEndIcon())),
//                null
//        );
//        return convertView;
//    }
//
//    private Drawable tintDrawable(Drawable drawable) {
//        drawable = DrawableCompat.wrap(drawable);
//        DrawableCompat.setTint(drawable, ContextCompat.getColor(context, R.color.gray_500));
//        return drawable;
//    }
//
//    @Override
//    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
//        final String item = (String) getChild(groupPosition, childPosition);
//        if (convertView == null) {
//            LayoutInflater layoutInflater = (LayoutInflater) this.context
//                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            convertView = layoutInflater.inflate(R.layout.expandable_profile_list_item, null);
//        }
//        TextView expandedListTextView = convertView
//                .findViewById(R.id.expandableTexView);
//        expandedListTextView.setText(item);
////        expandedListTextView.setCompoundDrawablesWithIntrinsicBounds(
////                ContextCompat.getDrawable(context, item.getStartIcon()),
////                null,
////                ContextCompat.getDrawable(context, item.getStartIcon()),
////                null
////        );
//        return convertView;
//    }
//
//    @Override
//    public boolean isChildSelectable(int groupPosition, int childPosition) {
//        return true;
//    }
}
