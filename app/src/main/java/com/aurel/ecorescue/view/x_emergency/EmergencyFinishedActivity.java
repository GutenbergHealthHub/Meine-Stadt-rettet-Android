package com.aurel.ecorescue.view.x_emergency;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.view.ThemedActivity;
import com.aurel.ecorescue.view.protocols.ProtocolActivity;

public class EmergencyFinishedActivity extends ThemedActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_finished);
    }

    public void finishActivity(View view) {
        finish();
    }

    public void fillReport(View view) {
        Intent intent = new Intent(this, ProtocolActivity.class);
        intent.putExtra("emergencyStateId", getIntent().getStringExtra("emergencyStateId"));
        intent.putExtra("emergencyStateStatus", getIntent().getStringExtra("emergencyStateStatus"));
        startActivity(intent);
        finish();
    }

}
