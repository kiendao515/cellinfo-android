package com.hust.btl.entity;

public class CellTower {
    private int cellId; // cell id
    private int locationAreaCode; // ma vung

    @Override
    public String toString() {
        return "CellTower{" +
                "cellId=" + cellId +
                ", locationAreaCode=" + locationAreaCode +
                ", mobileCountryCode=" + mobileCountryCode +
                ", mobileNetworkCode=" + mobileNetworkCode +
                ", lon=" + lon +
                ", lat=" + lat +
                ", age=" + age +
                ", signalStrength=" + signalStrength +
                ", timingAdvance='" + timingAdvance + '\'' +
                ", pci=" + pci +
                ", signal=" + signal +
                '}';
    }

    private int mobileCountryCode; //Mobile Country Code
    private int mobileNetworkCode; // Mobile Network Code

    private double lon;         // Base station longitude

    private double lat;         // Base station latitude
    private int age;
    private int signalStrength;
    private String timingAdvance;
    /* bsic for GSM, psc for WCDMA, pci for LTE,
        GSM has #getPsc() but always get Integer.MAX_VALUE,
        psc is undefined for GSM */
    private int pci;
    private Signal signal;

    public Signal getSignal() {
        return signal;
    }

    public void setSignal(Signal signal) {
        this.signal = signal;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }




    public int getPci() {
        return pci;
    }

    public void setPci(int pci) {
        this.pci = pci;
    }

    public CellTower(int cellId, int locationAreaCode, int mobileCountryCode, int mobileNetworkCode, int age, int signalStrength, String timingAdvance, int pci) {
        this.cellId = cellId;
        this.locationAreaCode = locationAreaCode;
        this.mobileCountryCode = mobileCountryCode;
        this.mobileNetworkCode = mobileNetworkCode;
        this.age = age;
        this.signalStrength = signalStrength;
        this.timingAdvance = timingAdvance;
        this.pci = pci;
    }




    public int getCellId() {
        return cellId;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }

    public int getLocationAreaCode() {
        return locationAreaCode;
    }

    public void setLocationAreaCode(int locationAreaCode) {
        this.locationAreaCode = locationAreaCode;
    }

    public int getMobileCountryCode() {
        return mobileCountryCode;
    }

    public void setMobileCountryCode(int mobileCountryCode) {
        this.mobileCountryCode = mobileCountryCode;
    }

    public int getMobileNetworkCode() {
        return mobileNetworkCode;
    }

    public void setMobileNetworkCode(int mobileNetworkCode) {
        this.mobileNetworkCode = mobileNetworkCode;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(int signalStrength) {
        this.signalStrength = signalStrength;
    }

    public String getTimingAdvance() {
        return timingAdvance;
    }

    public void setTimingAdvance(String timingAdvance) {
        this.timingAdvance = timingAdvance;
    }

    public CellTower() {
        signal = new Signal();
    }

    public CellTower(int cellId, int locationAreaCode, int mobileCountryCode, int mobileNetworkCode, int age, int signalStrength, String timingAdvance) {
        this.cellId = cellId;
        this.locationAreaCode = locationAreaCode;
        this.mobileCountryCode = mobileCountryCode;
        this.mobileNetworkCode = mobileNetworkCode;
        this.age = age;
        this.signalStrength = signalStrength;
        this.timingAdvance = timingAdvance;
    }
}
