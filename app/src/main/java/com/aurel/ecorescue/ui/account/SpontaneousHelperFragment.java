package com.aurel.ecorescue.ui.account;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.data.SettingsRepository;
import com.aurel.ecorescue.data.SpontaneousHelperRepository;
import com.aurel.ecorescue.data.UserRepository;
import com.aurel.ecorescue.data.UserStatusRepository;
import com.aurel.ecorescue.interfaces.Callback;
import com.aurel.ecorescue.utils.NavigationDrawer;
import com.aurel.ecorescue.view.MainActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class SpontaneousHelperFragment extends Fragment {

    public SpontaneousHelperFragment() {}

    private NavigationDrawer mDrawerHelper;
    private SpontaneousHelperRepository mRepository;


    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_spontaneous_helper, container, false);
    }

    public void onViewCreated(@NotNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        mDrawerHelper = ((MainActivity) requireActivity()).getNavigationDrawer();
        toolbar.setNavigationOnClickListener(v -> mDrawerHelper.openDrawer());
        mRepository = SpontaneousHelperRepository.getInstance();

        CheckBox cbElderlyCare = view.findViewById(R.id.cb_elderly_care);
        CheckBox cbAmbulance = view.findViewById(R.id.cb_support_for_ambulance);
        CheckBox cbPhysical = view.findViewById(R.id.cb_physically_difficult_task);
        CheckBox cbGeneral = view.findViewById(R.id.cb_general);

        cbElderlyCare.setChecked(mRepository.isElderlyCareNotificationEnabled());
        cbAmbulance.setChecked(mRepository.isSupportForAmbulanceServiceNotificationEnabled());
        cbPhysical.setChecked(mRepository.isPhysicallyDifficultTaskNotificationEnabled());
        cbGeneral.setChecked(mRepository.isGeneralSpontaneousAssistanceNotificationEnabled());

        cbElderlyCare.setOnCheckedChangeListener((buttonView, isChecked) ->
                mRepository.setElderlyCareNotification(isChecked));
        cbAmbulance.setOnCheckedChangeListener((buttonView, isChecked) ->
                mRepository.setSupportForAmbulanceServiceNotification(isChecked));
        cbPhysical.setOnCheckedChangeListener((buttonView, isChecked) ->
                mRepository.setPhysicallyDifficultTaskNotification(isChecked));
        cbGeneral.setOnCheckedChangeListener((buttonView, isChecked) ->
                mRepository.setGeneralSpontaneousAssistanceNotification(isChecked));

    }



}
