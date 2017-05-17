package com.hamzabinamin.yoyo;

/**
 * Created by Hamza on 12/14/2016.
 */

public class Player {

    private int place;
    private String deviceName;
    private String timeStamp;
    private int score;

    public Player() {

        this.place = 0;
        this.deviceName = "";
        this.timeStamp = "";
        this.score = 0;
    }

    public Player(int place, String deviceName, String timeStamp, int score) {

        this.place = place;
        this.deviceName = deviceName;
        this.timeStamp = timeStamp;
        this.score = score;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getPlace() {
        return this.place;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public String getTimeStamp() {
        return this.timeStamp;
    }

    public int getScore() {
        return  this.score;
    }
}
