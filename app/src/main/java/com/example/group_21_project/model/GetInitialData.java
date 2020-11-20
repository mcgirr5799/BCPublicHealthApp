package com.example.group_21_project.model;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.UserHandle;
import android.util.Log;
import android.view.Display;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.group_21_project.R;
import com.example.group_21_project.model.Address;
import com.example.group_21_project.model.Restaurant;
import com.example.group_21_project.model.RestaurantManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class GetInitialData {
    private RestaurantManager restaurants;
    private String tracknumber;
    private String name;
    private String facilityType;

    private Address address;
    //    Address
    private String city;
    private String physicalAddress;
    private double latitude;
    private double longitude;

    private int iconID;
    private int hazardLevel;
    private int issues;
    private int date;
    Context context;

    private ArrayList<Inspection> inspectionList;
    private ArrayList<Inspection> CurrinspectionList;
    private String iscritical = "Critical";

    private String ins_id;
    private String Insdate;
    private String type;
    private String hazardRating;
    private int numCritical;
    private int numNonCritical;

    private int code;
    private boolean isCritical;
    private String description;
    private ArrayList<Violation> violations;

    private String temp_hazardLevel = "";
    private PowerManager.WakeLock mWakeLock;
    private ProgressDialog mProgressDialog;


    public RestaurantManager readData(Context context, RestaurantManager rests) {
        this.restaurants = rests;
        this.context = context;
            InputStream is = context.getResources().openRawResource(R.raw.restaurants_itr1);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, Charset.forName("UTF-8"))
            );

            String line = "";
            try {
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    // Split by ','
                    String[] tokens = line.split(",");
                    // Read the data
                    tracknumber = tokens[0];
                    name = tokens[1];
                    physicalAddress = tokens[2];
                    city = tokens[3];
                    facilityType = tokens[4];
                    latitude = Double.parseDouble(tokens[tokens.length-2]);
                    longitude = Double.parseDouble(tokens[tokens.length-1]);
                    address = new Address(physicalAddress,city,latitude,longitude);
                    if(name.equals("104 Sushi & Co.")){iconID = R.drawable.sushi;}
                    else if(name.equals("Pattullo A&W")){iconID = R.drawable.a_w;}
                    else if(name.equals("Top in Town Pizza")){iconID = R.drawable.ttpizza;}
                    else if(name.equals("Zugba Flame Grilled Chicken")){iconID = R.drawable.zugba;}
                    else if(name.equals("Lee Yuen Seafood Restaurant")){iconID = R.drawable.lee;}
                    else {iconID = R.drawable.example_icon;}

                    CurrinspectionList=readInspection(context);
                    String high = new String("High");
                    String medium = new String("Moderate");
                    String low = new String("Low");

                    if (CurrinspectionList.size() > 0) {
                        date = Integer.parseInt(CurrinspectionList.get(0).getDate());
                        for (int i = 1; i < CurrinspectionList.size(); i++) {
                            if (Integer.parseInt(CurrinspectionList.get(i).getDate()) > date) {
                                date = Integer.parseInt(CurrinspectionList.get(i).getDate());
                            }
                        }
                    }

                    if (CurrinspectionList.isEmpty())
                        temp_hazardLevel = "Low";
                    else
                    {
                        temp_hazardLevel = CurrinspectionList.get(0).getHazardRating();}
                    if (temp_hazardLevel.equals(high)){
                        hazardLevel = R.drawable.high;
                    }
                    if (temp_hazardLevel.equals(medium)){
                        hazardLevel = R.drawable.medium;
                    }
                    if (temp_hazardLevel.equals(low)){
                        hazardLevel = R.drawable.sad;
                    }

                    boolean favorite = false;
                    SharedPreferences dataVersion = context.getSharedPreferences("preference",0);
                    if (dataVersion.contains(tracknumber)){
                        favorite = dataVersion.getBoolean(tracknumber,false);
                    }

                    if (favorite){
                        dataVersion.edit().putString(name, "Name: " + name + "Recent: " + date + "hazardLevel: " + temp_hazardLevel ).commit();
                    }
                    else {
                        if (dataVersion.contains(name)){
                            dataVersion.edit().remove(name).commit();
                        }
                    }

                    Restaurant R = new Restaurant(tracknumber,name,facilityType,address,iconID,favorite,hazardLevel,issues,date,CurrinspectionList);
                    restaurants.addRestaurant(R);
                    hazardLevel = 0;
                    date = 0;
                    issues = 0;
                    temp_hazardLevel = "";
                    CurrinspectionList = new ArrayList<>();
                };
            } catch (IOException e) {
                Log.wtf("MyActivity","Error reading data file on line " + line , e);
                e.printStackTrace();
            }
            return restaurants;
        }

    private ArrayList readInspection(Context context) {
        InputStream inspection = context.getResources().openRawResource(R.raw.inspectionreports_itr1);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inspection, Charset.forName("UTF-8"))
        );
        String line = "";
        try {
            reader.readLine();
            inspectionList = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length > 0) {
                    if (tracknumber.equals(tokens[0])) {
                        ins_id = tokens[0];
                        Insdate = tokens[1];
                        type = tokens[2];
                        numCritical = Integer.parseInt(tokens[3]);
                        numNonCritical = Integer.parseInt(tokens[4]);
                        hazardRating = tokens[tokens.length - 1];
                        violations = new ArrayList<Violation>();
                        issues = issues + numCritical + numNonCritical;
                        if (tokens.length > 7) {
                            int i = 5;
                            while (i < tokens.length - 2) {
                                if (!tokens[i].matches(".*\\d.*")) {
                                    i++;
                                } else {
                                    String description_num = tokens[i].replaceAll("[^0-9]", "");
                                    code = Integer.parseInt(description_num);
                                    if (tokens[i + 1].equals(iscritical)) {
                                        isCritical = true;
                                    } else {
                                        isCritical = false;
                                    }
                                    description = tokens[i + 2];
                                    Violation newVio = new Violation(code, isCritical, description);
                                    violations.add(newVio);
                                    i = i + 3;
                                }
                            }
                        }

                        //add inspection data to the chosen restaurant
                        Inspection I = new Inspection(ins_id, Insdate, type, numCritical, numNonCritical, hazardRating, violations);
                        inspectionList.add(I);

                        //reset the temp values
                        ins_id = "";
                        Insdate = "";
                        type = "";
                        numCritical = 0;
                        numNonCritical = 0;
                        hazardRating = "";
                    }
                }
            }
        } catch (IOException e) {
            Log.wtf("restaurant_detail", "Error reading inspection data file on line " + line, e);
            e.printStackTrace();
        }
        return (ArrayList) inspectionList;
    }
}
