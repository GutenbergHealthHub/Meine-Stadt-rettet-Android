package com.aurel.ecorescue.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.model.News;
import com.aurel.ecorescue.utils.ParseUtils;
import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;


public class NewsAdapter extends RecyclerView.Adapter {


    private Context context;
    private List<News> items;
    private NavController navController;


    public NewsAdapter(Context context, NavController navController) {
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
        News item = items.get(position);

        if (position == 0) {
            viewHolder.rl_container.setVisibility(View.GONE);
            viewHolder.rl_cover_container.setVisibility(View.VISIBLE);
            viewHolder.cover_title.setText(ParseUtils.getString(item.title));
            viewHolder.cover_subtitle.setText(ParseUtils.getString(item.subtitle));
            if (item.imageObject!=null)
            Glide.with(context)
                    .load(item.imageObject)
                    .centerCrop()
                    .placeholder(R.drawable.ic_news_placeholder)
                    .into(viewHolder.cover_thumbnail);
//            Picasso.with(viewHolder.cover_thumbnail.getContext()).load(item.imageObject).placeholder(R.drawable.ic_news_placeholder).error(R.drawable.ic_news_placeholder).into(viewHolder.cover_thumbnail);

        } else {
            viewHolder.rl_container.setVisibility(View.VISIBLE);
            viewHolder.rl_cover_container.setVisibility(View.GONE);
            viewHolder.title.setText(ParseUtils.getString(item.title));
            viewHolder.subtitle.setText(ParseUtils.getString(item.subtitle));

            Glide.with(context)
                    .load(item.imageObject)
                    .centerCrop()
                    .placeholder(R.drawable.ic_news_placeholder)
                    .into(viewHolder.thumbnail);

            //Picasso.with(viewHolder.thumbnail.getContext()).load(item.imageObject).placeholder(R.drawable.ic_news_placeholder).error(R.drawable.ic_news_placeholder).into(viewHolder.thumbnail);
        }


        viewHolder.container.setOnClickListener(v -> {
            Timber.d("Item clicked: %s", item.toString());
            Bundle bundle = new Bundle();
            bundle.putString("id", item.objectId);
            navController.navigate(R.id.action_newsFragment_to_newsDetailsFragment, bundle);
        });
    }


    @Override
    public int getItemCount() {
        if (items == null) return 0;
        return items.size();
    }

    class AdapterViewHolder extends RecyclerView.ViewHolder {
        private ImageView thumbnail, cover_thumbnail;
        private TextView title, cover_title, subtitle, cover_subtitle;
        private FrameLayout container;
        private RelativeLayout rl_container, rl_cover_container;

        AdapterViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news, parent, false));
            container = itemView.findViewById(R.id.fl_container);
            rl_container = itemView.findViewById(R.id.rl_container);
            rl_cover_container = itemView.findViewById(R.id.rl_cover_container);
            thumbnail = itemView.findViewById(R.id.iv_thumbnail);
            cover_thumbnail = itemView.findViewById(R.id.iv_cover_thumbnail);
            title = itemView.findViewById(R.id.tv_title);
            cover_title = itemView.findViewById(R.id.tv_cover_title);
            subtitle = itemView.findViewById(R.id.tv_subtitle);
            cover_subtitle = itemView.findViewById(R.id.tv_cover_subtitle);
        }
    }

    public void setItems(List<News> list) {
        items = list;
        notifyDataSetChanged();
    }

}
