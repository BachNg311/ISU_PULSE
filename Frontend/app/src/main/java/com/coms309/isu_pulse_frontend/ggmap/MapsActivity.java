package com.coms309.isu_pulse_frontend.ggmap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.coms309.isu_pulse_frontend.MainActivity;
import com.coms309.isu_pulse_frontend.R;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private ImageButton backButton;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker currentLocationMarker;
    private Marker fromMarker, toMarker;

    private LatLng currentLatLng, fromLatLng, toLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        backButton = findViewById(R.id.back_button);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(MapsActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // Initialize Places API
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));

        // Initialize Map Fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up autocomplete fragments
        setupAutocompleteFragments();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Set better map style and enable UI settings
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);

        fetchLocationUpdates();
    }

    private void fetchLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        mMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        updateCurrentLocationOnMap(location);
                    }
                });

        fusedLocationClient.requestLocationUpdates(
                LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(1000),
                locationCallback,
                getMainLooper()
        );
    }

    private void updateCurrentLocationOnMap(Location location) {
        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (currentLocationMarker == null) {
            currentLocationMarker = mMap.addMarker(new MarkerOptions()
                    .position(currentLatLng)
                    .title("Your Current Location"));
        } else {
            currentLocationMarker.setPosition(currentLatLng);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) return;
            for (Location location : locationResult.getLocations()) {
                updateCurrentLocationOnMap(location);
            }
        }
    };

    private void calculateRoute() {
        mMap.clear();

        // Handle cases for markers and routes
        if (fromLatLng == null && toLatLng == null) {
            // Reset to current location if both are null
            if (currentLatLng != null) {
                currentLocationMarker = mMap.addMarker(new MarkerOptions()
                        .position(currentLatLng)
                        .title("Your Current Location"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
            }
            return;
        }

        if (fromLatLng != null && toLatLng == null) {
            // Show "From" marker only
            fromMarker = mMap.addMarker(new MarkerOptions()
                    .position(fromLatLng)
                    .title("From Location"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(fromLatLng, 15));
            return;
        }

        if (fromLatLng != null && toLatLng != null) {
            // Build the Directions API URL
            String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=" + fromLatLng.latitude + "," + fromLatLng.longitude +
                    "&destination=" + toLatLng.latitude + "," + toLatLng.longitude +
                    "&key=" + getString(R.string.google_maps_key);

            // Use Volley to make the HTTP request
            RequestQueue queue = Volley.newRequestQueue(this);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            JSONArray routes = response.getJSONArray("routes");
                            if (routes.length() > 0) {
                                JSONObject route = routes.getJSONObject(0);
                                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                                String encodedPath = overviewPolyline.getString("points");

                                // Decode and display the route
                                List<LatLng> path = PolyUtil.decode(encodedPath);
                                displayRoute(path);
                            } else {
                                Toast.makeText(this, "No routes found", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Toast.makeText(this, "Error fetching route", Toast.LENGTH_SHORT).show();
                    });

            queue.add(request);
        }
    }

    private void displayRoute(List<LatLng> path) {
        mMap.addPolyline(new PolylineOptions().addAll(path));
        if (!path.isEmpty()) {
            fromMarker = mMap.addMarker(new MarkerOptions()
                    .position(path.get(0))
                    .title("Start"));
            toMarker = mMap.addMarker(new MarkerOptions()
                    .position(path.get(path.size() - 1))
                    .title("End"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(path.get(0), 12));
        }
    }

    private void setupAutocompleteFragments() {
        AutocompleteSupportFragment fromFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.from_autocomplete);
        AutocompleteSupportFragment toFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.to_autocomplete);

        fromFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        toFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));

        fromFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                fromLatLng = place.getLatLng();
                calculateRoute();
            }

            @Override
            public void onError(@NonNull com.google.android.gms.common.api.Status status) {
                Toast.makeText(MapsActivity.this, "Error selecting From place", Toast.LENGTH_SHORT).show();
            }
        });

        toFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                toLatLng = place.getLatLng();
                calculateRoute();
            }

            @Override
            public void onError(@NonNull com.google.android.gms.common.api.Status status) {
                Toast.makeText(MapsActivity.this, "Error selecting To place", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}
