package com.example.group_21_project;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatCallback;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.group_21_project.UI.filter;
import com.example.group_21_project.maps.ClusterManagerRenderer;
import com.example.group_21_project.maps.ClusterMarker;
import com.example.group_21_project.maps.ClusterMarkerInfoWindowAdapter;
import com.example.group_21_project.model.Address;
import com.example.group_21_project.model.DownloadFilesTask;
import com.example.group_21_project.model.GetInitialData;
import com.example.group_21_project.model.Inspection;
import com.example.group_21_project.model.Restaurant;
import com.example.group_21_project.model.RestaurantManager;
import com.example.group_21_project.model.UrlDataLength;
import com.example.group_21_project.model.Violation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.maps.android.clustering.ClusterManager;


import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnCameraMoveStartedListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final float DEFAULT_ZOOM = 15f;

    private GoogleMap mMap;
    private LocationManager locationManager;

    private boolean followUser = true;

    private RestaurantManager restaurants;
    boolean needUpdateTime;
    boolean needUpdateData;
    SharedPreferences dataVersion;
    SharedPreferences previous_filter;

    long lastUpdateLen;
    String lastUpdateTime;
    boolean usingFilter;


    private double latitude=0;
    private double longitide=0;
    boolean setSingleRest;
    private ArrayList<Restaurant> myItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        restaurants = new RestaurantManager();
        dataVersion = getSharedPreferences("preference",MODE_PRIVATE);
        setSingleRest = launchedFromSingleRestaurant();
