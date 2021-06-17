package com.aurel.ecorescue.ui.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aurel.ecorescue.R;
import com.aurel.ecorescue.enums.MapMarkerType;
import com.aurel.ecorescue.interfaces.OnDefibrilatorLoadedListener;
import com.aurel.ecorescue.interfaces.OnMapFilterApplyListener;
import com.aurel.ecorescue.utils.BlueDialogListener;
import com.aurel.ecorescue.utils.BlueDialogUtils;
import com.aurel.ecorescue.utils.NavigationDrawer;
import com.aurel.ecorescue.utils.PermissionUtils;
import com.aurel.ecorescue.view.MainActivity;
import com.aurel.ecorescue.view.dialogs.GPSDisabledDialog;
import com.aurel.ecorescue.view.dialogs.MapFilterDialog;
import com.aurel.ecorescue.view.map.GetDirectionsTask;
import com.aurel.ecorescue.view.map.MapDetailActivity;
import com.aurel.ecorescue.view.map.MapUtils;
import com.aurel.ecorescue.view.map.MarkerTag;
import com.aurel.ecorescue.view.map.ViewOwnDefibrilators;
import com.aurel.ecorescue.view.map.clustermanager.AedClusterItem;
import com.aurel.ecorescue.view.map.clustermanager.ClusterRenderer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

import static android.app.Activity.RESULT_OK;


public class AEDMapFragment extends Fragment implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMapClickListener,
        OnDefibrilatorLoadedListener,
        View.OnClickListener,
        OnMapFilterApplyListener,
        ClusterManager.OnClusterItemClickListener<AedClusterItem>,
        ClusterManager.OnClusterClickListener<AedClusterItem>  {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastUpdateLocation;
    private ClusterManager<AedClusterItem> mClusterManager;
    private List<String> existingItems = new ArrayList<>();
    double latitude;
    double longitude;
    MapUtils mapUtils;

    boolean infoShowing;
    private RelativeLayout infoLayout;
    private GetDirectionsTask directionsTask;
    private LatLng lastMarkerFetchPosition;
    final Handler handler = new Handler();

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 918;

    TextView infoTitle, infoSubSubTitle, infoDuration;
    ImageButton infoButton;
    MarkerTag selectedMarkerTag;


    // used to check weather to reload Aeds (if this changed since last upate)
    CameraPosition cameraPosition = new CameraPosition(new LatLng(0, 0), 0, 0, 0);
    final int delay = 5000; //milliseconds
    OnDefibrilatorLoadedListener listener = this;
    OnMapFilterApplyListener onMapFilterApplyListener = this;
    Runnable loadingDefis = new Runnable() {
        public void run() {
            Timber.d("MapActivity.onCameraIdle()");
            CameraPosition newPos = mMap.getCameraPosition();
            if (!newPos.target.equals(cameraPosition.target)) {
                cameraPosition = newPos;
                Location loc = new Location("");
                loc.setLatitude(mMap.getCameraPosition().target.latitude);
                loc.setLongitude(mMap.getCameraPosition().target.longitude);
                mapUtils.loadDefibrilators(loc, listener);
            }
            handler.postDelayed(this, delay);
        }
    };

    @Override
    public void onDestroy(){
        super.onDestroy();
        stopReloadingDefis();
    }


    //    @BindView(R.id.toolbar)
    public Toolbar toolbar;


    public AEDMapFragment() {}

    private NavController navController;
    private View view;
    private Context context;
    private Activity activity;

    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_aed_map, container, false);
    }

    private void setUpView(View view){
        view.findViewById(R.id.create_defibrilator).setOnClickListener(v->createDefibrilatorClick());
    }

    public void onViewCreated(@NotNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        this.context = getContext();
        this.activity = getActivity();
        toolbar = view.findViewById(R.id.toolbar);
        navController = Navigation.findNavController(view);
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity()!=null) {
                ((MainActivity) getActivity()).getNavigationDrawer().openDrawer();
            }
        });
        View fab = view.findViewById(R.id.create_defibrilator);
        ParseUser user = ParseUser.getCurrentUser();
        if (user == null || ParseAnonymousUtils.isLinked(user)) {
            fab.setVisibility(View.GONE);
        } else if (user.getBoolean("activated")){
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(v -> createDefibrilatorClick());
        }

        toolbar.inflateMenu(R.menu.menu_map);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.map_filter:
                        MapFilterDialog filter = new MapFilterDialog();
                        filter.SetListener(onMapFilterApplyListener);
                        filter.filterDefi = mapUtils.FilterDefi;
                        filter.filterHospital = mapUtils.FilterHospital;
                        filter.filterPharmacy = mapUtils.FilterPharmacy;
                        filter.filterFireDep = mapUtils.FilterFireDep;
                        filter.filterDoctor = mapUtils.FilterDoctor;
                        filter.filterDentist = mapUtils.FilterDentist;
                        filter.show(getChildFragmentManager(), "filter");
