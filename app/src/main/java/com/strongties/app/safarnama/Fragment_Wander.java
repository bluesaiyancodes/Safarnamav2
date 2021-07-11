package com.strongties.app.safarnama;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.Task;

public class Fragment_Wander extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "Wonder Fragment";

    View v;
    private GoogleMap googleMap;
    private View locationButton;
    private View mapView;
    FusedLocationProviderClient fusedLocationProviderClient;

    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment__wander, container, false);

        context = getContext();

        // Initialise FusionLocationProvider Client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        // Get Map View from Mapfragment
        mapView = mapFragment.getView();
        mapFragment.getMapAsync(this);


        return v;

    }

    public void updateMap(Location location, int zoom){
        assert  googleMap != null;
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoom));
    }

    @Override
    public void onMapReady(GoogleMap mMap) {
        googleMap = mMap;
        googleMap.getUiSettings().setAllGesturesEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(false);

        //Set Night or Day mode for Gmap
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_night));
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_day));
                break;
        }


        // Create a location task to fetch location
        Task<Location> locationTask;

        // Permission Check
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // Enable current location
        googleMap.setMyLocationEnabled(true);

        // Fetch last known location
        locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                if(task.getResult()!=null){
                    // Move camera to last known location
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude()), 14));
                }
            }
        });

        // Find the "Go to current location" button and make it invisible

        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the location button view
            locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            locationButton.setVisibility(View.GONE);


        }

        ImageView iv_location = v.findViewById(R.id.gmap_ic_location);
        iv_location.setOnClickListener(view -> {
            locationButton.callOnClick();
        });


    }


}