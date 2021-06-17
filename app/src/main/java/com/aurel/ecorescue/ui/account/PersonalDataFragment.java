package com.aurel.ecorescue.ui.account;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.transition.Slide;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aurel.ecorescue.R;
import com.aurel.ecorescue.profile.ProfileData;
import com.aurel.ecorescue.utils.GetAddressIntentService;
import com.aurel.ecorescue.utils.ParseUtils;
import com.aurel.ecorescue.utils.StyleUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.parse.ParseUser;
import com.transitionseverywhere.ChangeText;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;


public class PersonalDataFragment extends Fragment {

    public PersonalDataFragment() {}

    private NavController navController;
    private View view;

    Calendar bCalendar;

    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_data, container, false);
    }

    TransitionSet set = new TransitionSet()
            .addTransition(new Slide(Gravity.END))
            .setInterpolator(new LinearOutSlowInInterpolator());
    TransitionSet set2 = new TransitionSet()
            .addTransition(new Slide(Gravity.START))
            .setInterpolator(new LinearOutSlowInInterpolator());

    public void onViewCreated(@NotNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        navController = Navigation.findNavController(view);
        toolbar.setNavigationOnClickListener(v -> navController.navigateUp());
        if (getContext() != null) {
            toolbar.setNavigationIcon(StyleUtils.getHomeAsUpIndicator(getContext()));
        }


        RelativeLayout container = view.findViewById(R.id.container);
        FrameLayout fl_container = view.findViewById(R.id.fl_container);
        View firstPage = view.findViewById(R.id.ll_personal_information_container);
        View secondPage = view.findViewById(R.id.ll_address_container);
        View thirdPage = view.findViewById(R.id.ll_qualification_container);
        TextView page = view.findViewById(R.id.tv_counter);
        TextView instructions = view.findViewById(R.id.instructions);

        firstPage.setVisibility(View.VISIBLE);
        secondPage.setVisibility(View.GONE);
        thirdPage.setVisibility(View.GONE);
        view.findViewById(R.id.ib_prev).setVisibility(View.GONE);
        view.findViewById(R.id.ib_next).setVisibility(View.VISIBLE);
        view.findViewById(R.id.send_personal_information).setVisibility(View.GONE);
        page.setText("1/3");


        view.findViewById(R.id.ib_next).setOnClickListener(v -> {
            if (firstPage.getVisibility()==View.VISIBLE) {
                // We are on the first page, move to second
                TransitionManager.beginDelayedTransition(container, new ChangeText().setChangeBehavior(ChangeText.CHANGE_BEHAVIOR_OUT_IN));
                instructions.setText(getResources().getString(R.string.address_explanation));
                firstPage.setVisibility(View.GONE);
                thirdPage.setVisibility(View.GONE);
                view.findViewById(R.id.send_personal_information).setVisibility(View.GONE);
                view.findViewById(R.id.ib_prev).setVisibility(View.VISIBLE);
                view.findViewById(R.id.ib_next).setVisibility(View.VISIBLE);
                page.setText("2/3");
                TransitionManager.beginDelayedTransition(fl_container, set);
                secondPage.setVisibility(View.VISIBLE);
            } else {
                // We are on the second page, move to third
                TransitionManager.beginDelayedTransition(container, new ChangeText().setChangeBehavior(ChangeText.CHANGE_BEHAVIOR_OUT_IN));
                instructions.setText(getResources().getString(R.string.qualification_explanation));
                firstPage.setVisibility(View.GONE);
                secondPage.setVisibility(View.GONE);
                view.findViewById(R.id.send_personal_information).setVisibility(View.VISIBLE);
                view.findViewById(R.id.ib_prev).setVisibility(View.VISIBLE);
                view.findViewById(R.id.ib_next).setVisibility(View.GONE);
                page.setText("3/3");
                TransitionManager.beginDelayedTransition(fl_container, set);
                thirdPage.setVisibility(View.VISIBLE);
            }
        });

        view.findViewById(R.id.ib_prev).setOnClickListener(v -> {
            if (thirdPage.getVisibility()==View.VISIBLE) {
                // We are on the third page, move to second
                TransitionManager.beginDelayedTransition(container, new ChangeText().setChangeBehavior(ChangeText.CHANGE_BEHAVIOR_OUT_IN));
                instructions.setText(getResources().getString(R.string.address_explanation));
                firstPage.setVisibility(View.GONE);
                thirdPage.setVisibility(View.GONE);
                view.findViewById(R.id.send_personal_information).setVisibility(View.GONE);
                view.findViewById(R.id.ib_prev).setVisibility(View.VISIBLE);
                view.findViewById(R.id.ib_next).setVisibility(View.VISIBLE);
                page.setText("2/3");
                TransitionManager.beginDelayedTransition(fl_container, set2);
                secondPage.setVisibility(View.VISIBLE);
            } else {
                // We are on the second page, move to first
                TransitionManager.beginDelayedTransition(container, new ChangeText().setChangeBehavior(ChangeText.CHANGE_BEHAVIOR_OUT_IN));
                instructions.setText(getResources().getString(R.string.personal_information_explanation));
                thirdPage.setVisibility(View.GONE);
                secondPage.setVisibility(View.GONE);
                view.findViewById(R.id.send_personal_information).setVisibility(View.GONE);
                view.findViewById(R.id.ib_prev).setVisibility(View.GONE);
                view.findViewById(R.id.ib_next).setVisibility(View.VISIBLE);
                page.setText("1/3");
                TransitionManager.beginDelayedTransition(fl_container, set2);
                firstPage.setVisibility(View.VISIBLE);
            }
        });

        TextView birthday = view.findViewById(R.id.tv_birthday);
        birthday.setOnClickListener(v -> {
            Timber.d("Clicked");
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(view.getContext(), (timePicker, year, month, days) -> {
                birthday.setText(String.format(Locale.GERMANY, "%02d.%02d.%04d", days, month+1, year));
                calendar.set(year, month, days);
                this.bCalendar = calendar;
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });


        setUpView();


        addressResultReceiver = new LocationAddressResultReceiver(new Handler());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(view.getContext());

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                currentLocation = locationResult.getLocations().get(0);
                getAddress();
            };
        };
        startLocationUpdates();
    }

    EditText et_name, et_surname, et_code, et_mobile, et_street, et_number, et_zip, et_city, et_country;
    TextView tv_birthday, tv_qualification, tv_job;
    Switch sw_mobile_aed;

    int index_qual = 0;
    int index_job = 0;

    private void setUpView(){

        et_name = view.findViewById(R.id.et_name);
        et_surname = view.findViewById(R.id.et_surname);
        tv_birthday = view.findViewById(R.id.tv_birthday);
        et_code = view.findViewById(R.id.et_code);
        et_mobile = view.findViewById(R.id.et_phone);
        et_street = view.findViewById(R.id.et_street);
        et_number = view.findViewById(R.id.et_number);
        et_zip = view.findViewById(R.id.et_postal_code);
        et_city = view.findViewById(R.id.et_city);
        et_country = view.findViewById(R.id.et_country);
        tv_qualification = view.findViewById(R.id.tv_qualification);
        tv_job = view.findViewById(R.id.tv_job);
        sw_mobile_aed = view.findViewById(R.id.sw_mobile_aed);

        view.findViewById(R.id.btn_fill_with_current_location).setOnClickListener(v -> autofillAddress());

        ProfileData profileData = new ProfileData().createFromCurrentUser();

        index_qual = profileData.getQualification().isEmpty() ? 0 : Integer.valueOf(profileData.getQualification())-1;
        List<String> list = Arrays.asList(getResources().getStringArray(R.array.jobs));
        List<String> list2 = Arrays.asList(getResources().getStringArray(R.array.qualifications));
        index_job = list.indexOf(profileData.getProfession());

        String profession = profileData.getProfession();
        if (profession==null || profession.isEmpty()) {
            profession = getString(R.string.my_job);
        }
        tv_job.setText(profession);
        tv_qualification.setText(list2.get(index_qual));
        tv_qualification.setOnClickListener(v -> {
            new MaterialDialog.Builder(view.getContext()).title(R.string.qualification_for_reanimation).items(R.array.qualifications).itemsCallbackSingleChoice(index_qual, (dialog, itemView, which, text) -> {
                Timber.d("Selected: " + which + ", " + text);
                if (which>-1) index_qual = which;
                if (text!=null) tv_qualification.setText(text);
                return false;
            }).show();

        });

        sw_mobile_aed.setChecked(profileData.getMobileAed());

        tv_job.setOnClickListener(v -> {
            new MaterialDialog.Builder(view.getContext()).title(R.string.my_job).items(R.array.jobs).itemsCallbackSingleChoice(index_job, (dialog, itemView, which, text) -> {
                Timber.d("Selected: " + which + ", " + text);
                if (which>-1) index_job = which;
                if (text!=null) tv_job.setText(text);
                return false;
            }).show();
        });



        ParseUser user = ParseUser.getCurrentUser();
        // Step 1
        et_name.setText(profileData.getName());
        et_surname.setText(profileData.getSurname());
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String dateString = dateFormat.format(profileData.getBirthday());
        ParseUser u = ParseUser.getCurrentUser();
        Date date = u.getDate("birthdate");
        if (date!=null) {
            dateString = dateFormat.format(date);
        }
        tv_birthday.setText(dateString);
        if (user.getInt("phoneCode")==0) {
            et_code.setText("+49");
        } else {
            et_code.setText(String.valueOf(user.getLong("phoneCode")));
        }

        et_mobile.setText(String.valueOf(user.getLong("phoneNumber")));

        // Step 2
        et_street.setText(profileData.getStreet());
        et_number.setText(profileData.getStNumber());
        et_zip.setText(profileData.getZip());
        et_city.setText(profileData.getCity());
        et_country.setText(profileData.getCountry());

        // Step 3

        view.findViewById(R.id.send_personal_information).setOnClickListener(v -> savePersonalInformation());

    }

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private LocationAddressResultReceiver addressResultReceiver;
    private Location currentLocation;
    private LocationCallback locationCallback;
    private String street = "", stNumber = "", zip = "", city = "", country = "";

    @SuppressWarnings("MissingPermission")
    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(view.getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(2000);
            locationRequest.setFastestInterval(1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    null);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void getAddress() {

        if (!Geocoder.isPresent()) {
            Toast.makeText(view.getContext(),
                    "Can't find current address, ",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(view.getContext(), GetAddressIntentService.class);
        intent.putExtra("add_receiver", addressResultReceiver);
        intent.putExtra("add_location", currentLocation);
        view.getContext().startService(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                } else {
                    Toast.makeText(view.getContext(), "Location permission not granted, " +
                                    "restart the app if you want the feature",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }
    }

    private void showResults(String currentAdd){
        Timber.d("Got: %s", currentAdd);
        try {
            String[] arrOfStr = currentAdd.split("\n");
            Timber.d("Size: %s", arrOfStr.length);
            stNumber = arrOfStr[0];
            street = arrOfStr[1];
            city = arrOfStr[2];
            country = arrOfStr[3];
            zip = arrOfStr[4];
        } catch (Exception e) {
            Timber.d("error %s", e);
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        startLocationUpdates();

    }

    @Override
    public void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private class LocationAddressResultReceiver extends ResultReceiver {
        LocationAddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultCode == 0) {
                //Last Location can be null for various reasons
                //for example the api is called first time
                //so retry till location is set
                //since intent service runs on background thread, it doesn't block main thread
                Timber.d("Address Location null retrying");
                getAddress();
            }

            if (resultCode == 1) {
                Toast.makeText(view.getContext(),
                        "Address not found, " ,
                        Toast.LENGTH_SHORT).show();
            }

            String currentAdd = resultData.getString("address_result");

            showResults(currentAdd);
        }
    }

    private void savePersonalInformation() {
        ParseUser u = ParseUser.getCurrentUser();
        u.put("firstname", ParseUtils.getString(et_name.getText().toString()));
        u.put("lastname", ParseUtils.getString(et_surname.getText().toString()));
        if (bCalendar!=null) {
            Date date = new Date();
            date.setTime(bCalendar.getTimeInMillis());
            u.put("birthdate", date);
        }


        String phone = et_mobile.getText().toString().replace("+", "").replace(" ", "");
        String sphoneCode = ParseUtils.getString(et_code.getText().toString().replace("+", "").replace(" ", ""));
        long number = 0;
        long phoneCode = 0;
        if (!phone.isEmpty()) {
            number = Long.valueOf(phone);
        }
        if (!sphoneCode.isEmpty()) {
            phoneCode = Long.valueOf(sphoneCode);
        }
        u.put("phoneNumber", number);
        u.put("phoneCode", phoneCode);

        u.put("thoroughfare", ParseUtils.getString(et_street.getText().toString()));
        u.put("subThoroughfare", ParseUtils.getString(et_number.getText().toString()));
        u.put("zip",ParseUtils.getString(et_zip.getText().toString()));
        u.put("city", ParseUtils.getString(et_city.getText().toString()));
        u.put("country", ParseUtils.getString(et_country.getText().toString()));

        u.put("qualification", ""+(index_qual+1));

        String profession = ParseUtils.getString(tv_job.getText().toString());
        if (profession.equals(getString(R.string.my_job))){
            profession = "";
        }
        u.put("profession", profession);
        u.put("mobileAED", sw_mobile_aed.isChecked());

        u.saveInBackground(e -> {
            if (e!=null) {
                Timber.d("Saved successfully");
            } else {
                Timber.d("Error %s", e);
            }
        });

        navController.navigateUp();
    }
//
//    private int parseInt(String s){
//        for (int i=0; i<s.length(); i++){
//            char x = s.charAt(i);
//            if ()
//        }
//    }


    public void autofillAddress(){
        et_street.setText(street);
        et_number.setText(stNumber);
        et_zip.setText(zip);
        et_city.setText(city);
        et_country.setText(country);
    }


}
