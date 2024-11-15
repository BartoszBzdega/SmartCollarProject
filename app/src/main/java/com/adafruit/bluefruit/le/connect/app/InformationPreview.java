package com.adafruit.bluefruit.le.connect.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.adafruit.bluefruit.le.connect.R;
import com.adafruit.bluefruit.le.connect.ble.central.UartDataManager;

public class InformationPreview extends ConnectedPeripheralFragment implements UartDataManager.UartDataManagerListener {

    public static InformationPreview newInstance(@Nullable String singlePeripheralIdentifier) {
        InformationPreview fragment = new InformationPreview();
        fragment.setArguments(createFragmentArgs(singlePeripheralIdentifier));
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_information_preview, container, false);
    }
    @Override
    public void onUartRx(@NonNull byte[] data, @Nullable String peripheralIdentifier) {

    }

    //TODO: 2 wykresy dodac zeby widoczne byly
    //1) wykres dni od czasu spaceru (wykres liniowy)
    //2) wykres dni od dystansu pokonanego podczas spacer (wykres kolumnowy)
    // mowiac o dniach, jest to przedzial czasowy gdzie byla uruchomiona aplikacja, mozna wybrac od kiedy do kiedy pokazujemy dane (np tydzien, docelowo - od wybranej do wybranej daty)
}
