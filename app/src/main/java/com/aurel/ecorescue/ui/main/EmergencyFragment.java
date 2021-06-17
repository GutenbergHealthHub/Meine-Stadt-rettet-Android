package com.aurel.ecorescue.ui.main;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.utils.BlueDialogListener;
import com.aurel.ecorescue.utils.BlueDialogUtils;
import com.aurel.ecorescue.utils.NavigationDrawer;
import com.aurel.ecorescue.utils.ParseUtils;
import com.aurel.ecorescue.view.MainActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;


public class EmergencyFragment extends Fragment {

    public EmergencyFragment() {}

    private NavController navController;
    private Context context;

    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_emergency, container, false);
    }


    private MapView mMapView;

    private Geocoder geocoder;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        context = getActivity();


        geocoder = new Geocoder(context, Locale.getDefault());


    }

    public void makeCall() {
        Intent intent;
        intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel: 112"));
        startActivity(intent);
    }

    public static final int LOCATION_UPDATE_MIN_DISTANCE = 10;
    public static final int LOCATION_UPDATE_MIN_TIME = 5000;

    private GoogleMap mGoogleMap;
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                Timber.d("%f, %f", location.getLatitude(), location.getLongitude());
//                String loc = "Location";
                String adr = "Address";
                String lat = "Lat:";
                String lng = "Lng:";
                if (context!=null) {
//                    loc = context.getResources().getString(R.string.location);
                    lat = context.getResources().getString(R.string.lat);
                    lng = context.getResources().getString(R.string.lng);
                    adr = context.getResources().getString(R.string.address);
                }
                Spanned geoLoc = HtmlCompat.fromHtml("<b>"+lat+"</b> "+location.getLatitude()+"<br/><b>"+lng+"</b> "+location.getLongitude(), HtmlCompat.FROM_HTML_MODE_LEGACY);
                mGeoLocation.setText(geoLoc);
                List<Address> addresses = null; // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                try {
                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    mAddress.setText(HtmlCompat.fromHtml(("<b>"+adr+":</b> " + ParseUtils.getString(address)), HtmlCompat.FROM_HTML_MODE_LEGACY));
                } catch (Exception e) {
                    e.printStackTrace();
                }


                drawMarker(location);
                mLocationManager.removeUpdates(mLocationListener);
            } else {
                Timber.d("Location is null");
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };
    private LocationManager mLocationManager;

    private TextView mAddress, mGeoLocation;

    public void onViewCreated(@NotNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        navController = Navigation.findNavController(view);
        toolbar.setNavigationOnClickListener(v -> {
            navController.navigateUp();
        });

        mAddress = view.findViewById(R.id.tv_current_address);
        mGeoLocation = view.findViewById(R.id.tv_current_geo_location);

        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        mMapView.getMapAsync(mMap -> {
            mGoogleMap = mMap;
            initMap();
            getCurrentLocation();
        });

//        new MaterialDialog.Builder(context).title(R.string.call_112).content(R.string.do_you_want_to_call_112).cancelable(false).positiveText("CALL").negativeText("Cancel").onPositive((dialog, which) -> {
//            makeCall();
//        }).show();

        BlueDialogUtils blueDialog = new BlueDialogUtils(getContext(), new BlueDialogListener() {
            @Override
            public void onPositiveClick() {
                makeCall();
            }

            @Override
            public void onNegativeClick() {

            }

            @Override
            public void onNeutralClick() {

            }
        }, false);
        blueDialog.setContentText(R.string.call_112);
        blueDialog.setPositiveText(context.getResources().getString(R.string.call));
        blueDialog.setNegativeText(context.getResources().getString(R.string.cancel));
        blueDialog.show();

        MaterialButton goBack = view.findViewById(R.id.btn_go_back);
        goBack.setOnClickListener(v -> {
            navController.navigateUp();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        getCurrentLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        mLocationManager.removeUpdates(mLocationListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private void initMap() {
        int googlePlayStatus = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (googlePlayStatus != ConnectionResult.SUCCESS) {
            GooglePlayServicesUtil.getErrorDialog(googlePlayStatus, getActivity(), -1).show();
        } else {
            if (mGoogleMap != null) {
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
                mGoogleMap.getUiSettings().setAllGesturesEnabled(true);
            }
        }
    }

    private void getCurrentLocation() {
        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Location location = null;
        if (!(isGPSEnabled || isNetworkEnabled))
            Snackbar.make(mMapView, "Error", Snackbar.LENGTH_INDEFINITE).show();
        else {
            if (isNetworkEnabled) {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
                location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (isGPSEnabled) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
                location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
        if (location != null) {
            Timber.d("getCurrentLocation(%f, %f)", location.getLatitude(), location.getLongitude());
            drawMarker(location);
        }
    }

    private void drawMarker(Location location) {
        if (mGoogleMap != null) {
            mGoogleMap.clear();
            LatLng gps = new LatLng(location.getLatitude(), location.getLongitude());
            String current_pos = "Current Position";
            if (context!=null) {
                current_pos = context.getResources().getString(R.string.current_location);
            }
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(gps)
                    .title(current_pos));
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 12));
        }

    }


}
