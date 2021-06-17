package com.aurel.ecorescue.service;

import android.util.Log;

import com.aurel.ecorescue.enums.InformationItemType;
import com.aurel.ecorescue.interfaces.OnInformationItemsLoadedListener;
import com.aurel.ecorescue.model.InformationItem;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import timber.log.Timber;

/**
 * Created by daniel on 6/8/17.
 */

public class InformationItemParser {

    OnInformationItemsLoadedListener listener;

    public InformationItemParser(OnInformationItemsLoadedListener listener) {
        this.listener = listener;
    }

    public void getInformationItems(final InformationItemType type) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery(type.getValue());
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.whereEqualTo("activated", true);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> list, ParseException e) {
                if (e == null) {
                    Timber.d("Getting " + type + " is done");
                    ArrayList<InformationItem> news = new ArrayList<InformationItem>();
                    for (ParseObject o : list) {
                        InformationItem item = CreateInformationItemFromParseObject(type, o);
                        Log.d("EcoRescue", "creating information item " + item.Id);
                        news.add(item);
                    }
                    Collections.sort(news, new Comparator<InformationItem>() {
                        @Override
                        public int compare(InformationItem o1, InformationItem o2) {
                            return o2.CreatedAt.compareTo(o1.CreatedAt);
                        }
                    });
                    listener.informationItemsLoaded(type, news);
                } else {
                    listener.informationItemsLoaded(type, null);
                    Log.d("InformationItemParser", "ParseException: " + e.getCode() + " " + e.getLocalizedMessage() + " error:" + e + " msg:" + e.getMessage());
                    e.printStackTrace();

                }
            }
        });
    }

    public void GetItemFromLocalDatastore(final InformationItemType type, String id) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(type.getValue());
        query.whereEqualTo("objectId", id);
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (object == null) {
                    Log.d("InformationItemParser", "The getFirst request failed.");
                } else {
                    Log.d("InformationItemParser", "Retrieved the object.");
                    InformationItem item = CreateInformationItemFromParseObject(type, object);
                    listener.itemLoadedFromCache(type, item);
                }
            }
        });
    }

    InformationItem CreateInformationItemFromParseObject(InformationItemType type, ParseObject o) {
        if (type.equals(InformationItemType.NEWS)) {
            InformationItem item = new InformationItem();
            item.title = o.getString("title");
            item.Text = o.getString("abstract");
            item.description = o.getString("subtitle");
            item.Id = o.getObjectId();
            item.Url = o.getString("newsUrl");
            item.Type = InformationItemType.NEWS;
            try {
                item.Image = o.getParseFile("imageObject").getFile();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            item.CreatedAt = o.getCreatedAt();
            return item;
        } else if (type.equals(InformationItemType.EVENT)) {
            InformationItem item = new InformationItem();
            item.title = o.getString("title");
            item.Text = o.getString("information");
            item.Timestamp = o.getDate("date");
            item.description = o.getString("additionalInformation");
            item.Id = o.getObjectId();
            item.Url = o.getString("eventUrl");
            try {
                item.Image = o.getParseFile("imageObject").getFile();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            item.Type = InformationItemType.EVENT;
            item.CreatedAt = o.getCreatedAt();
            item.City = o.getString("city");
            item.Street = o.getString("street");
            item.Zip = o.getString("zip");
            item.Organizer = o.getString("organizer");
            item.To = o.getDate("to");
            item.From = o.getDate("from");
            item.Email = o.getString("email");
            item.Phone = o.getString("phone");
            return item;
        }
        return null;
    }
}
