package com.aurel.ecorescue.ui.news;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.data.DataRepository;
import com.aurel.ecorescue.model.Events;
import com.aurel.ecorescue.utils.NavigationDrawer;
import com.aurel.ecorescue.utils.ParseUtils;
import com.aurel.ecorescue.view.MainActivity;
import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;


public class EventDetailsFragment extends Fragment {

    public EventDetailsFragment() {}

    private NavController navController;
    private DataRepository mRepository;


    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }


    public void onViewCreated(@NotNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.news_tab_events);
        navController = Navigation.findNavController(view);
        mRepository = DataRepository.getInstance();
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(v -> {
            navController.navigateUp();
        });
        Bundle bundle = getArguments();
        if (bundle!=null) {
            Events event = mRepository.getEvent(ParseUtils.getString(bundle.getString("id")));

            if (event!=null) {
                ((TextView) view.findViewById(R.id.tv_title)).setText(ParseUtils.getString(event.title));
                ((TextView) view.findViewById(R.id.tv_title_second)).setText(getResources().getString(R.string.company) + ": " + ParseUtils.getString(event.Organizer));
                ((TextView) view.findViewById(R.id.tv_title_third)).setText(getResources().getString(R.string.location) + ": " + ParseUtils.getString(event.City));
                ((TextView) view.findViewById(R.id.tv_content)).setText(ParseUtils.getString(event.Text));

                ImageView imageView = view.findViewById(R.id.iv_cover_image);
                if (event.Image!=null) {
                    Glide.with(imageView.getContext()).load(event.Image).into(imageView);
                    imageView.setVisibility(View.VISIBLE);
                }

                if (!ParseUtils.getString(event.Url).isEmpty()) {
                    view.findViewById(R.id.btn_continue_reading).setOnClickListener(v -> {
                        String url = ParseUtils.getString(event.Url);

                        if (!url.contains("http")) {
                            url = "https://" + url;
                        }
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    });
                } else {
                    view.findViewById(R.id.btn_continue_reading).setVisibility(View.GONE);
                }

            }

        }
    }


}
