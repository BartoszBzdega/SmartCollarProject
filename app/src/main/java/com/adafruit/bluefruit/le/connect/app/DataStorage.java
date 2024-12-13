package com.adafruit.bluefruit.le.connect.app;

import android.content.Context;

import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;  // Import the File class
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;  // Import the IOException class to handle errors
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;

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
    public void saveWalkData(float walkDistance,float walkTime,String walkDate, File path){

        try{

            //File path =DataStorage.getFilesDir();
            File plik=new File(path,fileName);

            if(!plik.exists()){
                plik.createNewFile();
            }
                FileOutputStream fos = new FileOutputStream(plik);
                OutputStreamWriter osw = new OutputStreamWriter(fos);
                osw.write("okolwiek");
                osw.flush();
                fos.getFD().sync();
                osw.close();

//TODO: wyczaoic zapisywanie do pliku
                //try(FileOutputStream fos = fileoutputStream){}

                JSONObject jobject = new JSONObject();
                JSONArray jarray = new JSONArray();
                JSONObject jvalues = new JSONObject();
                jvalues.put("Date",walkDate);
                jvalues.put("Distance",walkDistance);
                jvalues.put("Time",walkTime);

                jarray.put(jvalues);
                jobject.put("Walk Data",jarray);
                //try (FileWriter writer = new FileWriter(plik)) {
                //    writer.write(jobject.toString());
                //}


            // Use RandomAccessFile to append new data without loading the whole file
            //RandomAccessFile raf = new RandomAccessFile(plik, "rw");
            //long fileLength = raf.length();

            // Move the pointer to the end of the "walkData" array
            //raf.seek(fileLength - 1); // Position before closing bracket

            // Add new data
            //String newData = "{\"WalkDate\":\"" + walkDate + "\",\"WalkTime\":\"" + walkTime + "\",\"WalkDistance\":" + walkDistance + "}";

           // if (fileLength > 20) { // If the array is not empty, add a comma
            //    raf.writeBytes(",");
           // }

            //raf.writeBytes(newData + "]}");
            //raf.close();

            /*
            * obiekt:
            * Walkdata{
            *   array{
            *       {Date,Distance,Time},
            *       {Date,Distance,Time},
            *       ...
            *     }
            * }
            *
            * */
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    //save data about pet
    public void savePetInfo(String name,int age,float weight,boolean overweight){

        try{
            File plik=new File(fileName);

            if(!plik.exists()){
                plik.createNewFile();
            }

            JSONObject jobject = new JSONObject();
            jobject.put("Name",name);
            jobject.put("age",age);
            jobject.put("Weight",weight);
            jobject.put("Overweight", overweight);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void readWalkData(){}

    public void readPetInfo(){}
}
//TODO - stream do zapisu
//TODO - przetetowac na zapisywaniu danych danych psa (formularz cymka)


