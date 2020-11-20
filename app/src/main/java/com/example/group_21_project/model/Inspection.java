package com.example.group_21_project.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Represents a restaurant health inspection. Holds
 * information such as an id, date of inspection, type of inspection,
 * overall hazard rating, number of critical and non-critical violations and
 * a list of all violations.
 *
 * This object implements the Comparable interface, which is used to compare
 * instances of Inspections. Inspections are compared by their date.
 *
 * This object implements the Iterable interface, which is used to iterate over
 * it's instances of Violations.
 * */
public class Inspection implements Comparable<Inspection>, Iterable<Violation> {
    private String id;
    private String date;
    private String type;
    private String hazardRating;
    private int numCritical;
    private int numNonCritical;


    private ArrayList<Violation> violations;

    public Inspection(String id, String date, String type, int numCritical,
                      int numNonCritical, String hazardRating, ArrayList<Violation> violations) {

        this.id = id;
        this.date = date;
        this.type = type;
        this.numCritical = numCritical;
        this.numNonCritical = numNonCritical;
        this.hazardRating = hazardRating;

        this.violations = violations;
        Collections.sort(violations);
    }

    public Inspection(String id, String date, String type, int numCritical,
                      int numNonCritical, String hazardRating) {

        this.id = id;
        this.date = date;
        this.type = type;
        this.numCritical = numCritical;
        this.numNonCritical = numNonCritical;
        this.hazardRating = hazardRating;
    }

    /**
     * Compares inspections based on their date.
     *
     * @param   inspection the inspection to be compared.
     * @return  a negative integer, zero, or a positive integer as this inspection's
     *          date is less than, equal to, or greater than the specified inspection.
     * */
    @Override
    public int compareTo(@NonNull Inspection inspection) {
        int compareInspectionYear = Integer.parseInt(inspection.getDate().substring(0, 4));
        int compareInspectionMonth = Integer.parseInt(inspection.getDate().substring(4, 6));
        int compareInspectionDay = Integer.parseInt(inspection.getDate().substring(6, 8));

        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(4, 6));
        int day = Integer.parseInt(date.substring(6, 8));

        if(compareInspectionYear < year) {
            return -1;
        } else if(compareInspectionYear > year) {
            return 1;
        } else { // years are the same
            if(compareInspectionMonth < month) {
                return -1;
            } else if(compareInspectionMonth > month) {
                return 1;
            } else { // months are the same
                if(compareInspectionDay < day) {
                    return -1;
                } else if(compareInspectionDay > day) {
                    return 1;
                }
            }
        }

        // dates are exactly the same
        return 0;
    }

    @NonNull
    @Override
    public Iterator<Violation> iterator() {
        return violations.iterator();
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public String getHazardRating() {
        return hazardRating;
    }

    public int getNumCritical() {
        return numCritical;
    }

    public int getNumNonCritical() {
        return numNonCritical;
    }

    public ArrayList<Violation> getViolations() {
        return violations;
    }



    @Override
    public String toString() {
        return "Inspection{" +
                "id='" + id + '\'' +
                ", date='" + date + '\'' +
                ", type='" + type + '\'' +
                ", hazardRating='" + hazardRating + '\'' +
                ", numCritical=" + numCritical +
                ", numNonCritical=" + numNonCritical +
                ", violations=" + violations +
                '}';
    }
}

