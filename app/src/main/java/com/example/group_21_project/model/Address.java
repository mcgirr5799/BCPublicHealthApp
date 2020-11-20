package com.example.group_21_project.model;

/**
 * Represents an address of a restaurant. Keeps
 * track of information such as the physical address,
 * city and gps coordinates. The physical address has the
 * template: '<building #> <street>'.
 * */
public class Address {
    private String physicalAddress;
    private String city;
    private double latitide;
    private double longitide;

    public Address(String physicalAddress, String city, double latitide, double longitide) {
        this.physicalAddress = physicalAddress;
        this.city = city;
        this.latitide = latitide;
        this.longitide = longitide;
    }

    public String getPhysicalAddress() {
        return physicalAddress;
    }

    public String getCity() {
        return city;
    }

    public double getLatitide() {
        return latitide;
    }

    public double getLongitide() {
        return longitide;
    }

    @Override
    public String toString() {
        return "Address{" +
                "physicalAddress='" + physicalAddress + '\'' +
                ", city='" + city + '\'' +
                ", latitide=" + latitide +
                ", longitide=" + longitide +
                '}';
    }
}

