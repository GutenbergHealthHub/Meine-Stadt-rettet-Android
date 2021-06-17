package com.aurel.ecorescue.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.model.OwnDefibrillator;
import com.aurel.ecorescue.view.map.CreateDefibrilatorActivityX;

import java.util.List;

public class OwnDefibrillatorAdapter extends RecyclerView.Adapter<OwnDefibrillatorAdapter.ViewHolder> {

    private List<OwnDefibrillator> items;
    private int itemLayout;
    private Context ctx;

    public OwnDefibrillatorAdapter(List<OwnDefibrillator> items, int itemLayout) {
        this.items = items;
        this.itemLayout = itemLayout;
    }

    @Override
    public OwnDefibrillatorAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ctx = parent.getContext();
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        v.setOnClickListener(clickListener);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(OwnDefibrillatorAdapter.ViewHolder holder, int position) {
        OwnDefibrillator item = items.get(position);
        holder.title.setText(item.object);

        if (holder.subtitle != null) {
            holder.subtitle.setText(item.street + " " + item.street_number);
            holder.subtitle2.setText(item.zipcode + " " + item.city);
        }

        holder.image.setImageBitmap(null);

        if (item.state == 1) {
            holder.image.setImageBitmap(BitmapFactory.decodeResource(ctx.getResources(),
                    R.drawable.mapmarker_defibrillator_unverified_v2));
            holder.status.setText(R.string.defrib_status_1);
            holder.status.setTextColor(ctx.getResources().getColor(R.color.md_yellow_900));
        }

        if (item.state == 2) {
            holder.image.setImageBitmap(BitmapFactory.decodeResource(ctx.getResources(),
                    R.drawable.mapmarker_defibrillator_unverified_v2));
            holder.status.setText(R.string.defrib_status_2);
            holder.status.setTextColor(ctx.getResources().getColor(R.color.colorRedDark));
        }

        if (item.state == 3) {
            holder.image.setImageBitmap(BitmapFactory.decodeResource(ctx.getResources(),
                    R.drawable.mapmarker_defibrillator_verified_v2));
            holder.status.setText(R.string.defrib_status_3);
            holder.status.setTextColor(ctx.getResources().getColor(R.color.colorGreenDark));
        }

        if (item.state == 4) {
            holder.image.setImageBitmap(BitmapFactory.decodeResource(ctx.getResources(),
                    R.drawable.mapmarker_defibrillator_verified_v2));
            holder.status.setText(R.string.defrib_status_4);
            holder.status.setTextColor(ctx.getResources().getColor(R.color.colorGreenDark));
        }

        holder.itemView.setTag(item);


    }

    @Override
    public int getItemCount() {
        if (items == null) return 0;
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView title;
        public TextView subtitle;
        public TextView subtitle2;
        public TextView timestamp;
        public TextView status;


        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            title = (TextView) itemView.findViewById(R.id.title);
            subtitle = (TextView) itemView.findViewById(R.id.subtitle);
            subtitle2 = (TextView) itemView.findViewById(R.id.subtitle2);
            timestamp = (TextView) itemView.findViewById(R.id.timestamp);
            status = itemView.findViewById(R.id.status);
        }
    }

    public void SetList(List<OwnDefibrillator> list) {
        items = list;
        notifyDataSetChanged();
    }

    public List<OwnDefibrillator> GetList() {
        return items;
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            OwnDefibrillator item = (OwnDefibrillator) v.getTag();
            Intent intent = new Intent(ctx, CreateDefibrilatorActivityX.class);
            intent.putExtra("id", item.id);
            ctx.startActivity(intent);
        }
    };

}
