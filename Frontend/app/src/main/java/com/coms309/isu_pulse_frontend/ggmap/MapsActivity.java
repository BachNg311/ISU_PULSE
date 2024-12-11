package com.coms309.isu_pulse_frontend.ggmap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.coms309.isu_pulse_frontend.MainActivity;
import com.coms309.isu_pulse_frontend.R;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;

public class MapsActivity extends AppCompatActivity {

    private MapView mapView;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Use Mapbox's MapView
        mapView = findViewById(R.id.mapView);
        backButton = findViewById(R.id.back_button);

        // Load a Mapbox style
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(MapsActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
