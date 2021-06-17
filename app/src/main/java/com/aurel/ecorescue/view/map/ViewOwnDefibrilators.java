package com.aurel.ecorescue.view.map;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.adapter.OwnDefibrillatorAdapter;
import com.aurel.ecorescue.interfaces.OnOwnDefibrillatorLoadedListener;
import com.aurel.ecorescue.model.OwnDefibrillator;
import com.aurel.ecorescue.service.OwnDefibrilatorParser;
import com.aurel.ecorescue.view.ThemedActivity;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

public class ViewOwnDefibrilators extends ThemedActivity implements OnOwnDefibrillatorLoadedListener, SwipeRefreshLayout.OnRefreshListener {

    OwnDefibrillatorAdapter adapter;
    private RecyclerView recyclerViewDefibrilators;
    OwnDefibrilatorParser parser;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView txtEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.x_activity_view_own_defibrilator);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());


        recyclerViewDefibrilators = (RecyclerView) findViewById(R.id.recycleview);
        initializeRecyclerView(recyclerViewDefibrilators);
        adapter = new OwnDefibrillatorAdapter(null, R.layout.x_cell_own_defribillators);
        recyclerViewDefibrilators.setAdapter(adapter);
        swipeRefreshLayout = findViewById(R.id.swipetorefresh);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(this);
        parser = new OwnDefibrilatorParser(this, getApplicationContext());
        txtEmpty = (TextView) findViewById(R.id.emptyview);


        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition(); //get position which is swipe

                if (direction == ItemTouchHelper.LEFT) {    //if swipe left

                    AlertDialog.Builder builder = new AlertDialog.Builder(ViewOwnDefibrilators.this); //alert for confirm to delete
                    builder.setMessage("Are you sure to delete?");    //set message
                    final List<OwnDefibrillator> list = adapter.GetList();
                    builder.setPositiveButton("REMOVE", new DialogInterface.OnClickListener() { //when click on DELETE
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (list.get(position).state == 3 || list.get(position).state == 4)
                                Toast.makeText(ViewOwnDefibrilators.this, "Sorry approved defilibrators cannot be deleted", Toast.LENGTH_SHORT).show();
                            else {
                                adapter.notifyItemRemoved(position);    //item removed from recylcerview
                                //Toast.makeText(ViewOwnDefibrilators.this, list.get(position).id, Toast.LENGTH_SHORT).show();

                                ParseQuery<ParseObject> query = ParseQuery.getQuery("Defibrillator");
                                query.whereEqualTo("objectId", list.get(position).id);
                                query.findInBackground(new FindCallback<ParseObject>() {
                                    public void done(List<ParseObject> datas, ParseException e) {
                                        if (e == null) {
                                            // iterate over all messages and delete them
                                            for (ParseObject data : datas) {
                                                data.deleteInBackground();
                                            }
                                            Toast.makeText(ViewOwnDefibrilators.this, "Deleted", Toast.LENGTH_SHORT).show();
                                            list.remove(position);  //then remove item
                                            onRefresh();
                                        } else {
                                            Toast.makeText(ViewOwnDefibrilators.this, "Error", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                            }
                            return;
                        }
                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {  //not removing items if cancel is done
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            adapter.notifyItemRemoved(position + 1);    //notifies the RecyclerView Adapter that data in adapter has been removed at a particular position.
                            adapter.notifyItemRangeChanged(position, adapter.getItemCount());   //notifies the RecyclerView Adapter that positions of element in adapter has been changed from position(removed element index to end of list), please update it.
                            return;
                        }
                    }).show();  //show alert dialog
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewDefibrilators); //set swipe to recylcerview
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.x_menu_add_defibrilator, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_defrib) {
            Intent intent = new Intent(getApplicationContext(), CreateDefibrilatorActivityX.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    RecyclerView initializeRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        return recyclerView;
    }

    @Override
    public void ownDefibrillatorLoaded(List<OwnDefibrillator> list) {
        adapter.SetList(list);
        swipeRefreshLayout.setRefreshing(false);
        if (list == null) {
            txtEmpty.setVisibility(View.VISIBLE);
            txtEmpty.setText(R.string.error_loading_data);
        } else if (list.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
            txtEmpty.setText(R.string.you_have_not_added_aeds);
        } else {
            txtEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void ownDefibrillatorLoaded(OwnDefibrillator defrib) {

    }

    @Override
    public void onRefresh() {
        Log.d("EcoRescue", "onRefresh()");
        loadData();
    }

    public void loadData() {
        parser.GetOwnDefibrillators(this);
    }
}




