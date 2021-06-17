package com.aurel.ecorescue.view.news;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.adapter.InformationItemAdapter;
import com.aurel.ecorescue.enums.InformationItemType;
import com.aurel.ecorescue.interfaces.OnInformationItemsLoadedListener;
import com.aurel.ecorescue.model.InformationItem;
import com.aurel.ecorescue.service.InformationItemParser;

import java.util.ArrayList;

/**
 * Created by daniel on 6/16/17.
 */

public class InformationItemTabFragment extends Fragment implements OnInformationItemsLoadedListener, SwipeRefreshLayout.OnRefreshListener {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_TYPE = "type";
    private View rootView;
    private InformationItemType informationItemType;
    private InformationItemAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView textViewEmpty;
    private InformationItemParser iiParser;
    private InformationItemType type;

    public InformationItemTabFragment() {

    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static InformationItemTabFragment newInstance(int sectionNumber) {
        InformationItemTabFragment fragment = new InformationItemTabFragment();
        Bundle args = new Bundle();
        if (sectionNumber == 0) {
            args.putSerializable(ARG_SECTION_TYPE, InformationItemType.NEWS);
        } else {
            args.putSerializable(ARG_SECTION_TYPE, InformationItemType.EVENT);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.x_fragment_information_item_tab, container, false);
        RecyclerView list = (RecyclerView) rootView.findViewById(R.id.recycleview);
        initializeRecyclerView(list);
        adapter = new InformationItemAdapter(null, R.layout.x_cell_informationitem);
        list.setAdapter(adapter);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipetorefresh);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(this);
        textViewEmpty = (TextView) rootView.findViewById(R.id.emptyview);
        iiParser = new InformationItemParser(this);
        type = (InformationItemType) getArguments().getSerializable(ARG_SECTION_TYPE);
        loadData();
        return rootView;
    }


    @Override
    public void informationItemsLoaded(InformationItemType type, ArrayList<InformationItem> informationItemList) {
        Log.d("EcoRescue", "InformationItemTabFragment informationItemsLoaded() " + type.name());
        adapter.SetList(informationItemList);
        swipeRefreshLayout.setRefreshing(false);
        if (informationItemList == null) {
            textViewEmpty.setVisibility(View.VISIBLE);
            textViewEmpty.setText(R.string.error_loading_data);
        } else if (informationItemList.isEmpty()) {
            switch (type.name()) {
                case "NEWS":
                    textViewEmpty.setText(R.string.no_news_found);
                    break;
                case "EVENT":
                    textViewEmpty.setText(R.string.no_events_found);
                    break;
                default:
                    break;
            }
            textViewEmpty.setVisibility(View.VISIBLE);
        } else {
            textViewEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void itemLoadedFromCache(InformationItemType type, InformationItem item) {
    }

    RecyclerView initializeRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        return recyclerView;
    }

    @Override
    public void onRefresh() {
        Log.d("EcoRescue", "onRefresh()");
        loadData();
    }

    void loadData() {
        iiParser.getInformationItems(type);
    }
}