package com.sp.laceaid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class RecordRunActivity extends FragmentActivity implements OnMapReadyCallback{

    public static final int DEFAULT_UPDATE_INTERVAL = 1;
    public static final int FAST_UPDATE_INTERVAL = 1;
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    private GoogleMap map;

    private RecordHelper helper;

    Polyline route = null;
    ArrayList<LatLng> routePoints = new ArrayList<>();
    double totalDistance;
    long totalTime;
    private LatLng userLocation;
    Marker currentLocationMarker;
    TextView tv_currentLocation, tv_time, tv_distance, tv_pace;
    Button startButton, endButton;
    Boolean runStarted = false, runInProgress = false;
    LocationRequest locationRequest;
    long startTime, min, sec, hour, pauseDifference = 0;
    String displayTime;

    LocationCallback locationCallBack;

    FusedLocationProviderClient fusedLocationProviderClient;

    // firebase
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_run);

        // grab data from firebase realtime db
        user = FirebaseAuth.getInstance().getCurrentUser();
        // need to add the url as the default location is USA not SEA
        databaseReference = FirebaseDatabase.getInstance("https://lace-aid-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        username = user.getUid();


        tv_currentLocation = findViewById(R.id.tv_currentLocation);
        tv_currentLocation.setText("Not Tracking Location");
        tv_time = findViewById(R.id.tv_time);
        tv_time.setText("00:00");
        tv_distance = findViewById(R.id.tv_distance);
        tv_distance.setText("0 km");
        tv_pace = findViewById(R.id.tv_pace);
        tv_pace.setText("0 km/h");
        startButton = findViewById(R.id.startButton);
        endButton = findViewById(R.id.endButton);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        locationCallBack = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                updateUIValues(locationResult.getLastLocation());
                drawPolyline(locationResult.getLastLocation());
            }
        };

//        startButton.setOnClickListener(view -> startLocationUpdates());
        startButton.setOnClickListener(view -> {
            if (runInProgress == false){
                startRun();
                runInProgress = true;
                startButton.setText("Pause Run");
            } else {
                pauseRun();
                startButton.setText("Resume Run");
                runInProgress = false;
            }
        });
        endButton.setOnClickListener(view -> {
            if (runStarted){
                startButton.setText("Resume Run");
                runInProgress = false;
                endRun();
            } else finish();

        });

        updateGPS();
    }

    @SuppressLint("MissingPermission")
    private void startRun() {
        if (runStarted == false){
            startTime = System.currentTimeMillis();
            min = 0;
            sec = 0;
            runStarted = true;
        }
        if (pauseDifference != 0){
            startTime += (System.currentTimeMillis() - pauseDifference);
            pauseDifference = 0;
        }
        Toast.makeText(this, "Run Activity Started", Toast.LENGTH_SHORT).show();
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        updateGPS();
    }

    private void pauseRun() {
        pauseDifference = System.currentTimeMillis();
        Toast.makeText(this, "Run Activity Paused", Toast.LENGTH_SHORT).show();
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    private void endRun() {
        pauseDifference = System.currentTimeMillis();
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
        new AlertDialog.Builder(RecordRunActivity.this)
                .setTitle("End Run")
                .setMessage("Do you want to save this activity?")
                .setCancelable(false)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            onSave();
                            runStarted = false;
                            startButton.setText("Start New Run");
                            routePoints.clear();
                            startTime = 0;
                            pauseDifference = 0;
                            if (route != null)
                                route.remove();
                            Toast.makeText(getApplicationContext(), "Run Saved", Toast.LENGTH_SHORT).show();
                        } catch (Exception e){
                            Toast.makeText(getApplicationContext(), "Unable to save run", Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //set what should happen when negative button is clicked

                    }
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS();
                }
                else {
                    Toast.makeText(this, "This app requires permission to be granted in order to work properly", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    private void updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(RecordRunActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // User provided the permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                // We got permissions. Put values of location into UI components
                if (location != null){
                    updateUIValues(location);
                    if (!runStarted) {
                        tv_currentLocation.setText("Not Tracking Location");
                        tv_time.setText("0:00:00");
                        tv_pace.setText("0 km/h");
                    }
                }

            });
        } else {
            // Permission not granted yet
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }

        }
    }

    @SuppressLint("MissingPermission")
    private void updateUIValues(Location location) {

        try {
            if (location.hasSpeed()) {
                double minPerKmSpeedConversion = 1000 / (location.getSpeed() * 60);
                double leftover = minPerKmSpeedConversion % 1;
                double minutes = minPerKmSpeedConversion - leftover;
                double seconds = Math.round(leftover * 60);
                if (seconds >= 10)
                    tv_pace.setText(String.valueOf((int)minutes+":"+(int)seconds) + " min/km");
                else
                    tv_pace.setText(String.valueOf((int)minutes+":0"+(int)seconds) + " min/km");
            } else {
                tv_pace.setText("0.0 km/h");
            }
            double roundedDistance = (double) Math.round(calculateTotalDistance() * 100) / 100;
            tv_distance.setText(String.valueOf(roundedDistance) + " km");

        } catch (NullPointerException e){
            Toast.makeText(this, "Location is null", Toast.LENGTH_SHORT).show();
        }

        // Geocoding address
        Geocoder geocoder = new Geocoder(RecordRunActivity.this);
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            tv_currentLocation.setText(addresses.get(0).getAddressLine(0));
        } catch (Exception e){
            tv_currentLocation.setText("Unable to get street address");
        }

        // User Location Marker
        userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }

        map.setMyLocationEnabled(true);

        // Move camera during run
        map.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
        map.animateCamera(CameraUpdateFactory.zoomTo(15));

        // Update Timer
        long difference = System.currentTimeMillis() - startTime;
        totalTime = difference / 1000;
        sec = totalTime % 60;
        min = totalTime / 60;
        hour = 0;
        while(min >= 60){
            min = 0;
            hour++;
        }

        if (min > 10 && sec < 10)
            displayTime = hour + ":" + min + ":0" + sec;
        else if (min < 10 && sec < 10)
            displayTime = hour + ":0" + min + ":0" + sec;
        else if (min < 10)
            displayTime = hour + ":0" + min + ":" + sec;
        else
            displayTime = hour + ":" + min + ":" + sec;

        tv_time.setText(displayTime);
    }

    private void drawPolyline (Location location) {
        if (route != null)
            route.remove();
        LatLng prevLocation = new LatLng(location.getLatitude(), location.getLongitude());
        routePoints.add(prevLocation);
        PolylineOptions polylineOptions = new PolylineOptions().addAll(routePoints);
        polylineOptions.color(Color.parseColor("#46A234"));
        route = map.addPolyline(polylineOptions);
    }

    double calculateTotalDistance() {
        int i = 1;
        totalDistance = 0;
        while (i < routePoints.size()){
            totalDistance = totalDistance + distance(routePoints.get(i).latitude, routePoints.get(i).longitude , routePoints.get(i-1).latitude, routePoints.get(i-1).longitude);
            i++;
        }
        return totalDistance;
    }

    private static double distance(double lat1, double lon1, double lat2, double lon2) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515 * 1.609344;
            return (dist);
        }
    }



    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
    }

//    private void saveRun(){
//        helper.insertInfo(
//                "" + user,
//                "" + totalTime,
//                totalDistance
//        );
//    }

    public void onSave(){
        BackgroundWorker backgroundWorker = new BackgroundWorker(this);
        backgroundWorker.execute("insert", username, displayTime, ""+totalDistance);
    }


}









