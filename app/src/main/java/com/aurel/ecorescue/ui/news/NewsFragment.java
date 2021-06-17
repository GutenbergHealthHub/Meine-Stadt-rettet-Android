package com.aurel.ecorescue.ui.news;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.adapter.NewsAdapter;
import com.aurel.ecorescue.data.DataRepository;
import com.aurel.ecorescue.interfaces.Callback;
import com.aurel.ecorescue.utils.NavigationDrawer;
import com.aurel.ecorescue.view.MainActivity;

import org.jetbrains.annotations.NotNull;


public class NewsFragment extends Fragment implements Callback {

    public NewsFragment() {}

    private DataRepository mRepository;
    private NavigationDrawer mDrawerHelper;

    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news, container, false);
    }


    private NewsAdapter mAdapter;
    private SwipeRefreshLayout mRefresh;

    public void onViewCreated(@NotNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.drawer_news));
        NavController navController = Navigation.findNavController(view);
        mDrawerHelper = ((MainActivity) requireActivity()).getNavigationDrawer();
        toolbar.setNavigationOnClickListener(v -> {
            mDrawerHelper.openDrawer();
        });


        mRepository = DataRepository.getInstance();


        mRefresh = view.findViewById(R.id.srl);
        RecyclerView rv = view.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new NewsAdapter(getContext(), navController);

        rv.setAdapter(mAdapter);

        mRepository.getNews().observe(getViewLifecycleOwner(), items -> {
            if (items!=null) {
                mAdapter.setItems(items);
            }
        });

        mRepository.loadNews("loadNews", this);

        mRefresh.setOnRefreshListener(() -> {
            mRefresh.setRefreshing(true);
            mRepository.loadNews("loadNews", this);
        });

    }


    @Override
    public void onSuccess(String id, String message) {
        // Data loaded
        if (id.equals("loadNews")) {
            mRefresh.setRefreshing(false);
        }
    }

    @Override
    public void onError(String id, String message) {
        // Problem loading
        if (id.equals("loadNews")) {
            mRefresh.setRefreshing(false);
        }
    }


}
