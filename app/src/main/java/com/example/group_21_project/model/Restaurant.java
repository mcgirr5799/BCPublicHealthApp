package com.example.group_21_project.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Represents a restaurant. Holds information
 * such as id, name of the establishment, facility type(normally restaurant)
 * address and a list of inspections.
 *
 * This object implements the Comparable interface, which is used to compare
 * instances of Restaurants. Restaurants are compared by their name, so alphabetically.
 *
 * This object implements the Iterable interface, which is used to iterate over
 * it's instances of Inspections.
 * */
public class Restaurant implements Comparable<Restaurant>, Iterable<Inspection> {
    private String id;
    private String name;
    private String facilityType;
    private Address address;
    private int iconID;
    private boolean favorite;
    private ArrayList<Inspection> inspections;


/*    
    MERGE CONFLICT KEPT IN CASE THIS BREAKS THE CODE
    private Address address;
    private ArrayList<Inspection> inspections;
*/public Restaurant() {
    this.id = "";
    this.name = "";
    this.facilityType = "";
    this.address = null;
    this.iconID = 0;
    inspections = new ArrayList<>();
}
    public Restaurant(String id, String name, String facilityType, Address address, int iconID) {
        this.id = id;
        this.name = name;
        this.facilityType = facilityType;
        this.address = address;
        this.iconID = iconID;
        inspections = new ArrayList<>();
    }

    private int hazardLevel;
    private int issues;
    private int date;

    public Restaurant(String id, String name, String facilityType, Address address , int iconID,boolean favorite, int hazardLevel, int issues, int date, ArrayList<Inspection> inspections) {

        this.id = id;
        this.name = name;
        this.address = address;
        this.facilityType = facilityType;
        this.favorite = favorite;


        this.iconID = iconID;
        this.hazardLevel = hazardLevel;
        this.issues = issues;
        this.date = date;
        this.inspections = inspections;
//        Collections.sort(inspections);
    }

    public int getIconID() {
        return iconID;
    }

    public int getHazardLevel() {
        return hazardLevel;
    }

    public int getIssues() {
        return issues;
    }

    public int getDate() {
        return date;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public boolean hasInspection() {
        return inspections.size() > 0;
    }

    /**
     * Compares restaurants by name. The ordering is alphabetical.
     *
     * @param   restaurant the restaurant to be compared.
     * @return  a negative integer, zero, or a positive integer as this restaurant's
     *          name is less than, equal to, or greater than the specified restaurant.
     * */
    @Override
    public int compareTo(@NonNull Restaurant restaurant) {
        return this.name.compareTo(restaurant.name);
    }

    @NonNull
    @Override
    public Iterator<Inspection> iterator() {
        return inspections.iterator();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFacilityType() {
        return facilityType;
    }

    public Address getAddress() {
        return address;
    }

    public ArrayList<Inspection> getInspections() {
        return inspections;
    }

    public void addInspection(Inspection inspection) {
        inspections.add(inspection);
        Collections.sort(inspections);
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", facilityType='" + facilityType + '\'' +
                ", address=" + address +
                ", inspections=" + inspections +
                '}';
    }
}
