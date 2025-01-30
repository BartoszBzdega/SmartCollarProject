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
import android.util.Log;
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
import org.osmdroid.views.overlay.Polyline;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MapFragment extends ConnectedPeripheralFragment implements UartDataManager.UartDataManagerListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private Button btnTimer;
    private TextView tvTimer;
    private TextView tvDistance;   // Wyświetlacz dystansu
    private boolean isRunning = false;
    private Handler timerHandler = new Handler();
    private long startTime = 0;
    private GeoPoint lastKnownLocation;
    private double distance = 0.0; // Przebyty dystans w metrach

    private Marker locationMarker;
    private Marker startMarker;
    private Marker stopMarker;
    private Polyline trackLine;
    private List<GeoPoint> trackPoints;

    private DataStorage dataStorage; // Obiekt DataStorage

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        // Inicjalizacja DataStorage
        dataStorage = new DataStorage();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = view.findViewById(R.id.map_view);
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // Ustawienie domyślnego poziomu przybliżenia
        mapView.getController().setZoom(15.0);  // Możesz dostosować wartość przybliżenia

        btnTimer = view.findViewById(R.id.btn_timer);
        tvTimer = view.findViewById(R.id.tv_timer);
        tvDistance = view.findViewById(R.id.tv_distance);
        Button btnCenterMap = view.findViewById(R.id.btn_center_map);

        btnCenterMap.setOnClickListener(v -> {
            if (lastKnownLocation != null) {
                mapView.getController().setCenter(lastKnownLocation);
                mapView.getController().setZoom(17.0);
            }
        });

        btnTimer.setOnClickListener(v -> toggleTimer());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            requestLocationUpdates();
        }

        trackPoints = new ArrayList<>();
        trackLine = new Polyline();
        trackLine.setWidth(10f);
        mapView.getOverlays().add(trackLine);
    }


    private void toggleTimer() {
        if (!isRunning && btnTimer.getText().equals("Start")) {
            isRunning = true;
            btnTimer.setText("Stop");
            startTime = SystemClock.elapsedRealtime();
            timerHandler.post(updateTimerRunnable);

            if (startMarker != null) mapView.getOverlays().remove(startMarker);
            if (stopMarker != null) mapView.getOverlays().remove(stopMarker);
            trackPoints.clear();
            trackLine.setPoints(trackPoints);
            distance = 0.0; // Reset dystansu
            tvDistance.setText("Dystans: 0 m");

            if (lastKnownLocation != null) {
                startMarker = new Marker(mapView);
                startMarker.setPosition(lastKnownLocation);
                startMarker.setTitle("Początek trasy");
                startMarker.setIcon(scaleMarkerIcon(R.drawable.ic_start_marker, 0.08f));
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mapView.getOverlays().add(startMarker);
                trackPoints.add(lastKnownLocation);
            }
        } else if (isRunning && btnTimer.getText().equals("Stop")) {
            isRunning = false;
            btnTimer.setText("Wyczyść");
            timerHandler.removeCallbacks(updateTimerRunnable);

            if (lastKnownLocation != null) {
                stopMarker = new Marker(mapView);
                stopMarker.setPosition(lastKnownLocation);
                stopMarker.setTitle("Koniec trasy");
                stopMarker.setIcon(scaleMarkerIcon(R.drawable.ic_stop_marker, 0.08f));
                stopMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mapView.getOverlays().add(stopMarker);
                trackPoints.add(lastKnownLocation);
            }
            trackLine.setPoints(trackPoints);

            // Zapisz dane do bazy po zakończeniu stopera
            File path = requireContext().getFilesDir(); // Ścieżka do pamięci wewnętrznej
            long elapsedTime = (SystemClock.elapsedRealtime() - startTime) / 1000; // Czas w sekundach
            dataStorage.saveWalkData((float) distance, (float) elapsedTime, path);
        } else if (!isRunning && btnTimer.getText().equals("Wyczyść")) {
            btnTimer.setText("Start");
            tvTimer.setText("00:00:00");
            tvDistance.setText("Dystans: 0 m");
            distance = 0.0;
            lastKnownLocation = null;
            if (startMarker != null) mapView.getOverlays().remove(startMarker);
            if (stopMarker != null) mapView.getOverlays().remove(stopMarker);
            trackPoints.clear();
            trackLine.setPoints(trackPoints);
        }

        mapView.invalidate();
    }

    private Drawable scaleMarkerIcon(int drawableId, float scale) {
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
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)  // Wysoka dokładność GPS
                .setInterval(2000)           // Odświeżanie co 2 sekundy
                .setFastestInterval(1000)    // Najszybsze odświeżanie co 1 sekundę
                .setMaxWaitTime(3000);       // Maksymalny czas oczekiwania (3 sekundy)

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() != null) {
                    Location newLocation = locationResult.getLastLocation();

                    // Sprawdzenie dokładności lokalizacji
                    if (newLocation.hasAccuracy() && newLocation.getAccuracy() < 20.0) {
                        GeoPoint newGeoPoint = new GeoPoint(newLocation.getLatitude(), newLocation.getLongitude());

                        // **Centrowanie mapy przy pierwszym odświeżeniu lokalizacji**
                        if (lastKnownLocation == null) {
                            mapView.getController().setCenter(newGeoPoint);
                            mapView.getController().setZoom(17.0);
                        }

                        // **Aktualizacja ścieżki i dystansu tylko, gdy stoper jest uruchomiony**
                        if (isRunning) {
                            if (lastKnownLocation != null) {
                                Location previousLocation = new Location("");
                                previousLocation.setLatitude(lastKnownLocation.getLatitude());
                                previousLocation.setLongitude(lastKnownLocation.getLongitude());

                                Location currentLocation = new Location("");
                                currentLocation.setLatitude(newGeoPoint.getLatitude());
                                currentLocation.setLongitude(newGeoPoint.getLongitude());

                                float distanceBetweenPoints = previousLocation.distanceTo(currentLocation);

                                if (distanceBetweenPoints > 0.1) {
                                    distance += distanceBetweenPoints;
                                    tvDistance.setText(String.format("Dystans: %.2f m", distance));
                                }
                            }

                            // Dodanie punktu do ścieżki, gdy stoper jest uruchomiony
                            trackPoints.add(newGeoPoint);
                            trackLine.setPoints(trackPoints);
                        }

                        // Aktualizacja pozycji
                        lastKnownLocation = newGeoPoint;
                        updateMapWithLocation(newLocation);
                    } else {
                        Log.d("Distance Tracker", "Pominięto lokalizację z powodu niskiej dokładności: " + newLocation.getAccuracy() + " m");
                    }
                }
            }
        };


        // Sprawdzenie uprawnień i rozpoczęcie aktualizacji lokalizacji
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }





    private void updateMapWithLocation(Location location) {
        if (location == null) return;

        lastKnownLocation = new GeoPoint(location.getLatitude(), location.getLongitude());

        if (locationMarker == null) {
            locationMarker = new Marker(mapView);
            locationMarker.setTitle("Twoja lokalizacja");
            locationMarker.setIcon(scaleMarkerIcon(R.drawable.ic_user_location, 0.1f));
            locationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(locationMarker);
        }

        locationMarker.setPosition(lastKnownLocation);
        mapView.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) mapView.onDetach();
    }

    @Override
    public void onUartRx(@NonNull byte[] data, @Nullable String peripheralIdentifier) {}

    public static MapFragment newInstance(@Nullable String singlePeripheralIdentifier) {
        MapFragment fragment = new MapFragment();
        fragment.setArguments(createFragmentArgs(singlePeripheralIdentifier));
        return fragment;
    }
}
