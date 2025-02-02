package com.adafruit.bluefruit.le.connect.app;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UartViewModel extends ViewModel {
    private MutableLiveData<String> bluetoothData = new MutableLiveData<>();

    public void setBluetoothData(String data) {
        bluetoothData.postValue(data);
    }

    public LiveData<String> getBluetoothData() {
        return bluetoothData;
    }
}
