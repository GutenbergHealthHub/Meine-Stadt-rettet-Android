package com.aurel.ecorescue.view.map;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.interfaces.OnOwnDefibrillatorLoadedListener;
import com.aurel.ecorescue.model.OwnDefibrillator;
import com.aurel.ecorescue.service.OwnDefibrilatorParser;
import com.aurel.ecorescue.view.dialogs.LoadingDialog;
import com.aurel.ecorescue.view.x_emergency.EmergencyAppCompatActivity;
import com.aurel.ecorescue.x_tools.x_EcoPreferences;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class CreateDefibrilatorActivityX extends EmergencyAppCompatActivity implements OnMapReadyCallback, OnOwnDefibrillatorLoadedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        View.OnClickListener,
        View.OnFocusChangeListener,
        AdapterView.OnItemSelectedListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnCameraMoveListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Address selectedAddress;

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 3517;
    private static final int CAMERA_REQUEST_ID = 1836;
    boolean searchMode;

    private EditText editTitle, editStreet, editPlz, editStreetNo, editCity, editDescription, editType, editModel;
    private int producer_value;
    private LinearLayout containerPhoto1, containerPhoto2, containerPhoto3;
    private File photo1, photo2, photo3;
    private ParseFile file1, file2, file3;
    private Button addPhoto;
    private LoadingDialog loadingDialog;
    Geocoder geocoder;
    private Location myInitialLocation;
    private boolean isMakingPhoto;
    OwnDefibrilatorParser parser;
    boolean hideSaveButton;
    Spinner spinner_producer;
    String existing_id;
    boolean retrived_data;
    boolean[] downloadedImages = new boolean[3];
    ImageView iv1, iv2, iv3;
    JSONArray downloadedFiles;
    private Marker defrib_marker;
    OwnDefibrillator defrib;
    private LatLng aedGeoLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.x_activity_create_defibrilator);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        setSupportActionBar(toolbar);
        for (int i = 0; i < 3; i++) {
            downloadedImages[i] = false;
        }
        final ProgressDialog progress = ProgressDialog.show(this, getString(R.string.please_wait), getString(R.string.loading_data), true);

        progress.setCancelable(false);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.show();

        new Thread(new Runnable() {
            public void run() {
                int tries = 0;
                while (!retrived_data) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    tries++;
                    if(tries>= 10){
                        progress.dismiss();
                        return;
                    }
                    // close the progress bar dialog
                }
                progress.dismiss();

            }
        }).start();

        containerPhoto1 = (LinearLayout) findViewById(R.id.container_photo1);
        containerPhoto2 = (LinearLayout) findViewById(R.id.container_photo2);
        containerPhoto3 = (LinearLayout) findViewById(R.id.container_photo3);
        addPhoto = (Button) findViewById(R.id.add_photo);
        containerPhoto1.setVisibility(View.GONE);
        containerPhoto2.setVisibility(View.GONE);
        containerPhoto3.setVisibility(View.GONE);

        editTitle = (EditText) findViewById(R.id.title);
        editStreet = (EditText) findViewById(R.id.adress_street);
        editStreetNo = (EditText) findViewById(R.id.adress_street_number);
        editCity = (EditText) findViewById(R.id.adress_City);
        editDescription = (EditText) findViewById(R.id.info);
        editPlz = (EditText) findViewById(R.id.adress_plz);
        editModel = findViewById(R.id.model);
        editType = findViewById(R.id.type);


        editTitle.setOnFocusChangeListener(this);
        editStreet.setOnFocusChangeListener(this);
        editStreetNo.setOnFocusChangeListener(this);
        editCity.setOnFocusChangeListener(this);
        editDescription.setOnFocusChangeListener(this);
        editModel.setOnFocusChangeListener(this);
        editType.setOnFocusChangeListener(this);

        editPlz.setOnFocusChangeListener(this);
        spinner_producer = (Spinner) findViewById(R.id.producer_spinner);
        spinner_producer.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.defibrillator_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_producer.setAdapter(adapter);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        geocoder = new Geocoder(this);
        loadingDialog = new LoadingDialog();
        iv1 = (ImageView) findViewById(R.id.photo_1);
        iv2 = (ImageView) findViewById(R.id.photo_2);
        iv3 = (ImageView) findViewById(R.id.photo_3);
        // to prevent layout scroll interfering with map scroll
        ImageView transparent = (ImageView) findViewById(R.id.imagetrans);
        final com.nirhart.parallaxscroll.views.ParallaxScrollView scroll = findViewById(R.id.scroll);
        transparent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        scroll.requestDisallowInterceptTouchEvent(true);
                        // Disable touch on transparent view
                        return false;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        scroll.requestDisallowInterceptTouchEvent(false);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        scroll.requestDisallowInterceptTouchEvent(true);
                        return false;

                    default:
                        return true;
                }
            }
        });

        findViewById(R.id.btn_save).setOnClickListener(v -> savePhotos());

    }


    @Override
    protected void onResume() {
        super.onResume();
        //Avoid pin page after taking a photo
        if (isMakingPhoto) {
            SharedPreferences.Editor editor = x_EcoPreferences.GetEditor(CreateDefibrilatorActivityX.this);
            editor.putBoolean(x_EcoPreferences.AppComesFromBackground, false);
            editor.apply();
            isMakingPhoto = false;
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("EcoRescue", "Error. Need permission to continue");
            return;
        }
        mMap.setMyLocationEnabled(true);
        buildGoogleApiClient();
        mMap.setOnMapClickListener(this);

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
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapClick(LatLng point) {
        aedGeoLocation = point;
        if (defrib == null || (!(defrib.state == 3 || defrib.state == 4))) {
            if (defrib_marker != null) { //if marker exists (not null or whatever)
                defrib_marker.setPosition(point);
            } else {
                defrib_marker = mMap.addMarker(new MarkerOptions()
                        .position(point)
                        .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.mapmarker_defibrillator_verified_v2))));

            }
            List<Address> addressList = null;
            try {
                addressList = geocoder.getFromLocation(point.latitude, point.longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addressList != null && addressList.size() > 0) {
                selectedAddress = addressList.get(0);
            }
            fillInfos(selectedAddress);
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        if (myInitialLocation == null) {
            myInitialLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
            setInfoForPlace(location.getLatitude(), location.getLongitude());
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_place:
                try {
                    AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                            .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                            .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                            .setCountry("DE")
                            .build();
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .setFilter(typeFilter)
                            .build(this);
                    searchMode = true;
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.add_photo:
                if (canAccessCamera()) {
                    isMakingPhoto = true;
                    EasyImage.openCamera(this, 0);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_REQUEST_ID);
                }
                break;
            case R.id.photo_delete_1:
                photo1 = null;
                containerPhoto1.setVisibility(View.GONE);
                downloadedImages[0] = false;
                addPhoto.setVisibility(View.VISIBLE);
                break;
            case R.id.photo_delete_2:
                photo2 = null;
                containerPhoto2.setVisibility(View.GONE);
                downloadedImages[1] = false;
                addPhoto.setVisibility(View.VISIBLE);
                break;
            case R.id.photo_delete_3:
                photo3 = null;
                addPhoto.setVisibility(View.VISIBLE);
                downloadedImages[2] = false;
                containerPhoto3.setVisibility(View.GONE);
                break;
        }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);

                Log.d("EcoRescue", "Place: " + place.getName());
                LatLng ll = place.getLatLng();

                if (defrib_marker != null) { //if marker exists (not null or whatever)
                    defrib_marker.setPosition(ll);
                } else {
                    defrib_marker = mMap.addMarker(new MarkerOptions()
                            .position(ll)
                            .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.mapmarker_defibrillator_verified_v2))));

                }


                editTitle.setText(place.getName());
                //setInfoForPlace(defrib_marker.getPosition().latitude, defrib_marker.getPosition().longitude);

                mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.d("EcoRescue", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            searchMode = false;
        } else {
            EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
                @Override
                public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {

                }

                @Override
                public void onImagePicked(File file, EasyImage.ImageSource imageSource, int i) {

                    file = adjustImage(file);//Compress and rotate image correctly
                    if (photo1 == null && !downloadedImages[0]) {
                        Glide.with(CreateDefibrilatorActivityX.this)
                                .load(file)
                                .into(iv1);
                        photo1 = file;
                        containerPhoto1.setVisibility(View.VISIBLE);
                    } else if (photo2 == null && !downloadedImages[1]) {
                        Glide.with(CreateDefibrilatorActivityX.this)
                                .load(file)
                                .into(iv2);
                        photo2 = file;
                        containerPhoto2.setVisibility(View.VISIBLE);
                    } else if (photo3 == null && !downloadedImages[2]) {
                        Glide.with(CreateDefibrilatorActivityX.this)
                                .load(file)
                                .into(iv3);
                        photo3 = file;
                        containerPhoto3.setVisibility(View.VISIBLE);
                    }

                    if ((photo1 != null || downloadedImages[0]) && (photo2 != null || downloadedImages[1]) && (photo3 != null || downloadedImages[2]))
                        addPhoto.setVisibility(View.GONE);
                }
            });
        }
    }

    private File adjustImage(File file) {
        try {
            //Compress image
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;

            FileInputStream inputStream = new FileInputStream(file);
            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            //Rotate correctly
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(file.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            selectedBitmap = rotateBitmap(selectedBitmap, orientation);

            OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 66, os);
            os.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private void setInfoForPlace(double lat, double lng) {
        try {
            List<Address> addressList = geocoder.getFromLocation(lat, lng, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                selectedAddress = address;
                fillInfos(address);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //load from existing defrib
        existing_id = getIntent().getStringExtra("id");
        if (existing_id != null) {
            //Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
            final OwnDefibrillator[] o = new OwnDefibrillator[1];
            parser = new OwnDefibrilatorParser(this, getApplicationContext());
            parser.GetDefibrillator(existing_id);
        } else
            //close progress box
            retrived_data = true;
    }

    private void fillInfos(Address address) {
        if (address == null)
            return;
        String street = address.getThoroughfare();
        String plz = address.getPostalCode();
        String city = address.getLocality();
        String streetno = address.getSubThoroughfare();
        editStreet.setText(street);
        editStreetNo.setText(streetno);
        editPlz.setText(plz);
        editCity.setText(city);


    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.x_menu_create_defibrilator, menu);
//        if (hideSaveButton)
//            menu.findItem(R.id.save).setVisible(false);
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if(item.getItemId() == R.id.save){
//            savePhotos();
//        }
        switch (item.getItemId()){
            case R.id.save:
                savePhotos();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void savePhotos() {
        if (photo1 == null && photo2 == null && photo3 == null) {
            photoSaved();
            return;
        }
        LoadingDialog loadingDialog = new LoadingDialog();
        loadingDialog.show(getFragmentManager(), "loading");

        if (photo1 != null) {
            final ParseFile file = new ParseFile(photo1);
            file.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    file1 = file;
                    photoSaved();
                }
            });
        }
        if (photo2 != null) {
            final ParseFile file = new ParseFile(photo2);
            file.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    file2 = file;
                    photoSaved();
                }
            });
        }
        if (photo3 != null) {
            final ParseFile file = new ParseFile(photo3);
            file.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    file3 = file;
                    photoSaved();
                }
            });
        }
    }

    private void photoSaved() {
        if (existing_id != null) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Defibrillator");
            query.getInBackground(existing_id, new GetCallback<ParseObject>() {
                public void done(ParseObject parseObject, ParseException e) {
                    if (e == null) {
                        parseObject.put("object", editTitle.getText().toString());
                        parseObject.put("street", editStreet.getText().toString());
                        parseObject.put("number", editStreetNo.getText().toString());
                        parseObject.put("city", editCity.getText().toString());
                        parseObject.put("zip", editPlz.getText().toString());
                        parseObject.put("information", editDescription.getText().toString());
                        parseObject.put("producerDefiValue", producer_value);
                        parseObject.put("type", editType.getText().toString());
                        parseObject.put("model", editModel.getText().toString());
                        parseObject.put("state", 1);

                        ParseGeoPoint geoPoint = new ParseGeoPoint(aedGeoLocation.latitude, aedGeoLocation.longitude);
                        parseObject.put("location", geoPoint);
                        parseObject.put("files", createJsonForImages());
                        ParseACL acl = new ParseACL();
                        acl.setPublicWriteAccess(true);
                        acl.setPublicReadAccess(true);
                        parseObject.setACL(acl);
                        parseObject.saveInBackground();
                        if (loadingDialog != null && loadingDialog.isVisible()) {
                            loadingDialog.dismiss();
                        }
                        finish();

                    }
                }
            });

        } else if ((file1 != null) == (photo1 != null)
                && (file2 != null) == (photo2 != null)
                && (file3 != null) == (photo3 != null)) {

            //all photos are now safed correctly. we can continue saving the defibriallator object.
            ParseObject defi = new ParseObject("Defibrillator");
            defi.put("object", editTitle.getText().toString());
            defi.put("street", editStreet.getText().toString());
            defi.put("number", editStreetNo.getText().toString());
            defi.put("city", editCity.getText().toString());
            defi.put("zip", editPlz.getText().toString());
            defi.put("creator", ParseUser.getCurrentUser());
            defi.put("information", editDescription.getText().toString());
            defi.put("producerDefiValue", producer_value);
            defi.put("type", editType.getText().toString());
            defi.put("model", editModel.getText().toString());
            defi.put("state", 1);

            defi.put("files", createJsonForImages());

            ParseGeoPoint geoPoint = new ParseGeoPoint(aedGeoLocation.latitude, aedGeoLocation.longitude);
            defi.put("location", geoPoint);

            ParseACL acl = new ParseACL();
            acl.setPublicWriteAccess(true);
            acl.setPublicReadAccess(true);
            defi.setACL(acl);

            defi.saveInBackground();
            Toast.makeText(this, R.string.defibrillator_approval, Toast.LENGTH_LONG).show();
            if (loadingDialog != null && loadingDialog.isVisible()) {
                loadingDialog.dismiss();
            }
            finish();


        }
    }

    private JSONArray createJsonForImages() {
        JSONArray jsonArray = new JSONArray();

        try {
            if (file1 != null && !downloadedImages[0]) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", file1.getName());
                jsonObject.put("url", file1.getUrl());
                jsonObject.put("__type", "File");
                jsonArray.put(jsonObject);
            } else if (downloadedFiles != null)
                jsonArray.put(downloadedFiles.getJSONObject(0));

            if (file2 != null && !downloadedImages[1]) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", file2.getName());
                jsonObject.put("url", file2.getUrl());
                jsonObject.put("__type", "File");
                jsonArray.put(jsonObject);
            } else if (downloadedFiles != null)
                jsonArray.put(downloadedFiles.getJSONObject(1));

            if (file3 != null && !downloadedImages[2]) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", file3.getName());
                jsonObject.put("url", file3.getUrl());
                jsonObject.put("__type", "File");
                jsonArray.put(jsonObject);
            } else if (downloadedFiles != null)
                jsonArray.put(downloadedFiles.getJSONObject(2));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }


    private void loadLocationForAdress() {
        try {
            String title = editTitle.getText().toString();
            String street = editStreet.getText().toString();
            String streetno = editStreetNo.getText().toString();
            String plz = editPlz.getText().toString();
            String city = editCity.getText().toString();
            String addressName = title + ", " + street + " " + streetno + ", " + plz + " " + city;
            List<Address> list = geocoder.getFromLocationName(addressName, 1);
            if (list.size() == 0) {
                return;
            }
            Address address = list.get(0);
            selectedAddress = address;
            aedGeoLocation = new LatLng(address.getLatitude(), address.getLongitude());

            if (defrib_marker != null) { //if marker exists (not null or whatever)
                defrib_marker.setPosition(aedGeoLocation);
            } else {
                defrib_marker = mMap.addMarker(new MarkerOptions()
                        .position(aedGeoLocation)
                        .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.mapmarker_defibrillator_verified_v2))));

            }

            mMap.moveCamera(CameraUpdateFactory.newLatLng(aedGeoLocation));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!searchMode) {
            loadLocationForAdress();
        }
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean canAccessCamera() {
        return (checkCallingOrSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //parent.getItemAtPosition(position);
        if (position == 14)
            producer_value = 99;
        else
            producer_value = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {


    }

    @Override
    public void ownDefibrillatorLoaded(List<OwnDefibrillator> list) {

    }

    public void fillFromParseObject(OwnDefibrillator defribb) {
        //Loading own defrib
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.defrib = defribb;
        if (defrib != null) {
            toolbar.setTitle(R.string.edit_defibrillator);
            editTitle.setText(defrib.object);
            editCity.setText(defrib.city);
            editStreet.setText(defrib.street);
            editStreetNo.setText(defrib.street_number);
            editPlz.setText(defrib.zipcode);
            editModel.setText(defrib.model);
            editType.setText(defrib.type);
            editDescription.setText(defrib.information);

            switch (defrib.producer) {
                case 0:
                    spinner_producer.setSelection(0);
                    break;
                case 1:
                    spinner_producer.setSelection(1);
                    break;
                case 2:
                    spinner_producer.setSelection(2);
                    break;
                case 3:
                    spinner_producer.setSelection(3);
                    break;
                case 4:
                    spinner_producer.setSelection(4);
                    break;
                case 5:
                    spinner_producer.setSelection(5);
                    break;
                case 6:
                    spinner_producer.setSelection(6);
                    break;
                case 7:
                    spinner_producer.setSelection(7);
                    break;
                case 8:
                    spinner_producer.setSelection(8);
                    break;
                case 9:
                    spinner_producer.setSelection(9);
                    break;
                case 10:
                    spinner_producer.setSelection(10);
                    break;
                case 11:
                    spinner_producer.setSelection(11);
                    break;
                case 12:
                    spinner_producer.setSelection(12);
                    break;
                case 13:
                    spinner_producer.setSelection(13);
                    break;
                case 99:
                    spinner_producer.setSelection(14);
                    break;

            }


            LatLng ll = new LatLng(defrib.latitude, defrib.longitude);

            if (defrib_marker != null) { //if marker exists (not null or whatever)
                defrib_marker.setPosition(ll);
            } else {
                defrib_marker = mMap.addMarker(new MarkerOptions()
                        .position(ll)
                        .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.mapmarker_defibrillator_verified_v2))));

            }
            mMap.moveCamera(CameraUpdateFactory.newLatLng(ll));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

            // to move
            Button b3 = findViewById(R.id.photo_delete_1);
            Button b4 = findViewById(R.id.photo_delete_2);
            Button b5 = findViewById(R.id.photo_delete_3);
            Button b6 = findViewById(R.id.add_photo);

            String img1, img2, img3;
            if (defrib != null)
                downloadedFiles = defrib.files;
            try {
                img1 = downloadedFiles.getJSONObject(0).get("url").toString();
                //fetchImages(new File(Uri.parse(img1).toString()));
                containerPhoto1.setVisibility(View.VISIBLE);
                Glide.with(this).load(img1).into(iv1);
                downloadedImages[0] = true;


            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                img2 = downloadedFiles.getJSONObject(1).get("url").toString();
                //fetchImages(new File(Uri.parse(img2).toString()));
                containerPhoto2.setVisibility(View.VISIBLE);
                Glide.with(this).load(img2).into(iv2);
                downloadedImages[1] = true;

            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                img3 = downloadedFiles.getJSONObject(2).get("url").toString();
                //fetchImages(new File(Uri.parse(img3).toString()));
                containerPhoto3.setVisibility(View.VISIBLE);
                Glide.with(this).load(img3).into(iv3);
                downloadedImages[2] = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if ((downloadedImages[0]) && downloadedImages[1] && downloadedImages[2])
                addPhoto.setVisibility(View.GONE);

            if (defrib.state == 3 || defrib.state == 4) {
                setTitle(R.string.view_defibrillator);
                hideSaveButton = true;
//                invalidateOptionsMenu();
                setUpSaveButton();
                makeEditTextUnEditable(editTitle);
                makeEditTextUnEditable(editCity);
                makeEditTextUnEditable(editDescription);
                makeEditTextUnEditable(editModel);
                makeEditTextUnEditable(editPlz);
                makeEditTextUnEditable(editStreetNo);
                makeEditTextUnEditable(editStreet);
                makeEditTextUnEditable(editType);
                Button b1 = findViewById(R.id.search_place);
                b1.setVisibility(View.GONE);
                b3.setVisibility(View.GONE);
                b4.setVisibility(View.GONE);
                b5.setVisibility(View.GONE);
                b6.setVisibility(View.GONE);

                //for spinner
                spinner_producer.setFocusable(false);
                spinner_producer.setEnabled(false);
                spinner_producer.setBackgroundColor(Color.TRANSPARENT);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setAllGesturesEnabled(false);
            }
            retrived_data = true;

        }
    }

    private void setUpSaveButton(){
        if (hideSaveButton) {
            findViewById(R.id.btn_save).setVisibility(View.GONE);
        } else {
            findViewById(R.id.btn_save).setVisibility(View.VISIBLE);
        }
    }

    public void makeEditTextUnEditable(EditText e) {
        e.setFocusable(false);
        e.setEnabled(false);
        e.setCursorVisible(false);
        e.setKeyListener(null);
        e.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public void ownDefibrillatorLoaded(OwnDefibrillator defrib) {
        fillFromParseObject(defrib);
    }

    @Override
    public void onCameraMove() {

    }

}
