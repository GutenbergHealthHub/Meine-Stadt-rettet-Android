package com.aurel.ecorescue.view.protocols;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.utils.ParseUtils;
import com.aurel.ecorescue.utils.StyleUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by aurel on 10-Oct-16.
 */

public class ListIncompleteProtocolsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_incomplete_protocols);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(StyleUtils.getHomeAsUpIndicator(this));
        toolbar.setNavigationOnClickListener(v -> finish());


        ParseQuery<ParseObject> queryFinished = new ParseQuery<>("EmergencyState");
        ParseQuery<ParseObject> queryCanceled = new ParseQuery<>("EmergencyState");

        queryFinished.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        queryCanceled.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);

        queryFinished.whereEqualTo("userRelation", ParseUser.getCurrentUser());
        queryCanceled.whereEqualTo("userRelation", ParseUser.getCurrentUser());

        queryFinished.whereExists("endedAt");
        queryCanceled.whereExists("cancelledAt");

        ParseQuery<ParseObject> queryProtocol = ParseQuery.or(Arrays.asList(queryFinished, queryCanceled));
        queryProtocol.whereDoesNotExist("protocolRelation");
        ParseQuery<ParseObject> innerQuery = ParseQuery.getQuery("ControlCenter");
        innerQuery.whereEqualTo("reportRequired", true);
        queryProtocol.whereMatchesQuery("controlCenterRelation", innerQuery);
        queryProtocol.include("emergencyRelation");
        queryProtocol.addDescendingOrder("createdAt");
        queryProtocol.findInBackground((emergencyStates, e) -> {
            if (emergencyStates.size() > 0) {
                List<HashMap<String, Object>> aList = new ArrayList<HashMap<String, Object>>();

                for (ParseObject emergencyState : emergencyStates) {
                    try {
                        emergencyState.fetchIfNeeded();
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                    Timber.d("EmergencyState: %s", emergencyState.getObjectId());

                    ParseObject emergency = emergencyState.getParseObject("emergencyRelation");
                    if (emergency==null) {
                        Timber.d("Error: null");
                        continue;
                    }

                    Date abortedAt = emergencyState.getDate("cancelledAt");
                    Date endedAt = emergencyState.getDate("endedAt");
                    boolean wasFinished = endedAt != null;
                    Date shownDate = wasFinished ? endedAt : abortedAt;

                    HashMap<String, Object> hm = new HashMap<>();
                    hm.put("street", ParseUtils.getString(emergency.getString("streetName")) + " " + ParseUtils.getString(emergency.getString("streetNumber")));
                    hm.put("city", ParseUtils.getString(emergency.getString("zip")) + " " + ParseUtils.getString(emergency.getString("city")));
                    hm.put("status", getResources().getString(wasFinished ? R.string.finished : R.string.aborted));
                    hm.put("status_color", getResources().getColor(wasFinished ? R.color.md_green_400 : R.color.md_red_400 ));
                    hm.put("date", new SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.getDefault()).format(shownDate));
                    hm.put("lat", emergency.getParseGeoPoint("locationPoint").getLatitude());
                    hm.put("lng", emergency.getParseGeoPoint("locationPoint").getLongitude());
                    aList.add(hm);
                }

                String[] from = {"street", "city", "status", "date"};
                int[] to = {R.id.street, R.id.city, R.id.status, R.id.date};
                ListIncompleteProtocolsAdapter adapter = new ListIncompleteProtocolsAdapter(getBaseContext(), aList, R.layout.activity_list_incomplete_protocol_item, from, to);
                ListView listView = (ListView) findViewById(R.id.listview);
                listView.setAdapter(adapter);

                listView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
                    String emergencyStateId = emergencyStates.get(position).getObjectId();
                    Intent intent = new Intent(this, ProtocolActivity.class);
                    intent.putExtra("emergencyStateId", emergencyStateId);
                    intent.putExtra("emergencyStateStatus", emergencyStateId);
                    startActivity(intent);
                    finish();
                });
            }
        });
    }

    public void finishActivity(View view) {
        finish();
    }
}