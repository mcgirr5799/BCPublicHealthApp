package com.example.group_21_project.UI;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.example.group_21_project.R;
import com.example.group_21_project.model.Inspection;
import com.example.group_21_project.model.Restaurant;
import com.example.group_21_project.model.RestaurantManager;
import com.example.group_21_project.model.Violation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class inspection_detail extends AppCompatActivity {
    private RestaurantManager restaurants;
    private Restaurant chosenRestaurant;
    private Inspection chosenInspection;

    private List<Violation> violationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection_detail);

        // sets chosen inspection
        setChosenInspection();

        // sets misc text such as title and number of critical/non-critical issues
        setInspectionText();

        // sets the full, formatted date of the inspection
        setDateText();

        // sets the hazard level of the inspection
        setHazedIcon();

        violationListener();
    }

    //get context
    private void setChosenInspection()
    {
        Bundle b = new Bundle();
        b = getIntent().getExtras();
        String restaurant_id = b.getString("selected_restaurant_id");
        int selected_inspection = b.getInt("selected_inspection");
        restaurants = RestaurantManager.getInstance();

        // restaurant_id is the id of the restaurant whose inspections we are viewing
        chosenRestaurant = restaurants.getRestaurantByID(restaurant_id);

        // selected_inspection is the position clicked on the list
        chosenInspection = chosenRestaurant.getInspections().get(selected_inspection);
    }

     private void setInspectionText() {
         TextView title = findViewById(R.id.restaurantTitle_textview);
         title.setText(chosenRestaurant.getName());

         TextView criticalIssues = findViewById(R.id.criticalIssueNumber_TextView);
         criticalIssues.setText(String.valueOf(chosenInspection.getNumCritical()));

         TextView nonCriticalIssues = findViewById(R.id.nonCriticalIssueNumber_TextView);
         nonCriticalIssues.setText(String.valueOf(chosenInspection.getNumNonCritical()));

         TextView inspection_type = findViewById(R.id.inspection_type_text);
         if ("Follow-Up".equals(String.valueOf(chosenInspection.getType()))) {
             inspection_type.setText(R.string.inspection_type_follow_up);
         } else {
             inspection_type.setText(R.string.inspection_type_routine);
         }


         //listview on activity
         ListView list = (ListView) findViewById(R.id.violationList);

         violationList = new ArrayList<Violation>();
         for (int i = 0 ; i  < chosenInspection.getViolations().size();i++){

             Violation temp = chosenInspection.getViolations().get(i);

             violationList.add(temp);
         }

         ArrayAdapter<Violation> adapter = new ViolationListAdapter();   // Items to be displayed
         list.setAdapter(adapter);
     }

     private void setDateText() {
         String inspectionDate = "";

         try {
             SimpleDateFormat originalFormat = new SimpleDateFormat("yyyyMMdd");
             Date newDate = originalFormat.parse(chosenInspection.getDate());
             SimpleDateFormat durationdays = new SimpleDateFormat("MMM dd, yyyy");
             inspectionDate = durationdays.format(newDate);
         } catch (ParseException e) {
             e.printStackTrace();
         }

         TextView date = findViewById(R.id.inspectionDate_Date);
         date.setText(inspectionDate);
     }

     private void setHazedIcon() {
         int temp_harzed_level = 0;

         if (chosenInspection.getHazardRating().equals("High")) {
             temp_harzed_level = R.drawable.high;
         }
         if (chosenInspection.getHazardRating().equals("Moderate")) {
             temp_harzed_level = R.drawable.medium;
         }
         if (chosenInspection.getHazardRating().equals("Low")) {
             temp_harzed_level = R.drawable.sad;
         }

         ImageView icon = findViewById(R.id.hazardLevel_ImageView);
         icon.setImageResource(temp_harzed_level);
     }

    private class ViolationListAdapter extends ArrayAdapter<Violation> {
        public ViolationListAdapter() {
            super(inspection_detail.this, R.layout.violation_item, violationList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View listView = convertView;

            if (listView == null) {
                listView = getLayoutInflater().inflate(R.layout.violation_item, parent, false);
            }

            Violation currentIns = violationList.get(position);

            TextView violation = (TextView) listView.findViewById(R.id.violation_list_name);

            int violationDescription = getDescription(currentIns);
            if(violationDescription != 0) {
                violation.setText(getDescription(currentIns));
            } else {
                violation.setText("");
            }


            ImageView isCriticalIcon = (ImageView) listView.findViewById(R.id.violation_iscritical);
            if (currentIns.isCritical())
            {
                isCriticalIcon.setImageResource(R.drawable.red_error_signal);
            }
            else
            {
                isCriticalIcon.setImageResource(R.drawable.yellow_error_signal);
            }

            ImageView viotype = (ImageView) listView.findViewById(R.id.violation_list_hazard);
            String currentDes = currentIns.getDescription();
            if(currentDes.contains("Pests")||currentDes.contains("pests"))
            {
                viotype.setImageResource(R.drawable.bugs);
            }
            else if(currentDes.contains("Equipment")||currentDes.contains("equipment"))
            {
                viotype.setImageResource(R.drawable.oven);
            }
            else if(currentDes.contains("utensils")||currentDes.contains("Utensils"))
            {
                viotype.setImageResource(R.drawable.no_others);
            }
            else if(currentDes.contains("food")||currentDes.contains("Food"))
            {
                viotype.setImageResource(R.drawable.no_food);
            }
            else
            {
                viotype.setImageResource(R.drawable.no);
            }

            return listView;
        }
    }

    public int getDescription(Violation violation) {
        String resourceName = "code_" + violation.getCode();
        return getResources().getIdentifier(resourceName, "string", getPackageName());
    }

    //making a new intent
    public static Intent makeLaunchInspectionDetail(Context c) {
        Intent intent = new Intent(c, inspection_detail.class);
        return intent;
    }

    private void violationListener() {
        ListView ins_list = (ListView) findViewById(R.id.violationList);
        ins_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {

                int violationDescription = getDescription(violationList.get(position));
                if(violationDescription != 0) {
                    AlertDialog.Builder message = new AlertDialog.Builder(inspection_detail.this);
                    message.setTitle(R.string.violation_detail_title);
                    message.setMessage(violationDescription);
                    message.setPositiveButton(R.string.violation_detail_positive_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    message.show();
                }
            }
        });
    }
}