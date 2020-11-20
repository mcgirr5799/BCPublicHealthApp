package com.example.group_21_project.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

/**
 * Represents a collection of Restaurants and offers
 * various methods of adding and accessing the Restaurants.
 *
 * This object implements the Iterable interface, which is used
 * to iterate over its collection of Restaurants.
 * */
public class RestaurantManager implements Iterable<Restaurant> {

    private ArrayList<Restaurant> restaurants;

    // private constructor so that this class can make use of the Singleton Pattern
    public RestaurantManager() {
        restaurants = new ArrayList<>();
    }
    public RestaurantManager(ArrayList<Restaurant> restList) {
        restaurants = restList;

    }

    public RestaurantManager(RestaurantManager res) {
        restaurants = res.restaurants;
    }

    private static RestaurantManager instance;
    public static RestaurantManager getInstance() {
        if(instance == null) {
            instance = new RestaurantManager();
        }

        return instance;
    }

    public static void setInstance(RestaurantManager instance) {
        RestaurantManager.instance = instance;
    }


    private boolean update = false;

    public boolean getUpdate() {return update;}

    public boolean setUpdate(boolean update){return  this.update = update;}

    private Date date = new Date(System.currentTimeMillis() - 3600 * 1000 * 21);

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @NonNull
    @Override
    public Iterator<Restaurant> iterator() {
        return restaurants.iterator();
    }

    public ArrayList<Restaurant> getRestaurants() {
        return restaurants;
    }

    public int size(){
        return restaurants.size();
    }

    public Restaurant getRes(int index){
        return restaurants.get(index);
    }

    /**
     * Returns the restaurant with the specified id.
     *
     * @param   id of the restaurant to retrieve.
     * @return  the restaurant with the specified id, or
     *          null if one could not be found.
     * */
    public Restaurant getRestaurantByID(String id) {
        for(Restaurant r : restaurants) {
            if(r.getId().equals(id)) {
                return r;
            }
        }

        return null;
    }

    public void addRestaurant(Restaurant restaurant) {
        restaurants.add(restaurant);
        Collections.sort(restaurants);
    }

}
