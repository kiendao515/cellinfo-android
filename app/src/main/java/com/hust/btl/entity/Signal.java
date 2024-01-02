package com.hust.btl.entity;

public class Signal {
    private int asuLevel;       /* Signal level as an asu value, asu is calculated based on 3GPP RSRP
                                   for GSM, between 0..31, 99 is unknown
                                   for WCDMA, between 0..31, 99 is unknown
                                   for LTE, between 0..97, 99 is unknown
                                   for CDMA, between 0..97, 99 is unknown */
    private int signalLevel;    // Signal level as an int from 0..4

    private int dbm;            // Signal strength as dBm

    public Signal() {
    }

    public int getAsuLevel() {
        return asuLevel;
    }

    public void setAsuLevel(int asuLevel) {
        this.asuLevel = asuLevel;
    }

    public int getSignalLevel() {
        return signalLevel;
    }

    public void setSignalLevel(int signalLevel) {
        this.signalLevel = signalLevel;
    }

    public int getDbm() {
        return dbm;
    }

    public void setDbm(int dbm) {
        this.dbm = dbm;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String type;
}
