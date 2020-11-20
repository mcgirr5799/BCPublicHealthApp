package com.example.group_21_project.UI;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import com.example.group_21_project.MapsActivity;
import com.example.group_21_project.R;

import java.util.ArrayList;


public class filter extends AppCompatActivity {

    private SharedPreferences previous_filter;
    private String hzLevel;
    private int crit_from;
    private int crit_to;
    private boolean isFavorite;
    private String restaurantName;
    boolean is_from_map;


    public static Intent makeLaunchFilter(Context c) {
        Intent intent = new Intent(c, filter.class);
        return intent;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        //set Spinner for hazard level
        previous_filter = getSharedPreferences("filter", MODE_PRIVATE);
        is_from_map = previous_filter.getBoolean("is_from_map",false);
        final Spinner hz_level = findViewById(R.id.filter_hz_level);
//        int[] levels = new int[]{R.string.hazard_rating_none,
//                R.string.hazard_rating_high, R.string.hazard_rating_moderate,
//                R.string.hazard_rating_low};
//        ArrayAdapter<String> level_list = new ArrayAdapter<>(this, R.array.hazard_level, android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<CharSequence> level_list = ArrayAdapter.createFromResource(this, R.array.hazard_level, android.R.layout.simple_spinner_dropdown_item);
        hz_level.setAdapter(level_list);

        //set the config recorded from last time
        setDefaultDisplay();
        if (hzLevel.equals("High"))
            hz_level.setSelection(1);
        if (hzLevel.equals("Moderate"))
            hz_level.setSelection(2);
        if (hzLevel.equals("Low"))
            hz_level.setSelection(3);

        hz_level.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selected_hz_level = hz_level.getSelectedItemPosition();
                if (selected_hz_level == 0)
                    hzLevel = "None";
                if (selected_hz_level == 1)
                    hzLevel = "High";
                if (selected_hz_level == 2)
                    hzLevel = "Moderate";
                if (selected_hz_level == 3)
                    hzLevel = "Low";

            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Switch filter_favorite = findViewById(R.id.filter_favorite);
        filter_favorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isFavorite = true;
                } else {
                    isFavorite = false;
                }
            }
        });

        /*
        when the user complete the filter and start search:
        1. get the result from the user
        2. check if the user use the filter (check the result with default values)
        3. if use filter, record a boolean to tell the main activity to display the filter
        4. if nothing changed (as default), or tap on clear filter, tell the main activity to display all
        */
        Button search_filter = findViewById(R.id.filter_search);
        search_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getFilterSetting();

            }
        });

        Button clear_filter = findViewById(R.id.filter_clear);
        clear_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hz_level.setSelection(0);
                getFilterClear();

            }
        });

        Button search_name = findViewById(R.id.filter_search_name);
        search_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getFilterSetting();

            }
        });

        Button close_filter = findViewById(R.id.filter_close);
        close_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(is_from_map){
                    Intent I = new Intent(filter.this,MapsActivity.class);
                    previous_filter.edit().putBoolean("is_from_map",false).apply();
                    startActivity(I);
                    finish();

                }
                else
                {
                Intent returnIntent = new Intent();
                setResult(RESULT_CANCELED,returnIntent);
                finish();}

            }
        });


    }


    private void setDefaultDisplay() {
        hzLevel = previous_filter.getString("hzLevel", "None");
        crit_from = previous_filter.getInt("crit_from", 0);
        crit_to = previous_filter.getInt("crit_to", 0);
        isFavorite = previous_filter.getBoolean("isFavorite", false);
        restaurantName = previous_filter.getString("restaurantName", "");

        EditText filter_crit_from = findViewById(R.id.filter_critical_from);
        EditText filter_crit_to = findViewById(R.id.filter_critical_to);

        EditText filter_restaurantName = findViewById(R.id.filter_restaurant_name);

        filter_crit_from.setText("" + crit_from);
        filter_crit_to.setText("" + crit_to);

        filter_restaurantName.setText("" + restaurantName);

        Switch filter_favorite = findViewById(R.id.filter_favorite);
        filter_favorite.setChecked(isFavorite);
    }


    private void getFilterSetting() {



        EditText filter_crit_from = findViewById(R.id.filter_critical_from);
        EditText filter_crit_to = findViewById(R.id.filter_critical_to);

        EditText filter_restaurantName = findViewById(R.id.filter_restaurant_name);
        restaurantName = filter_restaurantName.getText().toString();
        if(filter_crit_from.getText().toString().matches("")){crit_from = 0;}
        else
            crit_from = Integer.parseInt(filter_crit_from.getText().toString());
        if(filter_crit_to.getText().toString().matches("")){crit_to = 0;}
        else
            crit_to = Integer.parseInt(filter_crit_to.getText().toString());

        if (crit_from < 0 || crit_to < 0) {
            AlertDialog.Builder message = new AlertDialog.Builder(filter.this);
            message.setTitle(R.string.filter_invalid_critical_issue_num);
            message.setMessage(R.string.filter_critical_issue_greater_than_zero);
            message.setPositiveButton(R.string.filter_positive_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            message.show();

        } else if (crit_to < crit_from) {
            AlertDialog.Builder message = new AlertDialog.Builder(filter.this);
            message.setTitle(R.string.filter_invalid_max_crit_issue);
            message.setMessage(R.string.filter_max_crit_issue);
            message.setPositiveButton(R.string.filter_positive_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            message.show();
        }
        else {
            saveData();
        }
    }

    private void saveData() {
        Log.d("save", "saveData: "+isFavorite);
        previous_filter.edit().putString("hzLevel", hzLevel).apply();
        previous_filter.edit().putInt("crit_from",crit_from).apply();
        previous_filter.edit().putInt("crit_to",crit_to).apply();
        previous_filter.edit().putBoolean("isFavorite",isFavorite).apply();
        previous_filter.edit().putString("restaurantName",restaurantName).apply();

        previous_filter.edit().putBoolean("isFilter",isFilterChanged()).apply();

        if(is_from_map){
            Intent I = new Intent(filter.this,MapsActivity.class);
            previous_filter.edit().putBoolean("is_from_map",false).apply();
            startActivity(I);
            finish();

        }
        else
        {
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED,returnIntent);
        finish();}

    }


    private void getFilterClear() {
        EditText filter_crit_from = findViewById(R.id.filter_critical_from);
        EditText filter_crit_to = findViewById(R.id.filter_critical_to);
        EditText filter_restaurantName = findViewById(R.id.filter_restaurant_name);

        filter_crit_from.setText("" + 0);
        filter_crit_to.setText("" + 0);
        filter_restaurantName.setText("");

        Switch filter_favorite = findViewById(R.id.filter_favorite);
        filter_favorite.setChecked(false);

        previous_filter.edit().putString("hzLevel", "None").apply();
        previous_filter.edit().putInt("crit_from",0).apply();
        previous_filter.edit().putInt("crit_to",0).apply();
        previous_filter.edit().putBoolean("isFavorite",false).apply();
        previous_filter.edit().putString("restaurantName", "").apply();


    }

    private boolean isFilterChanged()
    {
        if(hzLevel.equals("None")&&crit_from==0&&crit_to==0&&isFavorite==false&&restaurantName.equals(""))
            return false;
        else
            return true;
    }


}