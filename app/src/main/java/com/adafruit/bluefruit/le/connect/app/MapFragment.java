package com.adafruit.bluefruit.le.connect.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.adafruit.bluefruit.le.connect.R;
import com.adafruit.bluefruit.le.connect.ble.central.UartDataManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class MapFragment extends ConnectedPeripheralFragment implements UartDataManager.UartDataManagerListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private Button btnTimer;
    private TextView tvTimer;
    private boolean isRunning = false;
    private Handler timerHandler = new Handler();
    private long startTime = 0;
    private GeoPoint lastKnownLocation;
    private boolean isFirstLocationUpdate = true; // Flaga do centrowania tylko przy pierwszej aktualizacji

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Konfiguracja Osmdroid
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        Configuration.getInstance().setTileFileSystemCacheMaxBytes(50L * 1024 * 1024); // Pamięć podręczna
        Configuration.getInstance().setCacheMapTileCount((short) 9); // Mniejsza liczba kafelków
        Configuration.getInstance().setCacheMapTileOvershoot((short) 2); // Mniejsze kafelki poza ekranem
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicjalizacja mapy
        mapView = view.findViewById(R.id.map_view);
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(12.0); // Mniejszy zoom na starcie

        // Przyciski
        btnTimer = view.findViewById(R.id.btn_timer);
        tvTimer = view.findViewById(R.id.tv_timer);
        Button btnCenterMap = view.findViewById(R.id.btn_center_map);

        // Obsługa przycisku do centrowania mapy
        btnCenterMap.setOnClickListener(v -> {
            if (lastKnownLocation != null) {
                mapView.getController().setCenter(lastKnownLocation);
                mapView.getController().setZoom(17.0);
            }
        });

        // Obsługa przycisku Start/Stop
        btnTimer.setOnClickListener(v -> toggleTimer());

        // Inicjalizacja klienta lokalizacji
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Sprawdź uprawnienia lokalizacji
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            requestLocationUpdates();
        }
    }

    private void toggleTimer() {
        if (isRunning) {
            isRunning = false;
            btnTimer.setText("Start");
            timerHandler.removeCallbacks(updateTimerRunnable);
        } else {
            isRunning = true;
            btnTimer.setText("Stop");
            startTime = SystemClock.elapsedRealtime();
            timerHandler.post(updateTimerRunnable);
        }
    }

    private final Runnable updateTimerRunnable = new Runnable() {
        @Override
        public void run() {
            long elapsedMillis = SystemClock.elapsedRealtime() - startTime;
            int seconds = (int) (elapsedMillis / 1000);
            int minutes = seconds / 60;
            int hours = minutes / 60;
            seconds %= 60;
            minutes %= 60;

            tvTimer.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
            timerHandler.postDelayed(this, 1000);
        }
    };

    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(2000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() != null) {
                    // Aktualizuj mapę z nową lokalizacją
                    updateMapWithLocation(locationResult.getLastLocation());

                    // Wykonaj centrowanie tylko przy pierwszej lokalizacji
                    if (isFirstLocationUpdate) {
                        centerMap(locationResult.getLastLocation());
                        isFirstLocationUpdate = false; // Ustaw flagę, aby zapobiec kolejnym centrowaniom
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void centerMap(Location location) {
        if (location != null) {
            lastKnownLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            mapView.getController().setCenter(lastKnownLocation);
            mapView.getController().setZoom(17.0);
        }
    }

    private void updateMapWithLocation(Location location) {
        if (location == null) {
            return; // Zabezpieczenie przed null
        }
        lastKnownLocation = new GeoPoint(location.getLatitude(), location.getLongitude());

        // Dodanie markera
        mapView.getOverlays().clear();
        Marker marker = new Marker(mapView);
        marker.setPosition(lastKnownLocation);
        marker.setTitle("Twoja lokalizacja");
        mapView.getOverlays().add(marker);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) {
            mapView.onDetach();
        }
    }

    @Override
    public void onUartRx(@NonNull byte[] data, @Nullable String peripheralIdentifier) {
    }

    public static MapFragment newInstance(@Nullable String singlePeripheralIdentifier) {
        MapFragment fragment = new MapFragment();
        fragment.setArguments(createFragmentArgs(singlePeripheralIdentifier));
        return fragment;
    }
}
