package com.aghnavi.agh_navi.dmsl.algorithms;


public class LogRecord {

    private String bssid;
    private int rss;

    public LogRecord(String bssid, int rss) {
        super();
        this.bssid = bssid;
        this.rss = rss;
    }

    public String getBssid() {
        return bssid;
    }

    public int getRss() {
        return rss;
    }

    public String toString() {
        return bssid + " " + String.valueOf(rss);
    }

}