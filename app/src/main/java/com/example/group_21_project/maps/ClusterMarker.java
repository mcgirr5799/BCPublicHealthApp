package com.example.group_21_project.maps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.group_21_project.model.Restaurant;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterMarker implements ClusterItem {
    private Restaurant restaurant;

    public ClusterMarker(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public int getIconPicture() {
        return restaurant.getIconID();
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return new LatLng(restaurant.getAddress().getLatitide(), restaurant.getAddress().getLongitide());
    }

    @Nullable
    @Override
    public String getTitle() {
        return restaurant.getName();
    }

    @Nullable
    @Override
    public String getSnippet() {
        if(restaurant.hasInspection())
        return restaurant.getAddress().getPhysicalAddress() + "\n" + restaurant.getInspections().get(0).getHazardRating();
        else
            return restaurant.getAddress().getPhysicalAddress() + "\n" + "High";
    }
}
