package com.example.livenativerppg.models.searchMain.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.CombinedLoadStates;
import androidx.paging.LoadState;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.livenativerppg.R;
import com.example.livenativerppg.component.base.BaseFragment;
import com.example.livenativerppg.databinding.FragmentSearchEmrBinding;
import com.example.livenativerppg.models.emr_EMR.ui.DateStringEMRRecycleAdapter;
import com.example.livenativerppg.models.searchMain.data.viewmodel.SearchVM;

import dagger.hilt.android.AndroidEntryPoint;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

@AndroidEntryPoint
public class SearchEmrFragment extends BaseFragment<FragmentSearchEmrBinding> implements SearchInterface {
    SearchEmrFragment() {
        super(R.layout.fragment_search_emr);
    }

    private SearchVM searchVM;
    private SearchEmrRecycleViewAdapter adapter;

    @Override
    public void onInitialized() {
        super.onInitialized();
        searchVM = new ViewModelProvider(requireActivity()).get(SearchVM.class);
        adapter = new SearchEmrRecycleViewAdapter();

        binding.EmrSearchRecycleView.setAdapter(adapter);
    }

    @Override
    public void addObservers() {
        super.addObservers();
        searchVM.getEmrData().observe(getViewLifecycleOwner() , immutableTriplePagingData -> {
            adapter.submitData(getLifecycle() , immutableTriplePagingData);
            adapter.addLoadStateListener(combinedLoadStates -> null);
        });
    }

    @Override
    public void setUpClicks() {

    }

    @Override
    public void clickListener() {

    }

    @Override
    public void searchListener(String searchText) {

    }
}