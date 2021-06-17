package com.aurel.ecorescue.ui.emergency;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.enums.InformationItemType;
import com.aurel.ecorescue.utils.AppExecutors;
import com.aurel.ecorescue.utils.ParseUtils;
import com.aurel.ecorescue.x_tools.x_EcoPreferences;
import com.bumptech.glide.Glide;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import timber.log.Timber;

public class IdentificationCardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification_card);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.emergency);
        toolbar.setNavigationOnClickListener(v -> finish());
        loadUserId();
    }

    private void loadUserId(){
        ParseUser u = ParseUser.getCurrentUser();

        ImageView frImage = findViewById(R.id.iv_fr_image);
        Glide.with(this)
                .load(ParseUtils.getUrl("profilePicture", u))
                .centerCrop()
                .placeholder(R.drawable.logo_v2)
                .into(frImage);

        ((TextView) findViewById(R.id.tv_fr_name)).setText(
                ParseUtils.getString("firstname", u) + " " +
                        ParseUtils.getString("lasttname", u));
        ((TextView) findViewById(R.id.tv_fr_email)).setText(u.getEmail());
        ((TextView) findViewById(R.id.tv_fr_address)).setText(parseAddress(u));

        ((TextView) findViewById(R.id.tv_fr_job)).setText(ParseUtils.getString("profession", u));
        ((TextView) findViewById(R.id.tv_fr_qualification)).setText(parseQualification(ParseUtils.getString("qualification", u)));
        ((TextView) findViewById(R.id.tv_fr_profile_id)).setText(u.getObjectId());

        ParseObject controlCenter = u.getParseObject("controlCenterRelation");
        try {
            if (controlCenter!=null) {
                controlCenter.fetchIfNeeded();
                ((TextView) findViewById(R.id.tv_cc_name)).setText(ParseUtils.getString("name", controlCenter));
                ((TextView) findViewById(R.id.tv_cc_address)).setText(parseAddressFull(controlCenter));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

//        ((TextView) findViewById(R.id.tv_fr_emergency_number)).setText(String.valueOf(sharedPreferences.getLong("EmergencyNumberDC", 0)));
        SharedPreferences sharedPreferences = x_EcoPreferences.GetSharedPreferences(this);
        String id = sharedPreferences.getString(x_EcoPreferences.ActiveEmergencyId, "");
        String endc = sharedPreferences.getString("emergencyNumberDC", "");
        if (endc!=null)((TextView) findViewById(R.id.tv_fr_emergency_number)).setText(endc);
        Timber.d("Got %s", id);
        if (id !=null && !id.isEmpty()) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Emergency");
            query.getInBackground(id, (object, e) -> {
                if (e == null) {
                    ParseObject cc = object.getParseObject("controlCenterRelation");
                    AppExecutors appExecutors = new AppExecutors();
                    appExecutors.networkIO().execute(() -> {
                        try {
                            cc.fetch();
                            appExecutors.mainThread().execute(() -> {
                                ((TextView) findViewById(R.id.tv_cc_name)).setText(ParseUtils.getString("name", cc));
                                ((TextView) findViewById(R.id.tv_cc_address)).setText(parseAddressFull(cc));
//                                ((TextView) findViewById(R.id.tv_fr_emergency_number)).setText(ParseUtils.getString("emergencyNumberDC", cc));
                            });
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                    });



                } else {
                    Timber.d(e, "Error");
                }
            });
        }


    }

    private String parseQualification(String q){
        if (q.isEmpty()) return "";
        int i = 0;
        try {
            i = Integer.valueOf(q);
        } catch (Exception e){
            Timber.d(e, "Error parsing int");
        }

        switch (i){
            default:
                break;
            case 1:
                return getResources().getString(R.string.qualifications_1);
            case 2:
                return getResources().getString(R.string.qualifications_2);
            case 3:
                return getResources().getString(R.string.qualifications_3);
            case 4:
                return getResources().getString(R.string.qualifications_4);
            case 5:
                return getResources().getString(R.string.qualifications_5);
            case 6:
                return getResources().getString(R.string.qualifications_6);
            case 7:
                return getResources().getString(R.string.qualifications_7);
            case 8:
                return getResources().getString(R.string.qualifications_8);
        }
        return "";
    }

    private String parseAddress(ParseObject u){
        String city = ParseUtils.getString("city", u);
        String country = ParseUtils.getString("country", u);
        String zip = ParseUtils.getString("zip", u);
        String result = zip;
        if (city!=null && !city.isEmpty()) result += (", " + city);
        if (country!=null && !country.isEmpty()) result += (", " + country);
        return  result;
    }

    private String parseAddressFull(ParseObject u){
        String address = ParseUtils.getString("address", u);
        return address + ", " + parseAddress(u);
    }

    @Override
    public void onResume(){
        super.onResume();
        SharedPreferences.Editor editor = x_EcoPreferences.GetEditor(this);
        editor.putBoolean("notificationAlreadyAccepted", true);
        editor.apply();
    }
}
