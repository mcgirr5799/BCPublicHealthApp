package com.example.group_21_project.model;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.RequiresApi;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class UrlDataLength extends AsyncTask<URL, Integer, Long> {


    long restaurantsFileLength;
    long inspectionsFileLength;
    long totalFileLength;
    Context context;



    private static final String PATH_TO_SERVER_RES = "https://data.surrey.ca/dataset/3c8cb648-0e80-4659-9078-ef4917b90ffb/resource/0e5d04a2-be9b-40fe-8de2-e88362ea916b/download/restaurants.csv";
    private static final String PATH_TO_SERVER_INS = "https://data.surrey.ca/dataset/948e994d-74f5-41a2-b3cb-33fa6a98aa96/resource/30b38b66-649f-4507-a632-d5f6f5fe87f1/download/fraserhealthrestaurantinspectionreports.csv";


    public UrlDataLength(Context context) {
        this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected Long doInBackground(URL... urls) {
        URL mUrl = null;
        try {
            mUrl = new URL(PATH_TO_SERVER_RES);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            assert mUrl != null;
            URLConnection connection = mUrl.openConnection();

            restaurantsFileLength= connection.getContentLengthLong();
            long inspectionsLength = readInspection_remote(context);

            totalFileLength = restaurantsFileLength+inspectionsLength;
        } catch (IOException e) {
            Log.wtf("MyActivity","Error reading data file on line " , e);
            e.printStackTrace();
        }
        return totalFileLength;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private long readInspection_remote(Context context) {
        URL mUrl = null;
        try {
            mUrl = new URL(PATH_TO_SERVER_INS);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        String line = null;
        try {
            assert mUrl != null;
            URLConnection connection = mUrl.openConnection();

            inspectionsFileLength = connection.getContentLengthLong();
        } catch (IOException e) {
            Log.wtf("restaurant_detail", "Error reading inspection data file on line " + line, e);
            e.printStackTrace();
        }
        return inspectionsFileLength;
    }

}



