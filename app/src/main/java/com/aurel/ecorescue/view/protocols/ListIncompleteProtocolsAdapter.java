package com.aurel.ecorescue.view.protocols;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.x_tools.x_Helper;
import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Map;

/**
 * Created by aurel on 10-Oct-16.
 */

public class ListIncompleteProtocolsAdapter extends SimpleAdapter {

    public ListIncompleteProtocolsAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        Map item = (Map)getItem(position);
        TextView statusText = view.findViewById(R.id.status);
        statusText.setTextColor((int)item.get("status_color"));
        ImageView mapImage = view.findViewById(R.id.mapImage);
        if ((double)item.get("lat") != 0 && (double)item.get("lng") != 0) {
            int size = x_Helper.GetPixelsForDPValue(mapImage.getContext(), 128);
            String url = "http://maps.googleapis.com/maps/api/staticmap?center=" + (double)item.get("lat") + "," + (double)item.get("lng")
                    + "&zoom=12&size=" + size + "x" + size
                    + "&markers=color:red%7Clabel:" + mapImage.getContext().getString(R.string.emergency) + "%7C" + (double)item.get("lat") + "," + (double)item.get("lng")
                    + "&key=" + mapImage.getContext().getString(R.string.google_maps_key);
            Glide.with(mapImage.getContext()).load(url).into(mapImage);
        }


        return view;
    }

}