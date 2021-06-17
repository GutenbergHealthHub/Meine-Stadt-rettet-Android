package com.aurel.ecorescue.view.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.enums.MapMarkerType;
import com.aurel.ecorescue.view.ThemedActivity;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class MapDetailActivity extends ThemedActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        View.OnClickListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private MarkerTag markerTag;
    private double lat, lng;
    private RelativeLayout containerImages;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.x_activity_map_detail);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        DrawerHelper.SetupNavigationDarwer(this, toolbar);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        containerImages = (RelativeLayout) findViewById(R.id.container_photos);
        containerImages.setVisibility(View.GONE);

        markerTag = (MarkerTag) getIntent().getSerializableExtra("tag");
        lat = getIntent().getDoubleExtra("lat", 0);
        lng = getIntent().getDoubleExtra("long", 0);

        fillInfos();
    }

    private void placeMarker() {
        MarkerOptions options = new MarkerOptions();
        MarkerOptions markerOptions = new MarkerOptions();
        LatLng ll = new LatLng(markerTag.lat, markerTag.lng);
        markerOptions.position(ll);
        //markerOptions.title(p.getString("adressName"));
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mMap.addMarker(markerOptions);
    }

    private void zoomMap(double lat, double lng) {

        //Calculate the markers to get their position
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        LatLng self = new LatLng(lat, lng);
        LatLng detail = new LatLng(markerTag.lat, markerTag.lng);
        Log.d("EcoRescue", "zoom to " + lat + " " + lng + " marker: " + markerTag.lat + " " + markerTag.lng);
        b.include(self);
        b.include(detail);
        LatLngBounds bounds = b.build();
        int pxPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, pxPadding);
        mMap.moveCamera(cu);
    }

    private void findDirections(double lat, double lng) {
        TextView distance = (TextView) findViewById(R.id.distance);
        GetDirectionsTask task = new GetDirectionsTask(mMap, distance, null);
        task.execute(GetDirectionsTask.GetURLForDirections(this, lat, lng, markerTag.lat, markerTag.lng));
    }

    private void getPlaceInformation() {
        Log.d("EcoRescue", "Place id " + markerTag.placeId);
        Places.GeoDataApi.getPlaceById(mGoogleApiClient, markerTag.placeId)
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (places.getStatus().isSuccess() && places.getCount() > 0) {
                            final Place myPlace = places.get(0);

                            RelativeLayout containerUrl = (RelativeLayout) findViewById(R.id.container_url);
                            if (myPlace.getWebsiteUri() != null) {
                                TextView url = (TextView) findViewById(R.id.url);
                                url.setText(myPlace.getWebsiteUri().toString());
                                containerUrl.setVisibility(View.VISIBLE);
                            } else {
                                containerUrl.setVisibility(View.GONE);
                            }

                            RelativeLayout containerPhone = (RelativeLayout) findViewById(R.id.container_phone);
                            if (myPlace.getPhoneNumber() != null) {
                                TextView url = (TextView) findViewById(R.id.phone);
                                url.setText(myPlace.getPhoneNumber());
                                containerPhone.setVisibility(View.VISIBLE);
                            } else {
                                containerPhone.setVisibility(View.GONE);
                            }

                            Log.d("EcoRescue", "Place found: " + myPlace.getName());
                        } else {
                            Log.d("EcoRescue", "Place not found");
                        }
                        places.release();
                    }
                });
    }

    private void fillInfos() {

        TextView title = (TextView) findViewById(R.id.title);
        TextView adress = (TextView) findViewById(R.id.subtitle);
        TextView info = (TextView) findViewById(R.id.info);
        RelativeLayout containerUrl = (RelativeLayout) findViewById(R.id.container_url);

        RelativeLayout containerPhone = (RelativeLayout) findViewById(R.id.container_phone);
        RelativeLayout containerInfo = (RelativeLayout) findViewById(R.id.container_info);

        if (markerTag.title != null) {
            title.setText(markerTag.title);
        } else {
            RelativeLayout dividerTitle = (RelativeLayout) findViewById(R.id.dividerTitle);
            dividerTitle.setVisibility(View.GONE);
            title.setVisibility(View.GONE);
        }
        adress.setText(markerTag.address);

        if (markerTag.type == MapMarkerType.defibrilator) {
            containerUrl.setVisibility(View.GONE);
            containerPhone.setVisibility(View.GONE);
            if (!markerTag.info.equals("")) {
                info.setText(markerTag.info);
            } else {
                containerInfo.setVisibility(View.GONE);
            }
            loadMoreDefibrilatorImages();
        } else {
            containerInfo.setVisibility(View.GONE);
        }
    }

    private void loadMoreDefibrilatorImages() {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Defibrillator");
        query.whereEqualTo("objectId", markerTag.placeId);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    JSONArray files = object.getJSONArray("files");
                    if (files != null) {
                        int length = files.length();
                        if (length > 0) {
                            containerImages.setVisibility(View.VISIBLE);
                        }
                        for (int i = 0; i < length; i++) {
                            try {
                                JSONObject o = (JSONObject) files.get(i);
                                final String url = (String) o.get("url");
                                Log.d("EcoRescue", "got image url " + url);
                                switch (i) {
                                    case 0:
                                        ImageView i1 = (ImageView) findViewById(R.id.photo1);
                                        i1.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(MapDetailActivity.this, ImageViewActivity.class);
                                                intent.putExtra("url", url);
                                                startActivity(intent);
                                            }
                                        });
                                        Glide.with(i1.getContext()).load(url).into(i1);
                                        break;
                                    case 1:
                                        ImageView i2 = (ImageView) findViewById(R.id.photo2);
                                        i2.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(MapDetailActivity.this, ImageViewActivity.class);
                                                intent.putExtra("url", url);
                                                startActivity(intent);
                                            }
                                        });
                                        Glide.with(i2.getContext()).load(url).into(i2);
                                        break;
                                    case 2:
                                        ImageView i3 = (ImageView) findViewById(R.id.photo3);
                                        i3.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(MapDetailActivity.this, ImageViewActivity.class);
                                                intent.putExtra("url", url);
                                                startActivity(intent);
                                            }
                                        });
                                        Glide.with(i3.getContext()).load(url).into(i3);
                                        break;
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }

                    }
                } else {
                    Log.d("EcoRescue", "MapDetailActivity error downloading defibrilator. " + e.getLocalizedMessage());
                }
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("EcoRescue", "Error. Need permission to continue");
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        buildGoogleApiClient();
        placeMarker();
        findDirections(lat, lng);

        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
//                mMap.animateCamera(CameraUpdateFactory.zoomTo(30));
                zoomMap(lat, lng);
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

        if (markerTag.type != MapMarkerType.defibrilator) {
            getPlaceInformation();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.navigate:
                String uri = String.format(Locale.ENGLISH, "geo:%f,%f?z=17&q=%f,%f(%s)", markerTag.lat, markerTag.lng, markerTag.lat, markerTag.lng, markerTag.title);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
                break;
        }
    }
}
