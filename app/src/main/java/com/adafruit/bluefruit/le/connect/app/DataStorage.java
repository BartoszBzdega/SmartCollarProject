package com.adafruit.bluefruit.le.connect.app;

import android.content.Context;
import android.util.Log;

import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;  // Import the File class
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;  // Import the IOException class to handle errors
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

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

            //godzine w plecy jest
            //format :   Sun Dec 22 10:51:07 GMT 2024
            //rozdzielić go spacjami i pokolei czytac
            Date data = Calendar.getInstance().getTime();
            Log.d("Data:", data.toString());

            if(!plik.exists()) {
                plik.createNewFile();

                FileOutputStream fos = new FileOutputStream(plik);
                OutputStreamWriter osw = new OutputStreamWriter(fos);
                JSONObject jobject = new JSONObject();
                JSONArray jarray = new JSONArray();
                JSONObject jvalues = new JSONObject();
                jvalues.put("WalkDate", data);
                jvalues.put("WalkTime", walkTime);
                jvalues.put("WalkDistance", walkDistance);


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
            File plik=new File(path,petDataFile);


            if(!plik.exists())
            {
                plik.createNewFile();
            }
                FileOutputStream fos = new FileOutputStream(plik);
                OutputStreamWriter osw = new OutputStreamWriter(fos);

                JSONObject jobject = new JSONObject();
                jobject.put("Name",name);
                jobject.put("Age",age);
                jobject.put("Weight",weight);

                osw.write(jobject.toString());
                osw.flush();
                fos.getFD().sync();
                osw.close();



        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String loadJSONFromAsset(File file) {
        String json = null;
        try {
            FileInputStream fis = new FileInputStream(file);

            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public ArrayList readPetInfo(File path){
        File plik =new File(path,petDataFile);
        ArrayList<HashMap<String, String>> formList = new ArrayList<>();
       try{
       if(plik.exists())
       {
               String json;

               JSONObject obj = new JSONObject(loadJSONFromAsset(plik));
               //pusta nazwa obiektu, wyzej tez nie jest podana, mozliwe ze wywali

               formList = new ArrayList<HashMap<String, String>>();

                HashMap<String, String> m_li = new HashMap<>();
                String nameValue = obj.getString("Name");
                String ageValue = obj.getString("Age");  // Używamy poprawnego klucza "Age"
                String weightValue = obj.getString("Weight");

           // Przechowujemy odczytane wartości w HashMap
           m_li.put("Name", nameValue);
           m_li.put("Age", ageValue);
           m_li.put("Weight", weightValue);

           // Dodajemy dane do listy
           formList.add(m_li);
           return formList;

       }
       else
       {
           plik.createNewFile();

           FileOutputStream fos = new FileOutputStream(plik);
           OutputStreamWriter osw = new OutputStreamWriter(fos);

           String name="no name";
           String age ="no age";
           String weight = "no weight";

           JSONObject jobject = new JSONObject();
           jobject.put("Name",name);
           jobject.put("Age",age);
           jobject.put("Weight",weight);

           osw.write(jobject.toString());
           osw.flush();
           fos.getFD().sync();
           osw.close();

       }

       } catch (Exception e) {
           throw new RuntimeException(e);
       }
    return formList;
    }

    public boolean checkPetDataFileExists(File path)
    {
        File plik =new File(path,fileName);
        if(plik.exists())
        {
            return true;

        }else{return false;}

    }

    public ArrayList readWalkData(File path){
        File plik =new File(path,fileName);
        ArrayList<HashMap<String, String>> formList = new ArrayList<>();
        try{
            if(plik.exists())
            {
                String json;

                JSONObject obj = new JSONObject(loadJSONFromAsset(plik));
                JSONArray jArry = obj.getJSONArray("Walk Data");
                formList = new ArrayList<HashMap<String, String>>();
                HashMap<String, String> m_li;

                for (int i = 0; i < jArry.length(); i++) {
                    //TODO: dodać odczytywanie wszystkich danych, i odczytywanie petdatainfo
                    JSONObject jo_inside = jArry.getJSONObject(i);
                    Log.d("Details-->", jo_inside.getString("WalkDate"));
                    String dateValue = jo_inside.getString("WalkDate");
                    String distanceValue = jo_inside.getString("WalkDistance");
                    String timeValue = jo_inside.getString("WalkTime");

                    //Add your values in your `ArrayList` as below:
                    m_li = new HashMap<String, String>();
                    m_li.put("Date", dateValue);
                    m_li.put("Distance",distanceValue);
                    m_li.put("Time",timeValue);

                    formList.add(m_li);

                }
                return formList;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return formList;

    }
}
//TODO - stream do zapisu
//TODO - przetetowac na zapisywaniu danych danych psa (formularz cymka)


