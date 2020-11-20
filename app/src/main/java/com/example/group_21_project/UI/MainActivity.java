package com.example.group_21_project.UI;


import android.content.Context;

import android.content.DialogInterface;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.group_21_project.MapsActivity;
import com.example.group_21_project.R;
import com.example.group_21_project.model.DownloadFilesTask;
import com.example.group_21_project.model.Inspection;
import com.example.group_21_project.model.Restaurant;
import com.example.group_21_project.model.RestaurantManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RestaurantManager restaurants;
    private SharedPreferences previous_filter;
    private boolean usingFilter;
    private List<Integer> restIndex;

    public static Intent makeLaunchMainActivity(Context c) {
        Intent intent = new Intent(c, MainActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        startActivity(new Intent(this, MapsActivity.class));
        Intent i = new Intent(this, MapsActivity.class);
        startActivityForResult(i,1);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(i,2);
            }
        });

    }

    /*the main activity will show the list when:
      request code 1: when finish the map activity
      request code 2: when finish the filter activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){

            previous_filter = getSharedPreferences("filter", MODE_PRIVATE);
            usingFilter = previous_filter.getBoolean("isFilter",false);

            if(usingFilter) {
                try {
                    displayFilterResult();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            else{
            getDataFromLastTime();

            try {
                populateListView(false);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            registerClickCallback();}
        }
        if(requestCode==2)
        {
            previous_filter = getSharedPreferences("filter", MODE_PRIVATE);
            usingFilter = previous_filter.getBoolean("isFilter",false);

            if(usingFilter) {
                try {
                    displayFilterResult();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    displayAll();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void getDataFromLastTime()
    {
        SharedPreferences dataVersion = getSharedPreferences("preference",MODE_PRIVATE);
        Gson gson = new Gson();
        String json = dataVersion.getString("restaurants", "");
        restaurants= gson.fromJson(json, RestaurantManager.class);
//        RestaurantManager.setInstance(restaurants);
    }

    private void displayAll() throws ParseException {
        getDataFromLastTime();
        populateListView(false);
        registerClickCallback();

    }

    private void displayFilterResult() throws ParseException {

        getDataFromLastTime();
        populateListView(true);
        registerClickCallback();
    }



    private List<Restaurant> myItems;
    private void populateListView(boolean showingFilter) throws ParseException {
        myItems = new ArrayList<Restaurant>();

        if(showingFilter) {
            restIndex = new ArrayList<>();
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
        }
        else {
            restIndex = new ArrayList<>();
            for (int i = 0; i < restaurants.size(); i++) {

                myItems.add(restaurants.getRes(i));
                restIndex.add(i);
                SharedPreferences dataVersion = getSharedPreferences("preference", MODE_PRIVATE);
                if (dataVersion.contains(restaurants.getRes(i).getName())) {
                    updateinspection = updateinspection + dataVersion.getString(restaurants.getRes(i).getName(), "") + "\n";
                    dataVersion.edit().putString("newupdate", "").commit();
                    dataVersion.edit().putString("newupdate", updateinspection).commit();
                }
            }
        }

        ArrayAdapter<Restaurant> adapter = new MylistAdapter();   // Items to be displayed

        // Configure the list view.
        ListView list = (ListView) findViewById(R.id.listViewMain);
        list.setAdapter(adapter);

        newupdateinfo();

    }

    private String updateinspection = "";
    private class MylistAdapter extends ArrayAdapter <Restaurant>{
        public MylistAdapter() {
            super(MainActivity.this,R.layout.res_item, myItems);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;

            if (itemView == null){
                itemView = getLayoutInflater().inflate(R.layout.res_item,parent,false);
            }
            SharedPreferences dataVersion = getSharedPreferences("preference",MODE_PRIVATE);

            Restaurant currentRes = myItems.get(position);
            TextView textName = (TextView) itemView.findViewById(R.id.name);
            textName.setText(currentRes.getName());

            ImageView icon = (ImageView) itemView.findViewById(R.id.icon);
            icon.setImageResource(currentRes.getIconID());

            ImageView hazard = (ImageView) itemView.findViewById(R.id.hazard);
            hazard.setImageResource(currentRes.getHazardLevel());

            TextView textIssues = (TextView) itemView.findViewById(R.id.issues);
            textIssues.setText(""+ currentRes.getIssues());

            TextView textDate = (TextView) itemView.findViewById(R.id.date);

            Button btn = (Button) itemView.findViewById(R.id.favorite);
            if (currentRes.isFavorite()){btn.setBackgroundResource(R.drawable.ic_baseline_red_24);}
            else {btn.setBackgroundResource(R.drawable.ic_baseline_gray_24);}

            Gson gson = new Gson();
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(currentRes.isFavorite()) {
                        restaurants.getRes(position).setFavorite(false);
                        btn.setBackgroundResource(R.drawable.ic_baseline_gray_24);
                        String json = gson.toJson(restaurants);
                        dataVersion.edit().putString("restaurants", "").commit();
                        dataVersion.edit().putString("restaurants", json).commit();
                        dataVersion.edit().putBoolean(restaurants.getRes(position).getId(),false).commit();
                        RestaurantManager.setInstance(restaurants);
                    }
                    else {
                        restaurants.getRes(position).setFavorite(true);
                        btn.setBackgroundResource(R.drawable.ic_baseline_red_24);
                        String json = gson.toJson(restaurants);
                        dataVersion.edit().putString("restaurants", "").commit();
                        dataVersion.edit().putString("restaurants", json).commit();
                        dataVersion.edit().putBoolean(restaurants.getRes(position).getId(),true).commit();
                        RestaurantManager.setInstance(restaurants);
                    }
                }
            });

            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyyMMdd");
            Date newdate= new Date();
            try {
                newdate = originalFormat.parse(Integer.toString(currentRes.getDate()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date c = Calendar.getInstance().getTime();
            long milliseconds = c.getTime() - newdate.getTime();
            long duration = milliseconds / (1000 * 60 * 60 * 24);
            String inspectiondate = "";

//          compare the years month and days
            SimpleDateFormat yeardiff = new SimpleDateFormat("MMM yyyy");
            if (duration > 365){
                SimpleDateFormat durationdays = new SimpleDateFormat("MMM yyyy");
                inspectiondate = durationdays.format(newdate);
            } else if (duration > 30){
                SimpleDateFormat durationdays = new SimpleDateFormat("MMM dd");
                inspectiondate = durationdays.format(newdate);
            } else {
                SimpleDateFormat durationdays = new SimpleDateFormat("dd");
                inspectiondate = durationdays.format(newdate) + "days";
            }
            textDate.setText(inspectiondate);
            return itemView;
//            return super.getView(position, convertView, parent);
        }
    }

    private void newupdateinfo() {
        SharedPreferences dataVersion = getSharedPreferences("preference",MODE_PRIVATE);

        if (dataVersion.getString("newupdate", "") != "") {
            updateinspection = dataVersion.getString("newupdate", "");
            final AlertDialog.Builder message = new AlertDialog.Builder(MainActivity.this);
            message.setTitle("New update inspection");
            message.setMessage(updateinspection);
            message.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, int which) {

                }
            });

            message.show();
        }
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




    private void registerClickCallback() {
        ListView list = (ListView) findViewById(R.id.listViewMain);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                Intent i = restaurant_detail.makeLaunchRestDetail(MainActivity.this);
                i.putExtra("selected_rest", restIndex.get(position));
                startActivity(i);
            }
        });
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case R.id.filter_button:
                Intent a = new Intent(this, filter.class);
                startActivityForResult(a,2);

        }
        return super.onOptionsItemSelected(item);
    }




}