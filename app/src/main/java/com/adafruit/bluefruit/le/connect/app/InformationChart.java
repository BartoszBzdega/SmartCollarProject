package com.adafruit.bluefruit.le.connect.app;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.adafruit.bluefruit.le.connect.R;
import com.adafruit.bluefruit.le.connect.ble.central.UartDataManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.model.GradientColor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class InformationChart extends ConnectedPeripheralFragment implements UartDataManager.UartDataManagerListener{

    private BarChart chart;
    private BarChart chartTime;
    private Button buttonWeek;
    private Button buttonMonth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_charting, container, false);
    }

    @Override
    public void onUartRx(@NonNull byte[] data, @Nullable String peripheralIdentifier) {

    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().getWindow().setFlags
                (WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getActivity().setContentView(R.layout.activity_main);
        getActivity().setTitle("BarChartActivity");

        // Initialize the views using findViewById
        chart = view.findViewById(R.id.chart); // Make sure your layout has a BarChart with this ID
        chartTime = view.findViewById(R.id.chartTime); // Make sure your layout has a BarChart with this ID

        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.getDescription().setEnabled(false);
        // if more than 60 entries are displayed in the chart, no values will be drawn
        chart.setMaxVisibleValueCount(60);
        // scaling can now only be done on x- and y-axis separately
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);

        chartTime.setDrawBarShadow(false);
        chartTime.setDrawValueAboveBar(true);
        chartTime.getDescription().setEnabled(false);
        // if more than 60 entries are displayed in the chart, no values will be drawn
        chartTime.setMaxVisibleValueCount(60);
        // scaling can now only be done on x- and y-axis separately
        chartTime.setPinchZoom(false);
        chartTime.setDrawGridBackground(false);

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);

        Legend lTime = chartTime.getLegend();
        lTime.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        lTime.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        lTime.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        lTime.setDrawInside(false);
        lTime.setForm(Legend.LegendForm.SQUARE);
        lTime.setFormSize(9f);
        lTime.setTextSize(11f);
        lTime.setXEntrySpace(4f);

        setData(7, 100);
        setDataTime(7, 100);
        chart.invalidate();  // Refresh the chart with new data
        chartTime.invalidate();  // Refresh the chart with new data
        chart.invalidate();  // Refresh the chart with new data

        chartTime.invalidate();  // Refresh the chart with new data
        buttonWeek=(Button)getView().findViewById(R.id.buttonWeek);
        buttonWeek.setOnClickListener(v->{
            setData(7, 100);
        setDataTime(7, 100);
        chart.invalidate();
        chartTime.invalidate();} );



        buttonMonth=(Button)getView().findViewById(R.id.buttonMonth);
        buttonMonth.setOnClickListener(v->{
            setData(31, 100);
            setDataTime(31, 100);
            chart.invalidate();
            chartTime.invalidate();} );


    }

    public static InformationChart newInstance(@Nullable String singlePeripheralIdentifier) {
        InformationChart fragment = new InformationChart();
        fragment.setArguments(createFragmentArgs(singlePeripheralIdentifier));
        return fragment;
    }

    private void setData(int count, float range) {

        int start = 0;
        ArrayList<BarEntry> values = new ArrayList<>();

        DataStorage dts = new DataStorage();

        ArrayList<HashMap<String, String>> formList = dts.readWalkData(getActivity().getFilesDir());

        int maxNumOfFields =formList.size();

        if(formList.size()>count)start = formList.size()-count;
        Log.d("i", String.valueOf(formList.size()));

        for (int i = start; i <  maxNumOfFields; i++) {

            String text =formList.get(i).toString();
            String data = getDistance(text);
            float val = Float.parseFloat(data);
            values.add(new BarEntry(i, val));
        }

        BarDataSet set1;
        if (chart.getData() != null && chart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        }else {
            set1 = new BarDataSet(values, "The year 2017");
            set1.setDrawIcons(false);
            Context context = getContext();
            int startColor1 = ContextCompat.getColor(context, android.R.color.holo_orange_light);
            int startColor2 = ContextCompat.getColor(context, android.R.color.holo_blue_light);
            int startColor3 = ContextCompat.getColor(context, android.R.color.holo_orange_light);
            int startColor4 = ContextCompat.getColor(context, android.R.color.holo_green_light);
            int startColor5 = ContextCompat.getColor(context, android.R.color.holo_red_light);
            int endColor1 = ContextCompat.getColor(context, android.R.color.holo_blue_dark);
            int endColor2 = ContextCompat.getColor(context, android.R.color.holo_purple);
            int endColor3 = ContextCompat.getColor(context, android.R.color.holo_green_dark);
            int endColor4 = ContextCompat.getColor(context, android.R.color.holo_red_dark);
            int endColor5 = ContextCompat.getColor(context, android.R.color.holo_orange_dark);

            List<GradientColor> gradientFills = new ArrayList<>();
            gradientFills.add(new GradientColor(startColor1, endColor1));
            gradientFills.add(new GradientColor(startColor2, endColor2));
            gradientFills.add(new GradientColor(startColor3, endColor3));
            gradientFills.add(new GradientColor(startColor4, endColor4));
            gradientFills.add(new GradientColor(startColor5, endColor5));
            set1.setGradientColors(gradientFills);

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            data.setBarWidth(0.9f);
            chart.setData(data);
        }
    }


    private void setDataTime(int count, float range) {
        int start = 0;
        ArrayList<BarEntry> values = new ArrayList<>();

        DataStorage dts = new DataStorage();

        ArrayList<HashMap<String, String>> formList = dts.readWalkData(getActivity().getFilesDir());

        int maxNumOfFields =formList.size();

        if(formList.size()>count)start = formList.size()-count;
        Log.d("i", String.valueOf(formList.size()));

        for (int i = start; i <  maxNumOfFields; i++) {

            String text =formList.get(i).toString();
            String data = getTime(text);
            float val = Float.parseFloat(data);
            values.add(new BarEntry(i, val));
        }
        BarDataSet set1;
        if (chartTime.getData() != null && chartTime.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) chartTime.getData().getDataSetByIndex(0);
            set1.setValues(values);
            chartTime.getData().notifyDataChanged();
            chartTime.notifyDataSetChanged();
        }else {
            set1 = new BarDataSet(values, "");
            set1.setDrawIcons(false);
            Context context = getContext();
            int startColor1 = ContextCompat.getColor(context, android.R.color.holo_orange_light);
            int startColor2 = ContextCompat.getColor(context, android.R.color.holo_blue_light);
            int startColor3 = ContextCompat.getColor(context, android.R.color.holo_orange_light);
            int startColor4 = ContextCompat.getColor(context, android.R.color.holo_green_light);
            int startColor5 = ContextCompat.getColor(context, android.R.color.holo_red_light);
            int endColor1 = ContextCompat.getColor(context, android.R.color.holo_blue_dark);
            int endColor2 = ContextCompat.getColor(context, android.R.color.holo_purple);
            int endColor3 = ContextCompat.getColor(context, android.R.color.holo_green_dark);
            int endColor4 = ContextCompat.getColor(context, android.R.color.holo_red_dark);
            int endColor5 = ContextCompat.getColor(context, android.R.color.holo_orange_dark);

            List<GradientColor> gradientFills = new ArrayList<>();
            gradientFills.add(new GradientColor(startColor1, endColor1));
            gradientFills.add(new GradientColor(startColor2, endColor2));
            gradientFills.add(new GradientColor(startColor3, endColor3));
            gradientFills.add(new GradientColor(startColor4, endColor4));
            gradientFills.add(new GradientColor(startColor5, endColor5));
            set1.setGradientColors(gradientFills);

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            data.setBarWidth(0.9f);
            chartTime.setData(data);
        }
    }

    public String getDistance(String t)
    {
        String []readT = t.split(",");
        String []readT2 = readT[2].split("=");
        String time=readT2[1].substring(0, readT2[1].length()-1);

        return time;
    }

    public String getTime(String t)
    {
        String []readT = t.split(",");
        String []readT2 = readT[0].split("=");
        String time=readT2[1];

        return time;
    }

    //ile bylo wpisow w pozadanym przedziale czasowym - np w zeszlym tygodniu bylo 15 spacerow, w zeszlym miesiacu wliczajac ten tydzien 17
    public int numOfDaysToEntries(int dayCount){
        return 0;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Załaduj nowe dane lub zaktualizuj dane
        setData(7, 100);
        setDataTime(7,100);
        chart.invalidate();  // Refresh the chart with new data
        chartTime.invalidate();
    }
}