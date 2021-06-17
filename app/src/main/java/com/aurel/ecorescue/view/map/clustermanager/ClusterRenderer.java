package com.aurel.ecorescue.view.map.clustermanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aurel.ecorescue.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

public class ClusterRenderer extends DefaultClusterRenderer<AedClusterItem> {

    private Context context;
    final IconGenerator mClusterIconGenerator;

    public ClusterRenderer(Context context, GoogleMap map, ClusterManager clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
        mClusterIconGenerator = new IconGenerator(context);
    }


    @Override
    protected void onBeforeClusterItemRendered(AedClusterItem item, MarkerOptions markerOptions) {
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.mapmarker_defibrillator_verified_v2)));
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<AedClusterItem> cluster, MarkerOptions markerOptions) {
        Drawable drawable = context.getResources().getDrawable(R.drawable.mapmarker_defi_cluster);
        ImageView imageView = new ImageView(context);
        imageView.setImageDrawable(drawable);

        TextView textView = new TextView(context);
        textView.setText(String.valueOf(cluster.getSize()));

        RelativeLayout relativeLayout = new RelativeLayout(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        textView.setLayoutParams(params);
        textView.setGravity(Gravity.CENTER);

        relativeLayout.addView(imageView);
        relativeLayout.addView(textView);

        mClusterIconGenerator.setBackground(drawable);
        mClusterIconGenerator.setContentView(relativeLayout);
        Bitmap icon = mClusterIconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }


}
