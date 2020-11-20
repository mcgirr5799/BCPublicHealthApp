package com.example.group_21_project.model;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.RequiresApi;

import android.os.PowerManager;
import android.util.Log;

import com.example.group_21_project.R;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class DownloadFilesTask extends AsyncTask<URL, Integer, RestaurantManager> {

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


    private static final String PATH_TO_SERVER_RES = "https://data.surrey.ca/dataset/3c8cb648-0e80-4659-9078-ef4917b90ffb/resource/0e5d04a2-be9b-40fe-8de2-e88362ea916b/download/restaurants.csv";
    private static final String PATH_TO_SERVER_INS = "https://data.surrey.ca/dataset/948e994d-74f5-41a2-b3cb-33fa6a98aa96/resource/30b38b66-649f-4507-a632-d5f6f5fe87f1/download/fraserhealthrestaurantinspectionreports.csv";

    public interface AsyncResponse {
        void processFinish(RestaurantManager output);
    }
    public AsyncResponse delegate = null;
    public DownloadFilesTask(Context context,RestaurantManager rests,AsyncResponse delegate) {
        this.context = context;
        this.restaurants = new RestaurantManager();
        this.delegate = delegate;
    }
    @Override
    protected void onPreExecute() {
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage(context.getString(R.string.update));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMax(100);
        mProgressDialog.setCancelable(true);

        // reference to instance to use inside listener
        mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DownloadFilesTask.this.cancel(true);
                SharedPreferences dataVersion = context.getSharedPreferences("preference",0);
                dataVersion.edit().putString("updateState","canceledUpdate").commit();
                restaurants = new RestaurantManager();
                getDataFromLastTime();
                mProgressDialog.dismiss();
                dialog.dismiss();
                delegate.processFinish(restaurants);

            }
        });

        mProgressDialog.show();
        super.onPreExecute();
    }


    @Override
    protected void onPostExecute(RestaurantManager restaurants) {
        super.onPostExecute(restaurants);
        mProgressDialog.dismiss();
        delegate.processFinish(restaurants);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected RestaurantManager doInBackground(URL... urls) {

            URL mUrl = null;
            try {
                mUrl = new URL(PATH_TO_SERVER_RES);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                assert mUrl != null;
                URLConnection connection = mUrl.openConnection();
                BufferedReader br = new BufferedReader(new
                        InputStreamReader(connection.getInputStream()));
                String line = "";

                long fileSize = 0;
                //when fail to get length
                while(fileSize<=0)
                {
                    connection = mUrl.openConnection();
                    br = new BufferedReader(new
                            InputStreamReader(connection.getInputStream()));
                    fileSize=connection.getContentLengthLong();
                }
                
                br.readLine();
                long currRead = 0;
//                int count = 0;
                while ((line = br.readLine()) != null ) {
                    if(DownloadFilesTask.this.isCancelled())
                    {break;}
//                    count++;
                    Log.d("MyActivity save", " " + line);
                    // Split by ','
                    String[] tokens = line.split(",");
                    // Read the data
                    tracknumber = tokens[0];
                    name = tokens[1];
                    physicalAddress = tokens[2];
                    city = tokens[3];
                    facilityType = tokens[4];
                    latitude = Double.parseDouble(tokens[tokens.length - 2]);
                    longitude = Double.parseDouble(tokens[tokens.length - 1]);
                    address = new Address(physicalAddress, city, latitude, longitude);
                    String[] name_tokens = name.split(" ");
                    if (name_tokens[0].equals("7-Eleven")) {
                        iconID = R.drawable.seven;
                    } else if (name_tokens[0].equals("A&W")) {
                        iconID = R.drawable.a_w;
                    } else if (name_tokens[0].equals("Blenz") && name_tokens[0].equals("Coffee")) {
                        iconID = R.drawable.blenz;
                    } else if (name_tokens[0].equals("Freshslice")) {
                        iconID = R.drawable.freshslice;
                    } else if (name_tokens[0].equals("McDonald's")) {
                        iconID = R.drawable.mcdonald;
                    } else if (name_tokens[0].equals("Cafe") && name_tokens[1].equals("Nelly")) {
                        iconID = R.drawable.cafe_nelly;
                    } else if (name_tokens[0].equals("Dragon")) {
                        iconID = R.drawable.dargon;
                    } else if (name_tokens[0].equals("Donair")) {
                        iconID = R.drawable.donair;
                    } else if (name_tokens[0].equals("COBS")) {
                        iconID = R.drawable.cobs;
                    } else if (name_tokens[0].equals("Cotto")) {
                        iconID = R.drawable.cotto;
                    } else if(name.equals("104 Sushi & Co.")){iconID = R.drawable.sushi;}
                    else if(name.equals("Pattullo A&W")){iconID = R.drawable.a_w;}
                    else if(name.equals("Top in Town Pizza")){iconID = R.drawable.ttpizza;}
                    else if(name.equals("Zugba Flame Grilled Chicken")){iconID = R.drawable.zugba;}
                    else if(name.equals("Lee Yuen Seafood Restaurant")){iconID = R.drawable.lee;}
                    else {
                        iconID = R.drawable.example_icon;
                    }


                    CurrinspectionList = readInspection_remote(context);
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
                    else {
                        temp_hazardLevel = CurrinspectionList.get(0).getHazardRating();
                    }
                    if (temp_hazardLevel.equals(high)) {
                        hazardLevel = R.drawable.high;
                    }
                    if (temp_hazardLevel.equals(medium)) {
                        hazardLevel = R.drawable.medium;
                    }
                    if (temp_hazardLevel.equals(low)) {
                        hazardLevel = R.drawable.sad;
                    }
                    boolean favorite = false;
                    SharedPreferences dataVersion = context.getSharedPreferences("preference",0);
                    Log.d("Contains", ""+tracknumber+dataVersion.contains(tracknumber));
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
                    Restaurant R = new Restaurant(tracknumber, name, facilityType, address, iconID, favorite,hazardLevel, issues, date, CurrinspectionList);
                    restaurants.addRestaurant(R);
                    hazardLevel = 0;
                    date = 0;
                    issues = 0;
                    temp_hazardLevel = "";
                    CurrinspectionList = new ArrayList<>();

                    long read_data_size = line.getBytes().length;
                    currRead += read_data_size;
                    Log.d("MyActivity readed", " " + currRead);
                    publishProgress((int) (currRead * 100 / fileSize));
                }
                ;
            } catch (IOException e) {
                Log.wtf("MyActivity", "Error reading data file on line ", e);
                e.printStackTrace();
            }

        return restaurants;

    }

    private ArrayList readInspection_remote(Context context) {
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
            BufferedReader reader = new BufferedReader(new
                    InputStreamReader(connection.getInputStream()));
            FileOutputStream file = context.openFileOutput("inspection.csv", Context.MODE_PRIVATE);
            reader.readLine();
            inspectionList = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if(DownloadFilesTask.this.isCancelled())
                {break;}
                file.write(line.getBytes());
                String[] tokens = line.split(",");
                if (tokens.length>0) {
                    if (tracknumber.equals(tokens[0])) {
                        ins_id = tokens[0];
                        Insdate = tokens[1];
                        type = tokens[2];
                        numCritical = Integer.parseInt(tokens[3]);
                        numNonCritical = Integer.parseInt(tokens[4]);
                        hazardRating = tokens[tokens.length - 1];
                        violations = new ArrayList<Violation>();
                        issues =issues+numCritical+numNonCritical;
                        if (tokens.length > 7) {
                            int i = 5;
                            while (i < tokens.length - 2) {
                                if(!tokens[i].matches(".*\\d.*"))
                                {
                                    i++;
                                }
                                else
                                {
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
                                i = i + 3;}
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

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgress(progress[0]);
    }

    private void getDataFromLastTime()
    {
        SharedPreferences dataVersion = context.getSharedPreferences("preference",0);
        Gson gson = new Gson();
        String json = dataVersion.getString("restaurants", "");
        restaurants= gson.fromJson(json, RestaurantManager.class);
        RestaurantManager.setInstance(restaurants);
    }



}


