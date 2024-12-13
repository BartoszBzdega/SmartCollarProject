package com.adafruit.bluefruit.le.connect.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adafruit.bluefruit.le.connect.R;
import com.adafruit.bluefruit.le.connect.ble.central.UartDataManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapFragment extends ConnectedPeripheralFragment implements UartDataManager.UartDataManagerListener {

    private MapView mapView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Konfiguracja osmdroid
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
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

        // Ustawienie początkowego widoku mapy
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(new GeoPoint(52.2297, 21.0122)); // Warszawa

        // Dodanie markera do mapy
        Marker marker = new Marker(mapView);
        marker.setPosition(new GeoPoint(52.2297, 21.0122));
        marker.setTitle("Warszawa");
        mapView.getOverlays().add(marker);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) {
            mapView.onDetach(); // Zwolnij zasoby
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
