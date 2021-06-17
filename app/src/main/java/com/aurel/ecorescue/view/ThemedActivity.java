package com.aurel.ecorescue.view;

import android.os.Bundle;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aurel.ecorescue.R;

import static com.aurel.ecorescue.StarterApplication.AppTheme;
import static com.aurel.ecorescue.StarterApplication.AppThemeEco;

public class ThemedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme();
    }

    private void setTheme() {
        if(AppTheme == AppThemeEco)
            setTheme(R.style.AppThemeMSR);
        else
            setTheme(R.style.AppThemeASB);
    }
}
