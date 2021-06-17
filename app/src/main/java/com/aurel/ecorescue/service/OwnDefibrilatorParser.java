package com.aurel.ecorescue.service;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.interfaces.OnOwnDefibrillatorLoadedListener;
import com.aurel.ecorescue.model.OwnDefibrillator;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class OwnDefibrilatorParser {

    OnOwnDefibrillatorLoadedListener listener;

    Context ctx;

    public OwnDefibrilatorParser(OnOwnDefibrillatorLoadedListener listener, Context context) {
        this.listener = listener;
        ctx = context;
    }


    public void GetOwnDefibrillators(final Activity activity) {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Defibrillator");
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.whereEqualTo("creator", ParseUser.getCurrentUser());

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> list, ParseException e) {
                if (e == null) {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            final ArrayList<OwnDefibrillator> defib = new ArrayList<>();
                            for (ParseObject p : list) {
                                OwnDefibrillator item = createDefibrillator(p);
                                defib.add(item);

                            }
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    listener.ownDefibrillatorLoaded(defib);
                                }
                            });
                        }
                    };
                    AsyncTask.execute(runnable);
                } else {
                    listener.ownDefibrillatorLoaded((List<OwnDefibrillator>) null);
                    Log.d("OwnDefibParser", "ParseException: " + e.getCode() + " " + e.getLocalizedMessage() + " error:" + e + " msg:" + e.getMessage());
                    e.printStackTrace();

                }
            }

        });
    }

    public void GetDefibrillator(String defibID) {
        final OwnDefibrillator[] item = new OwnDefibrillator[1];
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Defibrillator");
        query.whereEqualTo("objectId", defibID);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    Log.d("OwnDefibParser", "INSIDE FUNC");
                    item[0] = createDefibrillator(parseObject);
                    listener.ownDefibrillatorLoaded(item[0]);

                } else
                    Log.d("OwnDefibParser", "Could Not find object");
            }
        });
    }


    public OwnDefibrillator createDefibrillator(ParseObject p) {
        OwnDefibrillator r = new OwnDefibrillator();
        r.id = p.getObjectId();
        if (p.getString("object").equals(""))
            r.object = ctx.getString(R.string.undefined);
        else
            r.object = p.getString("object");
        r.street = p.getString("street");
        r.street_number = p.getString("number");
        r.zipcode = p.getString("zip");
        r.city = p.getString("city");
        r.Address = p.getString("street") + " " + p.getString("number") + " ," + p.getString("zip") + " " + p.getString("city");
        r.createdAt = p.getCreatedAt();
        r.state = p.getInt("state");
        r.information = p.getString("information");
        r.producer = p.getInt("producerDefiValue");
        r.model = p.getString("model");
        r.type = p.getString("type");
        r.geoPoint = p.getParseGeoPoint("location");
        r.latitude = r.geoPoint.getLatitude();
        r.longitude = r.geoPoint.getLongitude();
        r.files = p.getJSONArray("files");

        return r;
    }

}
