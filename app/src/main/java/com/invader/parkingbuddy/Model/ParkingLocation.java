package com.invader.parkingbuddy.Model;

public class ParkingLocation {

    private String name;
    private double latitude;
    private double longitude;
    private String address;
    private String description;
    private int totalSlots;
    private int availableSlots;
    private String cost;
    private String uid;
    private String mapURL;
    private String locationid;

    public ParkingLocation() {
    }

    public ParkingLocation(String name, double latitude, double longitude, String address, String description, int totalSlots, int availableSlots, String cost, String uid, String mapURL, String locationid) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.description = description;
        this.totalSlots = totalSlots;
        this.availableSlots = availableSlots;
        this.cost = cost;
        this.uid = uid;
        this.mapURL = mapURL;
        this.locationid = locationid;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public String getDescription() {
        return description;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public int getAvailableSlots() {
        return availableSlots;
    }

    public String getCost() {
        return cost;
    }

    public String getUid() {
        return uid;
    }

    public String getMapURL() {
        return mapURL;
    }

    public String getLocationid() {
        return locationid;
    }
}
