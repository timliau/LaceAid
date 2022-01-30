package com.sp.laceaid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class RecordRunActivity extends FragmentActivity implements OnMapReadyCallback{
//public class RecordRunActivity extends AppCompatActivity {

    public static final int DEFAULT_UPDATE_INTERVAL = 10;
    public static final int FAST_UPDATE_INTERVAL = 5;
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    private GoogleMap map;
    private LatLng userLocation;
    TextView tv_steps, tv_time, tv_distance, tv_pace;
    Button startButton, endButton;
    Boolean isTracking = false;
    LocationRequest locationRequest;

    LocationCallback locationCallBack;

    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_run);

        tv_steps = findViewById(R.id.tv_steps);
        tv_steps.setText("0");
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
            }
        };

//        startButton.setOnClickListener(view -> startLocationUpdates());
        startButton.setOnClickListener(view -> {
            if (isTracking == false){
                startLocationUpdates();
                isTracking = true;
                startButton.setText("Pause Run");
            } else {
                endLocationUpdates();
                isTracking = false;
                startButton.setText("Resume Run");
            }
        });
        endButton.setOnClickListener(view -> endLocationUpdates());

        updateGPS();
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        Toast.makeText(this, "Run Activity Started", Toast.LENGTH_SHORT).show();
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        updateGPS();
    }
    private void endLocationUpdates() {
        Toast.makeText(this, "Run Activity Ended", Toast.LENGTH_SHORT).show();
        tv_steps.setText("Activity Ended");
        tv_pace.setText("Activity Ended");
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
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
                if (location == null){
                    Toast.makeText(this, "Location is null bro", Toast.LENGTH_SHORT).show();
                }
                updateUIValues(location);
            });
        } else {
            // Permission not granted yet
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }

        }
    }

    private void updateUIValues(Location location) {

        try {
            // This Code is causing app to crash
            if (location.hasSpeed()) {
                tv_pace.setText(String.valueOf(location.getSpeed()) + " km/h");
            } else {
                tv_pace.setText("0.0 km/h");
            }
            tv_steps.setText(String.valueOf(location.getLatitude()));

            userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            map.addMarker(new MarkerOptions().position(userLocation).title("My Location"));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

        } catch (NullPointerException e){
            Toast.makeText(this, "Location is null", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

//        userLocation = new LatLng(50,50);
//        map.addMarker(new MarkerOptions().position(userLocation).title("My Location"));
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
    }
}