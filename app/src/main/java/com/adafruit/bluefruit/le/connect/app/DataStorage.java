package com.adafruit.bluefruit.le.connect.app;

import android.content.Context;
import android.util.Log;

import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;  // Import the File class
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;  // Import the IOException class to handle errors
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.Date;

public class DataStorage {

    private String walkDate;
    private float walkDistance;
    private float walkTime;//time in seconds?

    private String fileName="walkData.json";
    private String petDataFile = "petData.json";

    public DataStorage(){}

    public DataStorage(String walkDate, float walkDistance, float walkTime){
        this.walkDistance = walkDistance;
        this.walkTime=walkTime;
        this.walkDate = walkDate;
    }

    public String getDate() {
        return walkDate;
    }

    public void setDate(String date) {
        this.walkDate = date;
    }

    public float getDistance() {
        return walkDistance;
    }

    public void setDistance(float distance) {
        this.walkDistance = distance;
    }

    public float getTime() {
        return walkTime;
    }

    public void setWalkTime(float time) {
        this.walkTime = time;
    }

    //save data from walks
    public void saveWalkData(float walkDistance,float walkTime, File path){

        try{

            //File path =DataStorage.getFilesDir();
            File plik=new File(path,fileName);

            Date data = Calendar.getInstance().getTime();
            Log.d("Data:", data.toString());

            if(!plik.exists()) {
                plik.createNewFile();

                FileOutputStream fos = new FileOutputStream(plik);
                OutputStreamWriter osw = new OutputStreamWriter(fos);
                JSONObject jobject = new JSONObject();
                JSONArray jarray = new JSONArray();
                JSONObject jvalues = new JSONObject();
                jvalues.put("Date", data);
                jvalues.put("Distance", walkDistance);
                jvalues.put("Time", walkTime);

                jarray.put(jvalues);
                jobject.put("Walk Data", jarray);

                osw.write(jobject.toString());
                osw.flush();
                fos.getFD().sync();
                osw.close();
            }else{
                String newData = ",{\"WalkDate\":\"" + data.toString() + "\",\"WalkTime\":\"" + walkTime + "\",\"WalkDistance\":" + walkDistance + "}";

                RandomAccessFile raf = new RandomAccessFile(plik, "rw");
                long length = raf.length(); // Get the file length
                long insertPosition = length - 2; // Position 2 characters from EOF

                // Move to the insertion position
                raf.seek(insertPosition);

                // Read the last 2 characters into a buffer
                byte[] buffer = new byte[(int) (length - insertPosition)];
                raf.read(buffer);

                // Move back to the insertion point and write the new data
                raf.seek(insertPosition);
                raf.write(newData.getBytes());

                // Write back the saved characters
                raf.write(buffer);

            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    //save data about pet
    public void savePetInfo(String name,int age,float weight,File path){

        try{
            //File path =DataStorage.getFilesDir();
            File plik=new File(path,petDataFile);


            if(!plik.exists()) {
                plik.createNewFile();
            }                FileOutputStream fos = new FileOutputStream(plik);
                OutputStreamWriter osw = new OutputStreamWriter(fos);

                JSONObject jobject = new JSONObject();
                jobject.put("Name",name);
                jobject.put("age",age);
                jobject.put("Weight",weight);

                osw.write(jobject.toString());
                osw.flush();
                fos.getFD().sync();
                osw.close();



        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void readWalkData(){}

    public void readPetInfo(){}
}
//TODO - stream do zapisu
//TODO - przetetowac na zapisywaniu danych danych psa (formularz cymka)


