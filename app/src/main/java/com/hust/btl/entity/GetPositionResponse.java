package com.hust.btl.entity;

import com.google.gson.annotations.SerializedName;

public class GetPositionResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("balance")
    private int balance;

    @SerializedName("lat")
    private double lat;

    @SerializedName("lon")
    private double lon;

    @SerializedName("accuracy")
    private int accuracy;

    @SerializedName("address")
    private String address;

    public GetPositionResponse(String status, int balance, double lat, double lon, int accuracy, String address) {
        this.status = status;
        this.balance = balance;
        this.lat = lat;
        this.lon = lon;
        this.accuracy = accuracy;
        this.address = address;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
