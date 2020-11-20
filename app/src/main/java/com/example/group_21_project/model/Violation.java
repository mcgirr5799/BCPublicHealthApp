package com.example.group_21_project.model;

import androidx.annotation.NonNull;

/**
 * Represents an Inpection violation. Holds information
 * such as the code of the violation, whether or not it is
 * a critical violation and the description.
 *
 * This object implements the Comparable interface, which is used to compare
 * instances of Violations. Violations are compared by their criticality.
 * */
public class Violation implements Comparable<Violation> {
    private int code;
    private boolean isCritical;
    private String description;

    public Violation(int code, boolean isCritical, String description) {
        this.code = code;
        this.isCritical = isCritical;
        this.description = description;
    }

    /**
     * Compares violations based on their criticality.
     *
     * @param   violation the violation to be compared.
     * @return  a negative integer, zero, or a positive integer as this violation's
     *          critically is less than, equal to, or greater than the specified violation.
     * */
    @Override
    public int compareTo(@NonNull Violation violation) {
        // both violations are either critical or non-critical
        if(violation.isCritical && isCritical || !violation.isCritical && !isCritical) {
            return 0;
        } else if(violation.isCritical) { // if only the parameter violation is critical
            return 1;
        }

        // parameter violation is non-critical while this violaion is critical
        return -1;
    }

    public int getCode() {
        return code;
    }

    public boolean isCritical() {
        return isCritical;
    }

    public String getDescription() {
            return description;
    }

    @Override
    public String toString() {
        return "Violation{" +
                "code=" + code +
                ", isCritical=" + isCritical +
                ", description='" + description + '\'' +
                '}';
    }
}
