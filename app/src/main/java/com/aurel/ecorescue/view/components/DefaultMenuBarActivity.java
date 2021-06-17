package com.aurel.ecorescue.view.components;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.Menu;
import android.view.MenuInflater;
import android.widget.TextView;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.view.ThemedActivity;

public class DefaultMenuBarActivity extends ThemedActivity {

    Toolbar mToolbar;

    private Integer mContentViewId;

    protected void defineContentViewBeforeCreate(int contentViewId) {
        mContentViewId = contentViewId;
    }

    @SuppressLint("Assert")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //make sure, prepare was called with non-null values
        assert (mContentViewId != null);

        setContentView(R.layout.activity_default_menu_bar);

        getLayoutInflater().inflate(mContentViewId, findViewById(R.id.defaultMenuBarContent));
        mToolbar = findViewById(R.id.defaultMenuBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbar.setTitle("");
    }

    protected void setMenuBarTitle(String title) {
        ((TextView)mToolbar.findViewById(R.id.toolbarTitle)).setText(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.default_menu_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    protected void onResume() {
        super.onResume();
    }
}
