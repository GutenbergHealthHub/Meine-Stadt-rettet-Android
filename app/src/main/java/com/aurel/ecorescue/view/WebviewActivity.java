package com.aurel.ecorescue.view;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.telephony.TelephonyManager;
import android.util.Log;
import android.webkit.WebView;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.enums.WebviewActivityType;
import com.aurel.ecorescue.view.x_emergency.EmergencyAppCompatActivity;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

public class WebviewActivity extends EmergencyAppCompatActivity {

    public WebviewActivityType type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.x_activity_webview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        String name = (String) getIntent().getStringExtra("name");
        if (name != null) {
            toolbar.setTitle(name);
        }
        setSupportActionBar(toolbar);
        type = (WebviewActivityType) getIntent().getSerializableExtra("type");

        if (type == null) {
            Log.d("EcoRescue", "Error! WebviewActiviy getIntent does not contain a type. closing this view.");
            finish();
        }
        LoadUrl();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void LoadUrl() {
        Log.d("EcoRescue", "type=" + type.name());
        ParseQuery<ParseObject> query = ParseQuery.getQuery("URLS");
        query.whereEqualTo("type", type.name());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null && list != null && list.size() > 0) {
                    TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    String countryCode = tm.getSimCountryIso();
                    String url = "";
                    if (countryCode.equals("en")) {
                        url = list.get(0).getString("url_en");
                    }
                    if (url.length() <= 1) {//Fallback de
                        url = list.get(0).getString("url_de");
                    }

                    WebView webView = (WebView) findViewById(R.id.webview);
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.loadUrl(url);
                } else {
                    Log.d("EcoRescue", "Error parsing url for type " + type + ". Exiting view.");
                    finish();
                }
            }
        });
    }


}
