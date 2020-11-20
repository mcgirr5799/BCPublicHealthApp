package com.example.group_21_project.maps;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.example.group_21_project.MapsActivity;
import com.example.group_21_project.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

public class ClusterManagerRenderer extends DefaultClusterRenderer<ClusterMarker> {
    private final IconGenerator iconGenerator;
    private final ImageView imageView;
    private final Context context;

    public ClusterManagerRenderer(Context context, GoogleMap map, ClusterManager<ClusterMarker> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;

        iconGenerator = new IconGenerator(context.getApplicationContext());
        imageView = new ImageView(context.getApplicationContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(125, 125));
        imageView.setPadding(10, 10, 10, 10);
        iconGenerator.setContentView(imageView);
    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull ClusterMarker item, @NonNull MarkerOptions markerOptions) {
        int hazardIcon = R.drawable.example_icon;
        int hazardColor = R.color.white;
        if(item.getRestaurant().hasInspection()) {
            switch (item.getRestaurant().getInspections().get(0).getHazardRating()) {
                case "Low":
                    hazardColor = R.color.hazardLow;
                    hazardIcon = R.drawable.high;
                    break;
                case "Moderate":
                    hazardColor = R.color.hazardModerate;
                    hazardIcon = R.drawable.medium;
                    break;
                case "High":
                    hazardColor = R.color.hazardHigh;
                    hazardIcon = R.drawable.sad;
                    break;
            }
        } else {
            hazardColor = R.color.hazardHigh;
            hazardIcon = R.drawable.sad;
        }

        imageView.setBackgroundColor(context.getResources().getColor(hazardColor));
        imageView.setImageResource(hazardIcon);

        Bitmap icon = iconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item.getTitle()).snippet(item.getSnippet());
    }

    @Override
    protected boolean shouldRenderAsCluster(@NonNull Cluster<ClusterMarker> cluster) {
        return cluster.getSize() > 1;
    }
}
