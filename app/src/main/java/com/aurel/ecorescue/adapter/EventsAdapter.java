package com.aurel.ecorescue.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.model.Events;
import com.aurel.ecorescue.utils.ParseUtils;
import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;


public class EventsAdapter extends RecyclerView.Adapter {


    private Context context;
    private List<Events> items;
    private NavController navController;


    public EventsAdapter(Context context, NavController navController) {
        this.context = context;
        this.items = new ArrayList<>();
        this.navController = navController;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        return new AdapterViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final AdapterViewHolder viewHolder = (AdapterViewHolder)holder;
        Events item = items.get(position);

        viewHolder.title.setText(ParseUtils.getString(item.title));
        viewHolder.subtitle.setText(context.getResources().getString(R.string.company) + ": " + ParseUtils.getString(item.Organizer));
        viewHolder.content.setText(context.getResources().getString(R.string.location) + ": " + ParseUtils.getString(item.City));
//
        Glide.with(viewHolder.thumbnail.getContext()).load(item.Image).placeholder(R.drawable.ic_news_placeholder).error(R.drawable.ic_news_placeholder).into(viewHolder.thumbnail);

        viewHolder.container.setOnClickListener(v -> {
            Timber.d("Item clicked: %s", item.toString());
            Bundle bundle = new Bundle();
            bundle.putString("id", item.objectId);
//            navController.navigate(R.id.action_newsEventsFragment_to_eventDetailsFragment, bundle);
        });
    }


    @Override
    public int getItemCount() {
        if (items == null) return 0;
        return items.size();
    }

    class AdapterViewHolder extends RecyclerView.ViewHolder {
        private ImageView thumbnail;
        private TextView title, subtitle, content;
        private RelativeLayout container;

        AdapterViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false));
            container = itemView.findViewById(R.id.rl_container);
            thumbnail = itemView.findViewById(R.id.iv_thumbnail);
            title = itemView.findViewById(R.id.tv_title);
            subtitle = itemView.findViewById(R.id.tv_subtitle);
            content = itemView.findViewById(R.id.tv_content);
        }
    }

    public void setItems(List<Events> list) {
        items = list;
        notifyDataSetChanged();
    }

}
