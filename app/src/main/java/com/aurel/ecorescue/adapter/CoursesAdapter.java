package com.aurel.ecorescue.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.recyclerview.widget.RecyclerView;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.model.Course;
import com.aurel.ecorescue.utils.ParseUtils;
import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;


public class CoursesAdapter extends RecyclerView.Adapter {


    private Context context;
    private List<Course> items;
    private NavController navController;


    public CoursesAdapter(Context context, NavController navController) {
        this.context = context;
        this.items = new ArrayList<>();
        this.navController = navController;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        return new CoursesViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final CoursesViewHolder viewHolder = (CoursesViewHolder)holder;
        Course item = items.get(position);

        viewHolder.title.setText(ParseUtils.getString(item.name));
        viewHolder.subtitle.setText(context.getResources().getString(R.string.company) + ": " + ParseUtils.getString(item.organizer));
        viewHolder.content.setText(context.getResources().getString(R.string.location) + ": " + ParseUtils.getString(item.city));

        Glide.with(viewHolder.thumbnail.getContext()).load(item.image).placeholder(R.drawable.ic_news_placeholder).error(R.drawable.ic_news_placeholder).into(viewHolder.thumbnail);

        ViewCompat.setTransitionName(viewHolder.thumbnail, position + "_image");
        viewHolder.container.setOnClickListener(v -> {
            Timber.d("Item clicked: %s", item.toString());
            Bundle bundle = new Bundle();
            bundle.putString("id", item.objectId);
            FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                    .addSharedElement(viewHolder.thumbnail, "image")
                    .build();
            navController.navigate(R.id.action_coursesFragment_to_courseDetailsFragment, bundle, null, extras);
        });
    }


    @Override
    public int getItemCount() {
        if (items == null) return 0;
        return items.size();
    }

    class CoursesViewHolder extends RecyclerView.ViewHolder {
        private ImageView thumbnail;
        private TextView title, subtitle, content;
        private RelativeLayout container;

        CoursesViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false));
            container = itemView.findViewById(R.id.rl_container);
            thumbnail = itemView.findViewById(R.id.iv_thumbnail);
            title = itemView.findViewById(R.id.tv_title);
            subtitle = itemView.findViewById(R.id.tv_subtitle);
            content = itemView.findViewById(R.id.tv_content);
        }
    }

    public void setItems(List<Course> list) {
        items = list;
        notifyDataSetChanged();
    }

}
