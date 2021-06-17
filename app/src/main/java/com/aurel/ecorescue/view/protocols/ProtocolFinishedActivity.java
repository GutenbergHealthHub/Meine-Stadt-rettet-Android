package com.aurel.ecorescue.view.protocols;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.view.ThemedActivity;

public class ProtocolFinishedActivity extends ThemedActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protocol_finished);
    }

    public void finishActivity(View view) {
        finish();
    }

}
