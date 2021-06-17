package com.aurel.ecorescue.profile;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.data.UserRepository;
import com.aurel.ecorescue.data.UserStatusRepository;
import com.aurel.ecorescue.view.f_login_register.LoginRegisterActivity;
import com.bumptech.glide.Glide;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;

/**
 * Created by daniel on 6/25/17.
 */

public class UserViewPopulator {

    public static void PopulateUserView(View header, final AppCompatActivity activity) {
        boolean loggedInUser = true;
        ParseUser user = ParseUser.getCurrentUser();

        if (user == null || ParseAnonymousUtils.isLinked(user)) {
            loggedInUser = false;
        } else {
            try {
                user.fetchIfNeeded();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        UserRepository userRepository = UserRepository.getInstance();
        userRepository.refreshData(activity);


        ImageView userImage = header.findViewById(R.id.image);
        TextView userName = header.findViewById(R.id.name);
        TextView userEmail = header.findViewById(R.id.email);
        TextView userStatus = header.findViewById(R.id.status_value);

        userStatus.setTextColor(ContextCompat.getColor(activity, R.color.md_black_1000));

        userRepository.getNameSurname().observe(activity, s -> {
            if (s != null) {
                userName.setText(s);
            }
        });

        userRepository.getEmail().observe(activity, s -> {
            if (s != null) {
                userEmail.setText(s);
            }
        });

        UserStatusRepository userStatusRepository = UserStatusRepository.getInstance();
        userStatusRepository.getUserStatus().observe(activity, status -> {
            switch (status) {
                case ACTIVE:
                    userStatus.setTextColor(ContextCompat.getColor(activity, R.color.md_green_700));
                    userStatus.setText(" " + activity.getResources().getString(R.string.user_active) + " ");
                    break;
                case INACTIVE:
                case TEMPORARY_INACTIVE:
                    userStatus.setTextColor(ContextCompat.getColor(activity, R.color.md_red_700));
                    userStatus.setText(" " + activity.getResources().getString(R.string.user_inactive) + " ");
                    break;
                case NOT_REGISTERED:
                    userStatus.setTextColor(ContextCompat.getColor(activity, R.color.md_red_700));
                    userStatus.setText(" " + activity.getResources().getString(R.string.user_not_registered) + " ");
                    break;
            }
        });

        boolean logged = loggedInUser;
        userRepository.getProfileImageUrl().observe(activity, s -> {
//            Timber.d("URL: %s", s);
            if (s != null && !s.isEmpty()) {
                Glide.with(activity)
                        .load(s)
                        .centerCrop()
                        .placeholder(R.drawable.logo_v2)
                        .into(userImage);
            } else {
                if (logged) {
                    Glide.with(activity)
                            .load(R.drawable.logo_v2)
                            .into(userImage);
                } else {
                    Glide.with(activity)
                            .load(R.drawable.logo_v2)
                            .into(userImage);
                }

            }
        });

        if (loggedInUser) {

//            userStatus.setOnClickListener(v -> {
//                Intent loginIntent = new Intent(activity, ProfileMainActivity.class);
//                activity.startActivity(loginIntent);
//            });
            userStatus.setOnClickListener(null);
            header.findViewById(R.id.btn_login_register).setVisibility(View.GONE);
        } else {
            userImage.setImageResource(R.drawable.logo_v2);
            userImage.setBackground(userImage.getContext().getResources().getDrawable(android.R.color.transparent));
            userStatus.setVisibility(View.GONE);
            header.findViewById(R.id.btn_login_register).setVisibility(View.VISIBLE);
            header.findViewById(R.id.btn_login_register).setOnClickListener(v -> {
                Intent loginIntent = new Intent(activity, LoginRegisterActivity.class);
                activity.startActivity(loginIntent);
            });
//            userStatus.setOnClickListener(v -> {
//                Intent loginIntent = new Intent(activity, LoginRegisterActivity.class);
//                activity.startActivity(loginIntent);
//            });
        }
    }


}
