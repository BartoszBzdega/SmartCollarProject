package com.adafruit.bluefruit.le.connect.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    private boolean isFirstUpdate = true; // Flaga do ustawienia widoku tylko przy pierwszej lokalizacji

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Konfiguracja osmdroid
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        // Inicjalizacja klienta lokalizacji
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicjalizacja mapy
        mapView = view.findViewById(R.id.map_view);
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // Sprawdź uprawnienia lokalizacji
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Poproś o uprawnienia, jeśli ich nie przyznano
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Rozpocznij dynamiczne aktualizacje lokalizacji
            requestLocationUpdates();
        }
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY) // Wysoka dokładność
                .setInterval(5000) // Aktualizacja co 5 sekund
                .setFastestInterval(2000); // Najszybszy możliwy interwał (2 sekundy)

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult.getLastLocation() != null) {
                    Location location = locationResult.getLastLocation();
                    updateMapWithLocation(location);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void updateMapWithLocation(Location location) {
        GeoPoint userLocation = new GeoPoint(location.getLatitude(), location.getLongitude());

        // Ustawienie mapy na aktualną lokalizację tylko przy pierwszej aktualizacji
        if (isFirstUpdate) {
            mapView.getController().setZoom(17.0); // Szczegółowy zoom tylko na początku
            mapView.getController().setCenter(userLocation);
            isFirstUpdate = false;
        }

        // Dodanie lub aktualizacja markera
        mapView.getOverlays().clear(); // Usuń stare markery
        Marker marker = new Marker(mapView);
        marker.setPosition(userLocation);
        marker.setTitle("Twoja lokalizacja");
        mapView.getOverlays().add(marker);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Jeśli uprawnienia zostały przyznane, rozpocznij śledzenie lokalizacji
                requestLocationUpdates();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) {
            mapView.onDetach();
        }
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback); // Zatrzymaj aktualizacje lokalizacji
        }
    }

    @Override
    public void onUartRx(@NonNull byte[] data, @Nullable String peripheralIdentifier) {
        // Obsługa danych UART (jeśli potrzebna)
    }

    public static MapFragment newInstance(@Nullable String singlePeripheralIdentifier) {
        MapFragment fragment = new MapFragment();
        fragment.setArguments(createFragmentArgs(singlePeripheralIdentifier));
        return fragment;
    }
}
