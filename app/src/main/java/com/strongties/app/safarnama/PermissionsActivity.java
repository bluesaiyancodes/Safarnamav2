package com.strongties.app.safarnama;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class PermissionsActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final String TAG = "PermissionsActivity";

    public static final int REQUEST_CHECK_SETTINGS = 99;
    private final static long UPDATE_INTERVAL = 4 * 1000;  /* 4 secs */
    private final static long FASTEST_INTERVAL = 2000; /* 2 sec */

    private FusedLocationProviderClient mFusedLocationClient;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    boolean isMockLocation;


    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        // Checking if mock location is enabled
        prefs = getSharedPreferences("safarnamaPreferences", MODE_PRIVATE);
        editor = prefs.edit();
        isMockLocation = prefs.getBoolean("isMockLocation", false);

        // If mock location is not enabled get true location
        if(!isMockLocation){

            //Initialize Fusedlocationclient
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            // Set Proceed Button
            MaterialButton proceed_btn = findViewById(R.id.permissions_proceed);
            proceed_btn.setOnClickListener(view -> {
                view.setVisibility(View.INVISIBLE);
                getLocation();
            });

            //Check Location Permission
            String permission = ACCESS_FINE_LOCATION;
            if (EasyPermissions.hasPermissions(this, permission)) {

                // get Location and start MainActivity
                getLocation();

            } else {
                // Request Permission
                EasyPermissions.requestPermissions(this, "Safarnama needs to access your current location.", REQUEST_CHECK_SETTINGS, permission);
            }

        }



    }

    private void getLocation() {

        // ---------------------------------- LocationRequest ------------------------------------
        // Create the location request to start receiving updates
        LocationRequest mLocationRequestHighAccuracy = new LocationRequest();
        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequestHighAccuracy.setInterval(UPDATE_INTERVAL);
        mLocationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequestHighAccuracy.setNumUpdates(1);


        // Check for Location Permission, stop service if permission not provided.
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mFusedLocationClient.requestLocationUpdates(mLocationRequestHighAccuracy, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        Location location = locationResult.getLastLocation();
                        if (location != null) {
                            // Log.d(TAG, location.toString());

                            // Save the location details to cache
                            editor.putString("cachedLat", Double.toString(location.getLatitude()));
                            editor.putString("cachedLon", Double.toString(location.getLongitude()));
                            editor.apply();

                            // Got Location, now call MainActivity
                            Intent intent = new Intent(PermissionsActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                },
                Looper.myLooper());
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)){

            new AppSettingsDialog.Builder(this).build().show();
        }

    }

}