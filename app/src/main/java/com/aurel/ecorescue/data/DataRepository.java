package com.aurel.ecorescue.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aurel.ecorescue.enums.InformationItemType;
import com.aurel.ecorescue.interfaces.Callback;
import com.aurel.ecorescue.model.Course;
import com.aurel.ecorescue.model.Events;
import com.aurel.ecorescue.model.News;
import com.aurel.ecorescue.utils.AppExecutors;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class DataRepository {

    private static DataRepository sInstance;

    public static DataRepository getInstance() {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository();
                }
            }
        }
        return sInstance;
    }


    private MutableLiveData<List<Course>> courses = new MutableLiveData<>();

    private MutableLiveData<List<News>> news = new MutableLiveData<>();

    private MutableLiveData<List<Events>> events = new MutableLiveData<>();

    private DataRepository(){
        courses.setValue(new ArrayList<>());
        news.setValue(new ArrayList<>());
        events.setValue(new ArrayList<>());
    }

    public LiveData<List<Course>> getCourses(){
        return courses;
    }

    public LiveData<List<News>> getNews(){
        return news;
    }

    public LiveData<List<Events>> getEvents(){
        return events;
    }

    public Course getCourse(String id){
        List<Course> items = courses.getValue();
        if (items!=null) {
            for (Course course: items) {
                if (id.equals(course.objectId)) {
                    return course;
                }
            }
        }
        return null;
    }

    public News getNews(String id){
        List<News> items = news.getValue();
        if (items!=null) {
            for (News item: items) {
                if (id.equals(item.objectId)) {
                    return item;
                }
            }
        }
        return null;
    }

    public Events getEvent(String id){
        List<Events> items = events.getValue();
        if (items!=null) {
            for (Events item: items) {
                if (id.equals(item.objectId)) {
                    return item;
                }
            }
        }
        return null;
    }

    public void loadCourses(String id, @Nullable Callback callback){
        ParseQuery<ParseObject> query = ParseQuery.getQuery(InformationItemType.COURSE.getValue());
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.whereEqualTo("activated", true);
        query.orderByDescending("createdAt");
        AppExecutors appExecutors = new AppExecutors();
        query.findInBackground((list, e) -> {
            if (e == null) {
                appExecutors.networkIO().execute(() -> {
                    ArrayList<Course> items = new ArrayList<>();
                    for (ParseObject o : list) {
                        items.add(new Course(o));
                    }
                    courses.postValue(items);
                });
                callbackHelper(id, "", true, callback);
            } else {
                callbackHelper(id, e.getMessage(), false, callback);
                Timber.d("ParseException: " + e.getCode() + " " + e.getLocalizedMessage() + " error:" + e + " msg:" + e.getMessage());
            }
        });
    }

    public void loadNews(String id, @Nullable Callback callback){
        ParseQuery<ParseObject> query = ParseQuery.getQuery(InformationItemType.NEWS.getValue());
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.whereEqualTo("activated", true);
        query.orderByDescending("createdAt");
        AppExecutors appExecutors = new AppExecutors();
        query.findInBackground((list, e) -> {
            if (e == null) {
                appExecutors.networkIO().execute(() -> {
                    ArrayList<News> items = new ArrayList<>();
                    for (ParseObject o : list) {
                        items.add(new News(o));
                    }
                    news.postValue(items);
                });

                callbackHelper(id, "", true, callback);
            } else {
                callbackHelper(id, e.getMessage(), false, callback);
                Timber.d("ParseException: " + e.getCode() + " " + e.getLocalizedMessage() + " error:" + e + " msg:" + e.getMessage());
            }
        });
    }

    public void loadEvents(String id, @Nullable Callback callback){
        ParseQuery<ParseObject> query = ParseQuery.getQuery(InformationItemType.EVENT.getValue());
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.whereEqualTo("activated", true);
        query.findInBackground((list, e) -> {
            if (e == null) {
                ArrayList<Events> items = new ArrayList<>();
                for (ParseObject o : list) {
                    items.add(new Events(o));
                }
                events.postValue(items);
                callbackHelper(id, "", true, callback);
            } else {
                callbackHelper(id, e.getMessage(), false, callback);
                Timber.d("ParseException: " + e.getCode() + " " + e.getLocalizedMessage() + " error:" + e + " msg:" + e.getMessage());
            }
        });
    }

    private void callbackHelper(String id, String message, boolean outcome, @Nullable Callback callback){
        if (callback !=null) {
            if (outcome) {
                callback.onSuccess(id, message);
            } else {
                callback.onError(id, message);
            }
        }
    }

}
