package com.example.group_21_project.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.group_21_project.MapsActivity;
import com.example.group_21_project.R;
import com.example.group_21_project.model.Inspection;
import com.example.group_21_project.model.Restaurant;
import com.example.group_21_project.model.RestaurantManager;
import com.example.group_21_project.model.Violation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class restaurant_detail extends AppCompatActivity {
    private RestaurantManager restaurants;
    private Restaurant chosenRestaurant;
    private List<Inspection> inspectionList;
    private String iscritical = "Critical";

    private String ins_id;
    private String date;
    private String type;
    private String hazardRating;
    private int numCritical;
    private int numNonCritical;

    private int selected_num;

    private int code;
    private boolean isCritical;
    private String description;
    private ArrayList<Violation> violations;

    public static Intent makeLaunchRestDetail(Context c) {
        Intent intent = new Intent(c, restaurant_detail.class);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);

        //get the data of the chosen restaurant from MainActivity
        selected_num = setChosenRestaurant();

        //display the information of the restaurant
        Set_restaurant_detail_text();

        //get to the next activity when click on a single inspection
        inspectionListener();
    }



    private int setChosenRestaurant()
    {
        Bundle b = new Bundle();
        b=getIntent().getExtras();
        int selected_num = b.getInt("selected_rest");
        restaurants = RestaurantManager.getInstance();
        chosenRestaurant = restaurants.getRes(selected_num);

        return selected_num;
    }



    //set the detail text of the chosen restaurant
    //set the list of inspections of the chosen restaurant
    private void Set_restaurant_detail_text() {
        TextView rest_name = (TextView)findViewById(R.id.Rest_Name_textview);
        TextView rest_address = (TextView)findViewById(R.id.Rest_address_textview);
        TextView rest_gps = (TextView)findViewById(R.id.Rest_gps_textview);

        ListView list = (ListView) findViewById(R.id.inspection_listview);

        rest_name.setText(chosenRestaurant.getName());
        rest_address.setText(chosenRestaurant.getAddress().getPhysicalAddress());
        rest_gps.setText(chosenRestaurant.getAddress().getLatitide()+", "+chosenRestaurant.getAddress().getLongitide());
        SharedPreferences dataVersion = getSharedPreferences("preference",MODE_PRIVATE);
        //experimenting with a clickable textview
        rest_gps.setOnClickListener(new View.OnClickListener() {

            //putExtras for the chosen restaurants GPS coordinates
            double chosenLatitude = chosenRestaurant.getAddress().getLatitide();
            double chosenLongitide = chosenRestaurant.getAddress().getLongitide();


            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                dataVersion.edit().putFloat("chosenLatitude", (float)chosenLatitude).commit();
                dataVersion.edit().putFloat("chosenLongitide", (float)chosenLongitide).commit();
                startActivity(intent);
            }
        });


        inspectionList = new ArrayList<Inspection>();
        for (int i = 0 ; i  <chosenRestaurant.getInspections().size();i++){

            inspectionList.add(chosenRestaurant.getInspections().get(i));
        }

        ArrayAdapter<Inspection> adapter = new InspectionListAdapter();   // Items to be displayed
        list.setAdapter(adapter);
    }

    // get the inspection data to adapter
    private class InspectionListAdapter extends ArrayAdapter<Inspection> {
        public InspectionListAdapter() {
            super(restaurant_detail.this, R.layout.inspection_item, inspectionList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View listView = convertView;

            if (listView == null) {
                listView = getLayoutInflater().inflate(R.layout.inspection_item, parent, false);
            }

            Inspection currentIns = inspectionList.get(position);
            int temp_harzed_level = 0;
            if (currentIns.getHazardRating().equals("High")) {
                temp_harzed_level = R.drawable.high;
            }
            if (currentIns.getHazardRating().equals("Moderate")) {
                temp_harzed_level = R.drawable.medium;
            }
            if (currentIns.getHazardRating().equals("Low")) {
                temp_harzed_level = R.drawable.sad;
            }

            ImageView icon = (ImageView) listView.findViewById(R.id.inspection_list_hazard);
            icon.setImageResource(temp_harzed_level);

            TextView date = (TextView) listView.findViewById(R.id.inspection_list_date);

            TextView num_crit = (TextView) listView.findViewById(R.id.inspection_list_num_crit);
            num_crit.setText("" + currentIns.getNumCritical());

            TextView num_notcrit = (TextView) listView.findViewById(R.id.inspection_list_num_notcrit);
            num_notcrit.setText("" + currentIns.getNumNonCritical());

            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyyMMdd");
            Date newdate= new Date();
            try {
                newdate = originalFormat.parse(currentIns.getDate());
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

            date.setText(inspectiondate);

            return listView;
        }
    }
    private void inspectionListener() {
        ListView ins_list = (ListView) findViewById(R.id.inspection_listview);
        ins_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                Intent i = inspection_detail.makeLaunchInspectionDetail(restaurant_detail.this);
                i.putExtra("selected_restaurant_id", restaurants.getRes(selected_num).getId());
                i.putExtra("selected_inspection", position);
                startActivity(i);
            }
        });
    }



}