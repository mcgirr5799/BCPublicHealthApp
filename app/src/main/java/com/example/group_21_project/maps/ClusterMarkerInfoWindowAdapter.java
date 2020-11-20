package com.example.group_21_project.maps;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.example.group_21_project.R;
import com.example.group_21_project.UI.restaurant_detail;
import com.example.group_21_project.model.RestaurantManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterManager;

public class ClusterMarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter,
                                            ClusterManager.OnClusterItemClickListener<ClusterMarker>,
                                            ClusterManager.OnClusterItemInfoWindowClickListener<ClusterMarker> {
    private Context context;
    private ClusterMarker clusterMarker;

    private RestaurantManager restaurantManager;

    public ClusterMarkerInfoWindowAdapter(Context context) {
        this.context = context;

        this.restaurantManager = RestaurantManager.getInstance();
    }

    @Override
    public View getInfoWindow(Marker marker) {
        View view = View.inflate(context, R.layout.cluster_marker_item, null);

        TextView title = view.findViewById(R.id.cluster_marker_item_title);
        title.setText(clusterMarker.getTitle());

        TextView address = view.findViewById(R.id.cluster_marker_item_address);
        address.setText(clusterMarker.getRestaurant().getAddress().getPhysicalAddress());

        TextView hazardRating = view.findViewById(R.id.cluster_marker_item_hazard);
        if(clusterMarker.getRestaurant().hasInspection()) {
            switch (clusterMarker.getRestaurant().getInspections().get(0).getHazardRating()) {
                case "Moderate":
                    hazardRating.setText(R.string.hazard_rating_moderate);
                    break;
                case "Low":
                    hazardRating.setText(R.string.hazard_rating_low);
                    break;
                default:
                    hazardRating.setText(R.string.hazard_rating_high);
            }
        } else {
            hazardRating.setText(R.string.hazard_rating_high); // setting to High since default hazard level/icon is High in ClusterManagerRenderer
        }

        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public boolean onClusterItemClick(ClusterMarker item) {
        this.clusterMarker = item;
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(ClusterMarker item) {
        int selected_rest = 0;
        for(int i = 0; i < restaurantManager.getRestaurants().size(); i++) {
            if(item.getRestaurant() == restaurantManager.getRes(i)) {
                selected_rest = i;
                break;
            }
        }

        Intent i = restaurant_detail.makeLaunchRestDetail(context);
        i.putExtra("selected_rest", selected_rest);
        context.startActivity(i);
    }
}
