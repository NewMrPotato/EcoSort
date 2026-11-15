package com.rather.ecosort.models;

public class Placemark {
    private double latitude;
    private double longitude;
    private String title, content, name;

    public Placemark(double latitude, double longitude, String title, String content, String name) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.content = content;
        this.name = name;
    }

    public Placemark(double latitude, double longitude, String title, String content) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
