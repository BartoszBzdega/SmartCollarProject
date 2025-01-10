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

public class InformationPreview extends ConnectedPeripheralFragment implements UartDataManager.UartDataManagerListener {

    private Button buttonWalk;
    private Button buttonPetData;
    private Button readWalk;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //TODO: dodac tutaj button ktory wysyla dane do pliku i zobaczc czy w pliku sa

        return inflater.inflate(R.layout.fragment_information_preview, container, false);
    }
    @Override
    public void onUartRx(@NonNull byte[] data, @Nullable String peripheralIdentifier) {

    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        buttonWalk=(Button)getActivity().findViewById(R.id.buttonWalk);

        buttonWalk.setOnClickListener(v -> {
            EditText placeholder;
            DataStorage dts = new DataStorage();

            placeholder=getView().findViewById(R.id.editTextDistance);
            float distance = Float.valueOf(placeholder.getText().toString());

            placeholder=getView().findViewById(R.id.editTextTime);
            float time = Float.valueOf(placeholder.getText().toString());

            File path = getActivity().getFilesDir();
            Log.d("I",path.toString());
            dts.saveWalkData(distance,time,path);

        });


        buttonPetData=(Button) getView().findViewById(R.id.buttonPet);
        buttonPetData.setOnClickListener(v->{

            DataStorage dts = new DataStorage();
            EditText placeholder;

            placeholder = getView().findViewById(R.id.editTextName);
            String name = placeholder.getText().toString();

            placeholder = getView().findViewById(R.id.editTextAge);
            int age = Integer.parseInt(placeholder.getText().toString());

            placeholder = getView().findViewById(R.id.editTextWeight);
            float weight = Float.parseFloat(placeholder.getText().toString());

            File path = getActivity().getFilesDir();

            dts.savePetInfo(name,age,weight, path);

        });

        readWalk=(Button)getView().findViewById(R.id.buttonReadWalk);
        readWalk.setOnClickListener(view1 -> {

            DataStorage dts = new DataStorage();

            ArrayList<HashMap<String, String>> formList = dts.readWalkData(getActivity().getFilesDir());

            TextView placeholder = getView().findViewById(R.id.readWalkView);

            String text =formList.get(2).toString();
            String data = getTime(text);
            String distance = getDistance(text);
            Log.d("I",text);
            placeholder.setText(data);

        });
    }

    public static InformationPreview newInstance(@Nullable String singlePeripheralIdentifier) {
        InformationPreview fragment = new InformationPreview();
        fragment.setArguments(createFragmentArgs(singlePeripheralIdentifier));
        return fragment;
    }

    public String getDate(String d)
    {
        String date=new String();
        String []splitDate = d.split(" ");
        String []datDay = splitDate[1].split("=");
        date = datDay[1]+" "+splitDate[2]+" "+splitDate[3]+" "+splitDate[6];
        return date;
    }

    public String getTime(String t)
    {
        String []readT = t.split(",");
        String []readT2 = readT[0].split("=");
        String time=readT2[1];

        return time;
    }

    public String getDistance(String t)
    {
        String []readT = t.split(",");
        String []readT2 = readT[2].split("=");
        String time=readT2[1].substring(0, readT2[1].length()-1);

        return time;
    }


    //TODO: 2 wykresy dodac zeby widoczne byly
    //1) wykres dni od czasu spaceru (wykres liniowy)
    //2) wykres dni od dystansu pokonanego podczas spacer (wykres kolumnowy)
    // mowiac o dniach, jest to przedzial czasowy gdzie byla uruchomiona aplikacja, mozna wybrac od kiedy do kiedy pokazujemy dane (np tydzien, docelowo - od wybranej do wybranej daty)
    //zapis danych do plikow lokalnie  wstepnie zapis uruchomienia aplikacji - data i czas
}
