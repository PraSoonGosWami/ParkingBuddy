package com.invader.parkingbuddy.Fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.invader.parkingbuddy.Model.ParkingLocation;
import com.invader.parkingbuddy.R;
import com.location.aravind.getlocation.GeoLocator;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;


public class AddNewParkingFragment extends Fragment implements OnMapReadyCallback {


    private GoogleMap mMap;
    private LocationManager locationManager;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private TextView viewLoc;
    private double lat,lon;
    private String cityName;
    private Marker marker;
    private FloatingActionButton btnButton;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private EditText location_name,location_desc,location_tSlots,location_aSlots,location_cost;
    private PopupWindow popWindow;
    private Button edit;
    private String position;
    private String mapLink;
    private Snackbar snackbar;
    private Boolean isReady = false;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_new_parking, container, false);


        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //binding views ----------------------------------
        btnButton = view.findViewById(R.id.go_to_marker);
        edit = view.findViewById(R.id.edit);



        showSnackbar("Drag the pin or long click to set new location on map");


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //firebase Database references
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        databaseReference.keepSynced(true);


        btnButton.setOnClickListener(view1 -> {
            if (Build.VERSION.SDK_INT >= 23) {
                if (getContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else
                    requestLocation();
            } else
                requestLocation();
        });

        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);


        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location mCurrentLocation = locationResult.getLastLocation();
                LatLng myCoordinates = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

                cityName = getCityName(myCoordinates);
                lat = myCoordinates.latitude;
                lon = myCoordinates.longitude;
                position = lat+","+lon;
                mapLink="https://maps.googleapis.com/maps/api/staticmap?center="+position+
                        "&zoom=16&size=600x400&maptype=roadmap" +
                        "&markers=color:red|22.4585208,88.3854262&key=AIzaSyDd293hYyojdZ-epop9SMmzknxKLo9LchU";


                if(isReady) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myCoordinates, 16.0f));
                    if (marker == null) {
                        marker = mMap.addMarker(new MarkerOptions().position(myCoordinates));
                    } else
                        marker.setPosition(myCoordinates);
                    marker.setDraggable(true);
                }

            }

        };

        if (Build.VERSION.SDK_INT >= 23) {
            Log.d("mylog", "Getting Location Permission");
            if (getContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("mylog", "Not granted");
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            if (getContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("mylog", "Not granted");
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }else
                requestLocation();
        } else
            requestLocation();





        return view;
    }

    private String getCityName(LatLng myCoordinates) {
        String address = "";
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(myCoordinates.latitude, myCoordinates.longitude, 1);
            address = addresses.get(0).getAddressLine(0);
            lat  = addresses.get(0).getLatitude() ;
            lon = addresses.get(0).getLongitude();
            cityName = address;
            position = lat+","+lon;
            mapLink="https://maps.googleapis.com/maps/api/staticmap?center="+position+
                    "&zoom=16&size=600x400&maptype=roadmap" +
                    "&markers=color:red|22.4585208,88.3854262&key=AIzaSyDd293hYyojdZ-epop9SMmzknxKLo9LchU";

        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        isReady = true;
        mMap.setOnMapLongClickListener(latLng -> {
            marker.setPosition(latLng);
            cityName = getCityName(latLng);
            lat = latLng.latitude;
            lon = latLng.longitude;
            position = latLng.latitude+","+latLng.longitude;
            mapLink="https://maps.googleapis.com/maps/api/staticmap?center="+position+
                    "&zoom=16&size=600x400&maptype=roadmap" +
                    "&markers=color:red|22.4585208,88.3854262&key=AIzaSyDd293hYyojdZ-epop9SMmzknxKLo9LchU";
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng loc = new LatLng(marker.getPosition().latitude,marker.getPosition().longitude);
                cityName = getCityName(loc);
                lat = loc.latitude;
                lon = loc.longitude;
                position = loc.latitude+","+loc.longitude;
                mapLink="https://maps.googleapis.com/maps/api/staticmap?center="+position+
                        "&zoom=16&size=600x400&maptype=roadmap" +
                        "&markers=color:red|22.4585208,88.3854262&key=AIzaSyDd293hYyojdZ-epop9SMmzknxKLo9LchU";
            }
        });

        edit.setOnClickListener(v->{
            descPopup();
        });

    }

    private void requestLocation() {
        Boolean gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Boolean network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(!gps_enabled || !network_enabled)
            Toast.makeText(getContext(), "Turn on Location", Toast.LENGTH_SHORT).show();

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        String provider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);


        if (location != null && (System.currentTimeMillis() - location.getTime()) <= 1000 * 2) {
            LatLng myCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
            cityName = getCityName(myCoordinates);
            lat = myCoordinates.latitude;
            lon = myCoordinates.longitude;
            position = lat+","+lon;
            mapLink="https://maps.googleapis.com/maps/api/staticmap?center="+position+
                    "&zoom=16&size=600x400&maptype=roadmap" +
                    "&markers=color:red|22.4585208,88.3854262&key=AIzaSyDd293hYyojdZ-epop9SMmzknxKLo9LchU";
        } else {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setNumUpdates(1);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            Log.d("mylog", "Last location too old getting new location!");
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
            mFusedLocationClient.requestLocationUpdates(locationRequest,
                    mLocationCallback, Looper.myLooper());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        requestLocation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFusedLocationClient.flushLocations();
    }

    public void descPopup(){
        Button location_add,close_button;
        View view = getLayoutInflater().inflate(R.layout.location_description, null, false);
        popWindow = new PopupWindow(view,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

        ProgressBar progressBar = view.findViewById(R.id.progress);
        progressBar.setVisibility(View.GONE);

        viewLoc = view.findViewById(R.id.location);
        location_name = view.findViewById(R.id.location_name);
        location_desc = view.findViewById(R.id.location_desc);
        location_tSlots = view.findViewById(R.id.location_tSlots);
        location_aSlots = view.findViewById(R.id.location_aSlots);
        location_cost = view.findViewById(R.id.location_cost);
        location_add= view.findViewById(R.id.location_add);
        close_button = view.findViewById(R.id.close_button);

        location_add.setEnabled(true);
        // get device size
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);

        //mDeviceHeight = size.y;
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        // set height depends on the device size
        popWindow = new PopupWindow(view, width, height - 60, true);
        popWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        popWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        popWindow.setAnimationStyle(R.style.PopupAnimation);

        // show the popup at bottom of the screen and set some margin at bottom ie,
        popWindow.showAtLocation(view, Gravity.BOTTOM, 0, 100);



        viewLoc.setText(cityName);

        close_button.setOnClickListener(v->{
            popWindow.dismiss();
        });

        location_add.setOnClickListener(v->{
            location_add.setEnabled(false);
            pushData(location_add);
        });




    }
    public void pushData(Button button){
        ProgressDialog progressDialog = new ProgressDialog(getContext(),ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
        progressDialog.setMessage("Sit back relax!!");

        String address = viewLoc.getText().toString().trim();
        String name = location_name.getText().toString().trim();
        String desc = location_desc.getText().toString().trim();
        int tSlots = 0;
        int aSlots = 0;

        String pCost = location_cost.getText().toString();
        double lattitude = lat;
        double longitude = lon;
        String uid = firebaseUser.getUid();
        String pos = position;
        String id = uid.substring(0,4)+generateId();

        if(pos == null){
            Toast.makeText(getContext(), "Something went wrong\nTry selecting location again", Toast.LENGTH_SHORT).show();
            return;
        }
        String mapurl = mapLink;


        if(name.isEmpty() || name == null){
            if (Build.VERSION.SDK_INT >= 26) {
                location_name.setError("Name required");
                location_name.requestFocus();

            }
            else {
                Toast.makeText(getContext(), "Name required", Toast.LENGTH_SHORT).show();
                button.setEnabled(true);
            }
            return;

        }
        if(desc.isEmpty() || desc == null) {
            desc = "Not available";
        }


        if(location_tSlots.getText().toString().isEmpty() || location_tSlots.getText().toString()==null){
            if (Build.VERSION.SDK_INT >= 26) {
                location_tSlots.setError("Required");
                location_tSlots.requestFocus();
            }else{
                Toast.makeText(getContext(), "Slots cannot be empty", Toast.LENGTH_SHORT).show();
                button.setEnabled(true);
            }
            return;
        }

        if(location_aSlots.getText().toString().isEmpty() || location_aSlots.getText().toString()==null){
            if (Build.VERSION.SDK_INT >= 26) {
                location_aSlots.setError("Required");
                location_aSlots.requestFocus();
            }else {
                Toast.makeText(getContext(), "Slots cannot be empty", Toast.LENGTH_SHORT).show();
                button.setEnabled(true);
            }
            return;
        }

        if(pCost.isEmpty() || pCost == null){
            if (Build.VERSION.SDK_INT >= 26) {
                location_cost.setError("Required");
                location_cost.requestFocus();
            }else {
                Toast.makeText(getContext(), "Cont cannot be empty", Toast.LENGTH_SHORT).show();
                button.setEnabled(true);
            }
            return;
        }

        tSlots = Integer.parseInt(location_tSlots.getText().toString());

        aSlots = Integer.parseInt(location_aSlots.getText().toString());

        if(tSlots < aSlots){
            Toast.makeText(getContext(), "Total slots cannot be less than available slots", Toast.LENGTH_SHORT).show();
            button.setEnabled(true);
            return;
        }


        ParkingLocation parkingLocation = new ParkingLocation(
                name,
                lattitude,
                longitude,
                address,
                desc,
                tSlots,
                aSlots,
                pCost,
                uid,
                mapLink,
                id
        );

        progressDialog.show();
        databaseReference.child("Locations").child(id).setValue(parkingLocation)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if(task.isSuccessful()){
                        Toast.makeText(getActivity(), "Successfully added", Toast.LENGTH_SHORT).show();
                        if(popWindow.isShowing())
                            popWindow.dismiss();
                        getActivity().getSupportFragmentManager().beginTransaction().remove(new AddNewParkingFragment());

                    }


                });

    }

    //genares random secured id
    public String generateId() {
        final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();

        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }


    //snackbar
    public void showSnackbar(String msg) {
        snackbar= Snackbar
                .make(getActivity().findViewById(android.R.id.content), msg, Snackbar.LENGTH_INDEFINITE)
                .setAction("Got it", v -> snackbar.dismiss());
        snackbar.setActionTextColor(getResources().getColor(R.color.yello));
        snackbar.show();
    }

}
