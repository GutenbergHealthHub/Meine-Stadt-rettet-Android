package com.aurel.ecorescue.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.model.InformationItem;
import com.aurel.ecorescue.view.news.InformationItemActivityX;
import com.aurel.ecorescue.x_tools.x_DateFormatter;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by daniel on 6/11/17.
 */

public class InformationItemAdapter extends RecyclerView.Adapter<InformationItemAdapter.ViewHolder> {

    private List<InformationItem> items;
    private int itemLayout;
    private Context ctx;

    public InformationItemAdapter(List<InformationItem> items, int itemLayout) {
        this.items = items;
        this.itemLayout = itemLayout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ctx = parent.getContext();
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        v.setOnClickListener(clickListener);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        InformationItem item = items.get(position);
        holder.title.setText(item.title);
        if (holder.timestamp != null && item.Timestamp != null) {
            holder.timestamp.setText(x_DateFormatter.GetStandardDateFormatter().format(item.Timestamp));
            holder.timestamp.setVisibility(View.VISIBLE);
        } else {
            holder.timestamp.setVisibility(View.GONE);
        }
        if (holder.subtitle != null) {
            holder.subtitle.setText(item.description);
        }
        holder.image.setImageBitmap(null);
        if (item.Image != null) {
            holder.image.setVisibility(View.VISIBLE);
            Glide.with(holder.image.getContext()).load(item.Image).into(holder.image);
        } else {
            holder.image.setVisibility(View.INVISIBLE);
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
        public TextView timestamp;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            title = (TextView) itemView.findViewById(R.id.title);
            subtitle = (TextView) itemView.findViewById(R.id.subtitle);
            timestamp = (TextView) itemView.findViewById(R.id.timestamp);
        }
    }

    public void SetList(List<InformationItem> list) {
        items = list;
        notifyDataSetChanged();
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            InformationItem item = (InformationItem) v.getTag();
            Intent intent = new Intent(ctx, InformationItemActivityX.class);
            intent.putExtra("id", item.Id);
            intent.putExtra("type", item.Type);
            ctx.startActivity(intent);
        }
    };
}
