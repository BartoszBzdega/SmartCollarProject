package com.adafruit.bluefruit.le.connect.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

    private Marker locationMarker; // Marker dla lokalizacji użytkownika
    private Marker startMarker;    // Marker dla miejsca rozpoczęcia stopera
    private Marker stopMarker;     // Marker dla miejsca zakończenia stopera

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Konfiguracja Osmdroid
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
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

        // Obsługa przycisku Start/Stop/Wyczyść
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
        if (!isRunning && btnTimer.getText().equals("Start")) {
            // Uruchom stoper
            isRunning = true;
            btnTimer.setText("Stop");
            startTime = SystemClock.elapsedRealtime();
            timerHandler.post(updateTimerRunnable);

            // Usuń stare markery
            if (startMarker != null) {
                mapView.getOverlays().remove(startMarker);
            }
            if (stopMarker != null) {
                mapView.getOverlays().remove(stopMarker);
            }

            // Dodaj marker początkowy w bieżącej lokalizacji
            if (lastKnownLocation != null) {
                startMarker = new Marker(mapView);
                startMarker.setPosition(lastKnownLocation);
                startMarker.setTitle("Początek trasy");
                startMarker.setIcon(scaleMarkerIcon(R.drawable.ic_start_marker, 0.08f));
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mapView.getOverlays().add(startMarker);
            }
        } else if (isRunning && btnTimer.getText().equals("Stop")) {
            // Zatrzymaj stoper
            isRunning = false;
            btnTimer.setText("Wyczyść");
            timerHandler.removeCallbacks(updateTimerRunnable);

            // Dodaj marker końcowy w bieżącej lokalizacji
            if (lastKnownLocation != null) {
                if (stopMarker != null) {
                    mapView.getOverlays().remove(stopMarker); // Usuń poprzedni marker, jeśli istnieje
                }
                stopMarker = new Marker(mapView);
                stopMarker.setPosition(lastKnownLocation);
                stopMarker.setTitle("Koniec trasy");
                stopMarker.setIcon(scaleMarkerIcon(R.drawable.ic_stop_marker, 0.08f)); // Skala
                stopMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mapView.getOverlays().add(stopMarker);
            }
        } else if (!isRunning && btnTimer.getText().equals("Wyczyść")) {
            // Wyczyść stoper
            btnTimer.setText("Start");
            tvTimer.setText("00:00:00");

            // Usuń markery "start" i "stop"
            if (startMarker != null) {
                mapView.getOverlays().remove(startMarker);
                startMarker = null;
            }
            if (stopMarker != null) {
                mapView.getOverlays().remove(stopMarker);
                stopMarker = null;
            }

            mapView.invalidate(); // Odśwież mapę
        }
    }

    private Drawable scaleMarkerIcon(int drawableId, float scale) {
        // Załaduj ikonę jako bitmapę
        Bitmap bitmap = BitmapFactory.decodeResource(requireContext().getResources(), drawableId);
        int width = (int) (bitmap.getWidth() * scale);
        int height = (int) (bitmap.getHeight() * scale);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

        return new BitmapDrawable(requireContext().getResources(), scaledBitmap);
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
                    updateMapWithLocation(locationResult.getLastLocation());
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void updateMapWithLocation(Location location) {
        if (location == null) {
            return; // Zabezpieczenie przed null
        }
        lastKnownLocation = new GeoPoint(location.getLatitude(), location.getLongitude());

        // Aktualizuj marker bieżącej lokalizacji
        if (locationMarker == null) {
            locationMarker = new Marker(mapView);
            locationMarker.setTitle("Twoja lokalizacja");
            mapView.getOverlays().add(locationMarker);
        }

        locationMarker.setPosition(lastKnownLocation); // Aktualizuj pozycję
        mapView.invalidate(); // Odśwież mapę
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