//        dataVersion.edit().clear().commit();
        boolean firstTime = dataVersion.getBoolean("firstTime",true);
        dataVersion.edit().putLong("fileLen", 0).commit();
        //when first time start the app, load the default data set and ask for update
        previous_filter = getSharedPreferences("filter", MODE_PRIVATE);
        usingFilter = previous_filter.getBoolean("isFilter",false);

            if(firstTime) {
            firstTimeStart();
        }


        else //when not the first time start, check if any downloaded data, if not, initialize with initial data and update request
        {



            //display the saved restaurant data from last time
            getDataFromLastTime();
            if(usingFilter) {
                try {
                    getFilteredData();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }


            //check if after 20 hours
            needUpdateTime = checkUpdateTime();

            try {
                needUpdateData = checkUpdateData();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //if both time >20 hours and have new data, ask for update
            if(needUpdateTime&&needUpdateData)
            {  comfirmUpdate();
                }
            //if not meet both condition, continue with data from last time
            else{
                obtainSupportMapFragment();
                getLocationPermission();
                setupLocationManager();
               }

            }



    }

    private void getFilteredData() throws ParseException {
        myItems = new ArrayList<Restaurant>();
        List restIndex = new ArrayList<>();
        String hz_level = previous_filter.getString("hzLevel", "None");
        int crit_from = previous_filter.getInt("crit_from", 0);
        int crit_to = previous_filter.getInt("crit_to", 0);
        boolean isFavorite = previous_filter.getBoolean("isFavorite", false);
        String restaurantName = previous_filter.getString("restaurantName", "");

        for (int i = 0; i < restaurants.size(); i++) {
            Restaurant temprest = restaurants.getRes(i);
            if (hz_level.equals("None")) {
            }
            //when there is filter on hz level
            else {
                //when the restaurant has inspection
                if (temprest.getInspections().size() > 0) {
                    //when the restaurant first inspection hz level equals to target hz level, keep the restaurant
                    if (temprest.getInspections().get(0).getHazardRating().equals(hz_level)) {
                    } else//when the restaurant first inspection hz level not equals to target hz level, null the restaurant
                        temprest = new Restaurant();
                } else//when the restaurant dose not have inspection, null the restaurant
                    temprest = new Restaurant();
            }
            if (crit_from != 0) {
                if (numOfRestCritLastYear(temprest) < crit_from)
                    temprest = new Restaurant();
                if (numOfRestCritLastYear(temprest) > crit_to)
                    temprest = new Restaurant();
            }

            if (isFavorite == true) {
                if (temprest.isFavorite() == false)
                    temprest = new Restaurant();
            }
            Log.d("checking", "populateListView: "+temprest.getName().contains(restaurantName));
            if (!(restaurantName.equals(""))) {
                Log.d("checking", "populateListView: here");
                if (!(temprest.getName().toLowerCase().contains(restaurantName.toLowerCase()))) {
                    temprest = new Restaurant();
                }
            }

            if(temprest.getName().equals("")){}
            else{
                restIndex.add(i);
                myItems.add(restaurants.getRes(i));
            }
        }
        restaurants = new RestaurantManager(myItems);
    }

    private int numOfRestCritLastYear(Restaurant rest) throws ParseException {
        int count = 0;
        Timestamp currTime = new Timestamp(System.currentTimeMillis());
        Date currdate = new Date(currTime.getTime());
        if(rest.getInspections().size()>0)
        {   for(int i =0;i<rest.getInspections().size();i++)
        {
            Inspection tempIns = rest.getInspections().get(i);
            String dateString = tempIns.getDate();
            DateFormat format = new SimpleDateFormat("yyyyMMdd");
            Date InsDate = format.parse(dateString);
            long differInDate = currdate.getTime()-InsDate.getTime();
            if (differInDate<=31556952000L)
            {
                count+=tempIns.getNumCritical();
            }
        }
        }

        return count;
    }


    /*
     * Obtain the SupportMapFragment and get notified when the map is ready to be used.
     * */
    private void obtainSupportMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void setupLocationManager() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    /*
     * Manipulates the map once available.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // initialize UI of zoom buttons and floating buttons
        intializeUI();

        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnCameraMoveStartedListener(this);

        /*
        Address address = new Address("12808 King George Blvd", "Surry", 49.20610961, -122.8668064);
        Restaurant restaurant = new Restaurant("SDFO-8HKP7E", "Pattullo A&W", "Restaurant", address, R.drawable.example_icon);

        ArrayList<Violation> violations = new ArrayList<>();
        violations.add(new Violation(308, false, "Equipment/utensils/food contact surfaces are not in good working order [s. 16(b)]"));
        Inspection inspection = new Inspection("SDFO-8HKP7E", "20181024", "Follow-Up", 0, 1, "Low", violations);
        restaurant.addInspection(inspection);

        Address address2 = new Address("12808 King George Blvd", "Surry", 49.21, -122.87);
        Restaurant restaurant2 = new Restaurant("SDFO-8HKP7E", "Pattullo A&W", "Restaurant", address2, R.drawable.example_icon);

        ArrayList<Violation> violations2 = new ArrayList<>();
        violations2.add(new Violation(308, false, "Equipment/utensils/food contact surfaces are not in good working order [s. 16(b)]"));
        Inspection inspection2 = new Inspection("SDFO-8HKP7E", "20181024", "Follow-Up", 0, 1, "Low", violations2);
        restaurant2.addInspection(inspection2);

        ArrayList<Restaurant> restaurants = new ArrayList<>();
        restaurants.add(restaurant);
        restaurants.add(restaurant2);
*/
        addMapMarkers(restaurants.getRestaurants());

        if(setSingleRest)
            moveCameraToSelectedLocation();
        else
            moveCameraToCurrentLocation();
    }

    // initialize UI features
    private void intializeUI() {
        mMap.getUiSettings().setZoomControlsEnabled(true);

        FloatingActionButton mapFloatingActionButton = findViewById(R.id.restaurantListFAB);

        mapFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if you press the button it finishes the map similar to a back press

                Intent returnIntent = new Intent();
                setResult(RESULT_CANCELED,returnIntent);
                finish();
            }
        });


        FloatingActionButton filterFloatingActionButton = findViewById(R.id.filterFAB);

        filterFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if you press the button it finishes the map similar to a back press

                Intent a = filter.makeLaunchFilter(MapsActivity.this);
                previous_filter.edit().putBoolean("is_from_map",true).apply();
                startActivity(a);
                finish();
            }
        });
    }

    private void addMapMarkers(ArrayList<Restaurant> restaurants) {
        ClusterManager<ClusterMarker> clusterManager = new ClusterManager<>(getApplicationContext(), mMap);
        mMap.setOnCameraIdleListener(clusterManager);

        ClusterManagerRenderer clusterManagerRenderer = new ClusterManagerRenderer(this, mMap, clusterManager);
        clusterManager.setRenderer(clusterManagerRenderer);

        for(Restaurant r : restaurants) {
            ClusterMarker newClusterMarker = new ClusterMarker(r);
            clusterManager.addItem(newClusterMarker);
        }

        clusterManager.cluster();

        mMap.setInfoWindowAdapter(clusterManager.getMarkerManager());
        ClusterMarkerInfoWindowAdapter clusterMarkerInfoWindowAdapter = new ClusterMarkerInfoWindowAdapter(this);
        clusterManager.getMarkerCollection().setInfoWindowAdapter(clusterMarkerInfoWindowAdapter);
        clusterManager.setOnClusterItemClickListener(clusterMarkerInfoWindowAdapter);
        clusterManager.setOnClusterItemInfoWindowClickListener(clusterMarkerInfoWindowAdapter);
    }

    private void moveCameraToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(lastKnownLocation != null) {
            LatLng currentLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM));
        }
    }

    private void moveCameraToSelectedLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        if(longitide != 0 && latitude!=0) {
            LatLng currentLocation = new LatLng(latitude, longitide);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM));
        }
    }

    @Override
    public void onCameraMoveStarted(int i) {
        if(i == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            Toast.makeText(this, R.string.maps_back_to_user_location,
                    Toast.LENGTH_SHORT).show();
            followUser = false;
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        followUser = true;
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {

        if(setSingleRest) {
                moveCameraToSelectedLocation();

        }
        else
        {
            if(followUser)
            {
            moveCameraToCurrentLocation();
 }
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // nothing to do here
    }

    @Override
    public void onProviderEnabled(String provider) {
        // nothing to do here
    }

    @Override
    public void onProviderDisabled(String provider) {
        // nothing to do here
    }


    private void firstTimeStart() {
        Timestamp currTime = new Timestamp(System.currentTimeMillis());
        dataVersion.edit().putBoolean("firstTime", false).commit();
        dataVersion.edit().putString("time", "2010-10-10 10:10:10.10").commit();

        //save the initial data
        GetInitialData IniData=new GetInitialData();
        restaurants = IniData.readData(MapsActivity.this,restaurants);
        RestaurantManager.setInstance(restaurants);
        Gson gson = new Gson();
        String json = gson.toJson(restaurants);
        dataVersion.edit().putString("restaurants", "").commit();
        dataVersion.edit().putString("restaurants", json).commit();


        comfirmUpdate();
    }

    private void getDataFromLastTime()
    {
        Gson gson = new Gson();
        String json = dataVersion.getString("restaurants", "");
        restaurants= gson.fromJson(json, RestaurantManager.class);
        RestaurantManager.setInstance(restaurants);
    }


    //check if update is required when the first start and after every 20 hours
    private boolean checkUpdateTime() {
        //get the time current time and last update time for checking if update required
        Timestamp currTime = new Timestamp(System.currentTimeMillis());
        lastUpdateTime = dataVersion.getString("time","yyyy-MM-dd hh:mm:ss.SSS");
        Timestamp lastUpdate=null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
            Date parsedDate = dateFormat.parse(lastUpdateTime);
            lastUpdate = new java.sql.Timestamp(parsedDate.getTime());
        } catch(Exception e) { //this generic but you can control another types of exception
            // look the origin of excption
        }

        long timeDiffer = currTime.getTime()-lastUpdate.getTime();
        int timeDifferHours = (int)timeDiffer/3600000;


        if (timeDifferHours>=20||timeDifferHours<=-20)
            return true;
        else
            return false;
    }

    private boolean checkUpdateData() throws ExecutionException, InterruptedException {
        lastUpdateLen = dataVersion.getLong("fileLen",0);
        long currFileLen=getOnlineFileLength();

        //sometime data length has small error

        if(lastUpdateLen<=currFileLen+10&&lastUpdateLen>=currFileLen-10)
            return false;
        else
            return true;
    }

    private long getOnlineFileLength() throws ExecutionException, InterruptedException {
        long FileLen=0;
        while(FileLen<=0)
        {
            UrlDataLength currLen = new UrlDataLength(MapsActivity.this);
            FileLen= currLen.execute().get();
           }
        return FileLen;
    }
    private void comfirmUpdate() {

        final AlertDialog.Builder message = new AlertDialog.Builder(MapsActivity.this);
        message.setTitle(R.string.update_available);
        message.setMessage(R.string.update_text);
        message.setPositiveButton(R.string.update_positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {

                new DownloadFilesTask(MapsActivity.this,restaurants, new DownloadFilesTask.AsyncResponse() {
                    @Override
                    public void processFinish(RestaurantManager output) {
                        restaurants = output;
                        String update_state= dataVersion.getString("updateState","updateFinished");
                        if(update_state.equals("updateFinished"))
                            postUpdate();
                        else
                        {
                            dataVersion.edit().putString("updateState","updateFinished").apply();
                            obtainSupportMapFragment();
                            getLocationPermission();
                            setupLocationManager();
                        }
                    }
                }).execute();



            }

        });
        message.setNegativeButton(R.string.update_negative_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                obtainSupportMapFragment();
                getLocationPermission();
                setupLocationManager();

            }

        });

        message.show();


    }
    private void postUpdate() {
        //when load data,  reset time and file length
        try {
            dataVersion.edit().putLong("fileLen", getOnlineFileLength()).commit();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Timestamp currTime = new Timestamp(System.currentTimeMillis());
        String currTimeString = currTime.toString();
        dataVersion.edit().putString("time", currTimeString).commit();

        //save the current data
        Gson gson = new Gson();
        String json = gson.toJson(restaurants);
        dataVersion.edit().putString("restaurants", "").commit();
        dataVersion.edit().putString("restaurants", json).commit();

        obtainSupportMapFragment();
        getLocationPermission();
        setupLocationManager();

    }

    private boolean launchedFromSingleRestaurant(){

        latitude = (double)dataVersion.getFloat("chosenLatitude",0);
        longitide = (double)dataVersion.getFloat("chosenLongitide",0);
        if(latitude!=0&&longitide!=0)
        {
        dataVersion.edit().putFloat("chosenLatitude", 0).commit();
        dataVersion.edit().putFloat("chosenLongitide", 0).commit();
        return true;}
        else
            return false;
    }



}