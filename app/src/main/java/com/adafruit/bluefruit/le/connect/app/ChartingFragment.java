package com.adafruit.bluefruit.le.connect.app;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.adafruit.bluefruit.le.connect.R;
import com.adafruit.bluefruit.le.connect.ble.central.UartDataManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ChartingFragment extends ConnectedPeripheralFragment implements UartDataManager.UartDataManagerListener {



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_charting, container, false);
    }
    @Override
    public void onUartRx(@NonNull byte[] data, @Nullable String peripheralIdentifier) {

    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);






    }

    public static ChartingFragment newInstance(@Nullable String singlePeripheralIdentifier) {
        ChartingFragment fragment = new ChartingFragment();
        fragment.setArguments(createFragmentArgs(singlePeripheralIdentifier));
        return fragment;
    }



}
