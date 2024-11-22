package com.adafruit.bluefruit.le.connect.app;

public class DataStorage {

    private String walkDate;
    private float walkDistance;
    private float walkTime;//time in seconds?

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
}

//TODO - zrobic struktury na czas, daty, odleglosc, dane psa
