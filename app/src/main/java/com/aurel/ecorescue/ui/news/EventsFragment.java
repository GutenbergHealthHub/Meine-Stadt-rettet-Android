package com.aurel.ecorescue.ui.news;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.adapter.EventsAdapter;
import com.aurel.ecorescue.data.DataRepository;
import com.aurel.ecorescue.interfaces.Callback;

import org.jetbrains.annotations.NotNull;


public class EventsFragment extends Fragment implements Callback {

    public EventsFragment() {}

    private NavController navController;
    private DataRepository mRepository;

    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_events, container, false);
    }


    private EventsAdapter mAdapter;
    private SwipeRefreshLayout mRefresh;

    public void onViewCreated(@NotNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        if (getParentFragment() != null) {
            navController = ((NewsEventsFragment)getParentFragment()).getNavController();
        }

        mRepository = DataRepository.getInstance();


        mRefresh = view.findViewById(R.id.srl);
        RecyclerView rv = view.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new EventsAdapter(getContext(), navController);

        rv.setAdapter(mAdapter);

        mRepository.getEvents().observe(getViewLifecycleOwner(), items -> {
            if (items!=null) {
                mAdapter.setItems(items);
            }
        });

        mRepository.loadEvents("loadEvents", this);

        mRefresh.setOnRefreshListener(() -> {
            mRefresh.setRefreshing(true);
            mRepository.loadCourses("loadEvents", this);
        });
    }


    @Override
    public void onSuccess(String id, String message) {
        // Data loaded
        if (id.equals("loadEvents")) {
            mRefresh.setRefreshing(false);
        }
    }

    @Override
    public void onError(String id, String message) {
        // Problem loading
        if (id.equals("loadEvents")) {
            mRefresh.setRefreshing(false);
        }
    }



}
