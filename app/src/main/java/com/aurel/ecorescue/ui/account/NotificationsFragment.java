package com.aurel.ecorescue.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.utils.NavigationDrawer;
import com.aurel.ecorescue.utils.StyleUtils;
import com.aurel.ecorescue.view.MainActivity;

import org.jetbrains.annotations.NotNull;


public class NotificationsFragment extends Fragment {

    public NotificationsFragment() {}

    private NavController navController;
    boolean temp1 = false, temp2 = false;

    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    public void onViewCreated(@NotNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        navController = Navigation.findNavController(view);
        toolbar.setNavigationOnClickListener(v -> navController.navigateUp());
        if (getContext() != null) {
            toolbar.setNavigationIcon(StyleUtils.getHomeAsUpIndicator(getContext()));
        }

        TextView tvNotifications = view.findViewById(R.id.tv_notifications);
        TextView tvCriticalAlert = view.findViewById(R.id.tv_critical_alert);


        view.findViewById(R.id.rl_notifications).setOnClickListener(v -> {
            tvNotifications.setText(getContext().getResources().getString(!temp1 ? R.string.enabled : R.string.disabled));
            temp1 = !temp1;
        });
        view.findViewById(R.id.rl_critical_alert).setOnClickListener(v -> {
            tvCriticalAlert.setText(getContext().getResources().getString(!temp2 ? R.string.enabled : R.string.disabled));
            temp2 = !temp2;
        });

    }


}
