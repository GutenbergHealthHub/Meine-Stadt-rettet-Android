package com.aurel.ecorescue.ui.main;

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
import com.aurel.ecorescue.model.Course;
import com.aurel.ecorescue.utils.NavigationDrawer;
import com.aurel.ecorescue.utils.ParseUtils;
import com.aurel.ecorescue.utils.StyleUtils;
import com.aurel.ecorescue.view.MainActivity;
import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;


public class CourseDetailsFragment extends Fragment {

    public CourseDetailsFragment() {}

    private NavController navController;
    private DataRepository mRepository;


    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_details, container, false);
    }


    public void onViewCreated(@NotNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.drawer_courses);
        navController = Navigation.findNavController(view);
        mRepository = DataRepository.getInstance();

        if (getContext() != null) {
            toolbar.setNavigationIcon(StyleUtils.getHomeAsUpIndicator(getContext()));
        }

        toolbar.setNavigationOnClickListener(v -> {
            navController.navigateUp();
        });
        Bundle bundle = getArguments();
        if (bundle!=null) {
            Course course = mRepository.getCourse(ParseUtils.getString(bundle.getString("id")));

            if (course!=null) {
                ((TextView) view.findViewById(R.id.tv_title)).setText(ParseUtils.getString(course.name));
                ((TextView) view.findViewById(R.id.tv_company)).setText(getResources().getString(R.string.company) + ": " + ParseUtils.getString(course.organizer));
                ((TextView) view.findViewById(R.id.tv_location)).setText(getResources().getString(R.string.location) + ": " + ParseUtils.getString(course.city));
                ((TextView) view.findViewById(R.id.tv_content)).setText(ParseUtils.getString(course.information));

                ImageView imageView = view.findViewById(R.id.iv_cover_image);
                if (course.image!=null) {
                    Glide.with(getContext()).load(course.image).into(imageView);
                    imageView.setVisibility(View.VISIBLE);
                }

                if (!ParseUtils.getString(course.url).isEmpty()) {
                    view.findViewById(R.id.btn_continue_reading).setOnClickListener(v -> {
                        String url = ParseUtils.getString(course.url);

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