//                filter.show(getFragmentManager(), "filter");
                        break;
                    case R.id.map_search:
                        try {
                            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                                    .setCountry("DE")
                                    .build();

                            LatLng southwest = SphericalUtil.computeOffset(new LatLng(latitude, longitude), 100000 * Math.sqrt(2.0), 225);
                            LatLng northeast = SphericalUtil.computeOffset(new LatLng(latitude, longitude), 100000 * Math.sqrt(2.0), 45);
                            LatLngBounds searchArea = new LatLngBounds(southwest, northeast);
                            Intent intent =
                                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                            .setBoundsBias(searchArea)
                                            .setFilter(typeFilter)
                                            .build(activity);
                            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                        } catch (GooglePlayServicesRepairableException e) {
                            // TODO: Handle the error.
                        } catch (GooglePlayServicesNotAvailableException e) {
                            // TODO: Handle the error.
                        }
                        break;
                }
                return false;
            }
        });
        setUpView(view);

        view.findViewById(R.id.mapinfo_navigate).setOnClickListener(this::onClick);
        view.findViewById(R.id.mapinfo_info).setOnClickListener(this::onClick);

        if (!CheckGooglePlayServices()) {
            new MaterialDialog.Builder(context)
                    .title("Error")
                    .content("Need Google Play Service to run. Please update.")
                    .positiveText("OK")
                    .cancelable(false)
                    .onPositive((dialog, which) -> {
                        navController.navigate(R.id.mainFragment);
                    })
                    .show();

        }

        if (!canAccessLocation()) {
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Timber.d("MapFragment loading error");
        }

        infoTitle = view.findViewById(R.id.mapinfo_title);
        infoSubSubTitle = view.findViewById(R.id.mapinfo_distance);
        infoLayout = view.findViewById(R.id.map_info);
        infoDuration = view.findViewById(R.id.mapinfo_duration);
        infoButton = view.findViewById(R.id.mapinfo_info);
        GPSDisabledDialog.checkGPS(context);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (selectedMarkerTag == null || selectedMarkerTag.type != MapMarkerType.searchedPlace) {
            infoLayout.setVisibility(View.GONE);
            if (mMap != null) {
                mMap.setPadding(0, 0, 0, 0);
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the x_camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (PermissionUtils.checkPermissions(context, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            mapUtils = new MapUtils(activity, googleMap, this);
            directionsTask = new GetDirectionsTask(mMap, infoDuration, null);
            mMap.setMyLocationEnabled(true);
            mMap.setOnMapClickListener(this);
            buildGoogleApiClient();
            setUpClusterer();
            loadMarkers();
            startReloadingDefis();
        }
    }

    // reloads defis every #delay milliseconds
    private void startReloadingDefis() {
        handler.postDelayed(loadingDefis, delay);
    }

    private void stopReloadingDefis() {
        handler.removeCallbacks(loadingDefis);
        mMap.clear();
        if (mClusterManager!=null) mClusterManager.clearItems();
        if (existingItems!=null) existingItems.clear();
    }

    private void setUpClusterer() {
        mClusterManager = new ClusterManager<>(context, mMap);
        ClusterRenderer clusterRenderer = new ClusterRenderer(context, mMap, mClusterManager);
        mClusterManager.setRenderer(clusterRenderer);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterClickListener(this);
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
    }

    private void addItemsToClusterManager(List<AedClusterItem> aedClusterItems) {
        for (AedClusterItem aedClusterItem : aedClusterItems) {
            if (!existingItems.contains(aedClusterItem.getId())) {
                mClusterManager.addItem(aedClusterItem);
                existingItems.add(aedClusterItem.getId());
            }
        }
        mClusterManager.cluster();
        Timber.d("current number of AEDs: %s", existingItems.size());
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(context);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(activity, result,
                        0).show();
            }
            return false;
        }
        return true;
    }

    private boolean canAccessLocation() {
        return PermissionUtils.checkPermissions(context, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        if (mLastUpdateLocation == null || mLastUpdateLocation.distanceTo(location) > 500) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(14));

            mLastUpdateLocation = location;
        }

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            Timber.d("Removing Location Updates");
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        infoLayout.setVisibility(View.GONE);
        mMap.setPadding(0, 0, 0, 0);
        directionsTask.clear();
        directionsTask.cancel(true);
    }

    @Override
    public void defibrilatorLoaded(List<ParseObject> list) {
        if (mapUtils.FilterDefi) {
            Timber.d("defibrilators loaded" + (list != null) + " size=" + list.size());
            List<AedClusterItem> aedClusterItems = MapUtils.toAedClusterItems(list);
            addItemsToClusterManager(aedClusterItems);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mapinfo_navigate:
                String uri = String.format(Locale.ENGLISH, "geo:%f,%f?z=17&q=%f,%f(%s)", selectedMarkerTag.lat, selectedMarkerTag.lng, selectedMarkerTag.lat, selectedMarkerTag.lng, selectedMarkerTag.title);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
                break;
            case R.id.mapinfo_info:
                Intent info = new Intent(context, MapDetailActivity.class);
                Bundle b = new Bundle();
                b.putSerializable("tag", selectedMarkerTag);
                b.putDouble("lat", latitude);
                b.putDouble("long", longitude);
                info.putExtras(b);
                startActivity(info);
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_map, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }



    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.map_filter:
                MapFilterDialog filter = new MapFilterDialog();
                filter.SetListener(this);
                filter.filterDefi = mapUtils.FilterDefi;
                filter.filterHospital = mapUtils.FilterHospital;
                filter.filterPharmacy = mapUtils.FilterPharmacy;
                filter.filterFireDep = mapUtils.FilterFireDep;
                filter.filterDoctor = mapUtils.FilterDoctor;
                filter.filterDentist = mapUtils.FilterDentist;
                filter.show(getFragmentManager(), "filter");
//                filter.show(getFragmentManager(), "filter");
                break;
            case R.id.map_search:
                try {
                    AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                            .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                            .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                            .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                            .setCountry("DE")
                            .build();

                    LatLng southwest = SphericalUtil.computeOffset(new LatLng(latitude, longitude), 100000 * Math.sqrt(2.0), 225);
                    LatLng northeast = SphericalUtil.computeOffset(new LatLng(latitude, longitude), 100000 * Math.sqrt(2.0), 45);
                    LatLngBounds searchArea = new LatLngBounds(southwest, northeast);
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                    .setBoundsBias(searchArea)
                                    .setFilter(typeFilter)
                                    .build(activity);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void createDefibrilatorClick() {
        ParseUser user = ParseUser.getCurrentUser();
        if (ParseAnonymousUtils.isLinked(user)) { //User is not registered
//            CreateDefibrialltorInfoDialog infoDialog = new CreateDefibrialltorInfoDialog();
//            infoDialog.activity = activity;
//            infoDialog.userIsRegistered = false;
//            infoDialog.show(getChildFragmentManager(), "infoDialogDefiNotRegistered");
//            infoDialog.show(getFragmentManager(), "infoDialogDefiNotRegistered");

        } else if (!user.getBoolean("activated")) { // User is not activated
//            CreateDefibrialltorInfoDialog infoDialog = new CreateDefibrialltorInfoDialog();
//            infoDialog.activity = activity;
//            infoDialog.userIsRegistered = true;
//            infoDialog.show(getChildFragmentManager(), "infoDialogDefiRegistered");

//            new MaterialDialog.Builder(context).title(R.string.create_defi_info_title).content(R.string.create_defi_info).cancelable(false).positiveText(R.string.create_defi_info_status).onPositive((dialog, which) -> {
//                navController.navigate(R.id.action_aedMapFragment_to_profileFragment);
//            }).show();

            BlueDialogUtils blueDialog = new BlueDialogUtils(getContext(), new BlueDialogListener() {
                @Override
                public void onPositiveClick() {
                    navController.navigate(R.id.action_aedMapFragment_to_profileFragment);
                }

                @Override
                public void onNegativeClick() {

                }

                @Override
                public void onNeutralClick() {

                }
            }, true);
            blueDialog.setContentText(R.string.create_defi_info);
            blueDialog.setPositiveText(view.getContext().getResources().getString(R.string.create_defi_info_status));
            blueDialog.show();
//            infoDialog.show(getFragmentManager(), "infoDialogDefiRegistered");
        } else {
            Intent intent = new Intent(activity.getApplicationContext(), ViewOwnDefibrilators.class);
            startActivity(intent);
        }
    }

    @Override
    public void applyMapFilter(boolean defi, boolean pharmacy, boolean hospital, boolean firedepartment, boolean doctor, boolean dentist) {
        mapUtils.FilterDefi = defi;
        mapUtils.FilterHospital = hospital;
        mapUtils.FilterFireDep = firedepartment;
        mapUtils.FilterPharmacy = pharmacy;
        mapUtils.FilterDentist = dentist;
        mapUtils.FilterDoctor = doctor;

        if (defi)
            startReloadingDefis();
        else
            stopReloadingDefis();

        CameraPosition cpos = mMap.getCameraPosition();
        LatLng pos = cpos.target;
        mapUtils.loadMapMarker(pos);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(context, data);

                Timber.d("Place: %s", place.getName());
                MarkerOptions markerOptions = new MarkerOptions();
                LatLng ll = place.getLatLng();
                markerOptions.position(ll);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                Marker m = mMap.addMarker(markerOptions);
                MarkerTag tag = new MarkerTag();
                tag.address = place.getAddress().toString();
                tag.title = place.getName().toString();
                tag.type = MapMarkerType.searchedPlace;
                tag.lat = place.getLatLng().latitude;
                tag.lng = place.getLatLng().longitude;
                m.setTag(tag);

                mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(context, data);
                Timber.d(status.getStatusMessage());
            }

        }
    }

    public void loadMarkers() {
        CameraPosition cpos = mMap.getCameraPosition();
        LatLng pos = cpos.target;
        boolean loadMarkers = false;
        if (lastMarkerFetchPosition == null) {
            loadMarkers = true;
        } else {
            float[] result = new float[3];
            Location.distanceBetween(lastMarkerFetchPosition.latitude, lastMarkerFetchPosition.longitude, pos.latitude, pos.longitude, result);
            if (result[0] > 5000) {
                loadMarkers = true;
            }
        }
        if (loadMarkers) {
            mapUtils.loadMapMarker(pos);
            lastMarkerFetchPosition = pos;
        }
    }

    @Override
    public boolean onClusterItemClick(AedClusterItem aedClusterItem) {
        Timber.d("onMarkerClick");
        infoShowing = true;

        //Display the info view
        infoLayout.setVisibility(View.VISIBLE);
        int pxPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics());
        mMap.setPadding(0, 0, 0, pxPadding);

        MarkerTag tag = aedClusterItem.markerTag;
        selectedMarkerTag = tag;
        infoTitle.setText(tag.title);
        infoSubSubTitle.setText(tag.address);

        if (tag.type == MapMarkerType.searchedPlace) {
            infoButton.setVisibility(View.GONE);
        } else {
            infoButton.setVisibility(View.VISIBLE);
        }

        //Draw route
        String url = GetDirectionsTask.GetURLForDirections(context, latitude, longitude, aedClusterItem.getPosition().latitude, aedClusterItem.getPosition().longitude);
        directionsTask.clear();
        directionsTask.cancel(true);
        infoDuration.setText("");
        directionsTask = new GetDirectionsTask(mMap, infoDuration, null);
        directionsTask.execute(url);
        return false;
    }

    @Override
    public boolean onClusterClick(Cluster<AedClusterItem> cluster) {
        infoLayout.setVisibility(View.GONE);
        mMap.setPadding(0, 0, 0, 0);
        directionsTask.clear();
        directionsTask.cancel(true);
        return false;
    }


}
