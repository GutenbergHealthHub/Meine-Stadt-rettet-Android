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
import com.aurel.ecorescue.data.UserRepository;
import com.aurel.ecorescue.data.UserStatusRepository;
import com.aurel.ecorescue.interfaces.Callback;
import com.aurel.ecorescue.data.SettingsRepository;
import com.aurel.ecorescue.utils.NavigationDrawer;
import com.aurel.ecorescue.view.MainActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class StandbyModeFragment extends Fragment implements Callback {

    public StandbyModeFragment() {}

    private NavigationDrawer mDrawerHelper;
    private SettingsRepository mRepository;
    private CheckBox mDoNotNotifyMeAtHome;
    private TextView mDaysOff, mTimeFrom, mTimeTo;
    private List<Integer> tempDaysOff;
    private View view;


    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_standby_mode, container, false);
    }

    public void onViewCreated(@NotNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        mDrawerHelper = ((MainActivity) requireActivity()).getNavigationDrawer();
        toolbar.setNavigationOnClickListener(v -> {
            mDrawerHelper.openDrawer();
        });

        mRepository = SettingsRepository.getInstance();
        mDoNotNotifyMeAtHome = view.findViewById(R.id.cb_do_not_notify_at_home);
        mDaysOff = view.findViewById(R.id.tv_no_emergency_days);
        mTimeFrom = view.findViewById(R.id.tv_off_duty_time_from);
        mTimeTo = view.findViewById(R.id.tv_off_duty_time_to);


        updateView();

        mTimeFrom.setOnClickListener(v -> selectOffDutyTimeFrom());
        mTimeTo.setOnClickListener(v -> selectOffDutyTimeTo());
        view.findViewById(R.id.ibtn_cancel_off_duty_time).setOnClickListener(v -> clearOffDutyTimeSelection());
        mDaysOff.setOnClickListener(v -> selectDutyOffDays());
        mDoNotNotifyMeAtHome.setOnCheckedChangeListener((buttonView, isChecked) -> doNotNotifyAtHome(isChecked));

    }

    public void selectOffDutyTimeFrom(){
        Date date = new Date();
        Callback callback = this;
        new TimePickerDialog(getContext(), (timePicker, selectedHour, selectedMinute) -> {
            String time = String.format(Locale.GERMANY, "%02d:%02d", selectedHour, selectedMinute);
            mTimeFrom.setText(time);
            date.setHours(selectedHour);
            date.setMinutes(selectedMinute);
            mRepository.setDutyOffTimeFrom(date, callback);
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity!=null) mainActivity.setUpAlarmReceiverStart(selectedHour, selectedMinute);
            UserRepository userRepository = UserRepository.getInstance();
            userRepository.updateStatus(view.getContext());
            UserStatusRepository userStatusRepository = UserStatusRepository.getInstance();
            userStatusRepository.updateUserStatus();
//            UserStatusRepository userStatusRepository = UserStatusRepository.getInstance();
//            userStatusRepository.updateUserStatus(false, false);
        }, date.getHours(), date.getMinutes(), true).show();
    }

    public void selectOffDutyTimeTo(){
        Date date = new Date();
        Callback callback = this;
        new TimePickerDialog(getContext(), (timePicker, selectedHour, selectedMinute) -> {
            mTimeTo.setText(String.format(Locale.GERMANY, "%02d:%02d", selectedHour, selectedMinute));
            date.setHours(selectedHour);
            date.setMinutes(selectedMinute);
            mRepository.setDutyOffTimeTo(date, callback);
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity!=null) mainActivity.setUpAlarmReceiverFinish(selectedHour, selectedMinute);
            UserRepository userRepository = UserRepository.getInstance();
            userRepository.updateStatus(view.getContext());
            UserStatusRepository userStatusRepository = UserStatusRepository.getInstance();
            userStatusRepository.updateUserStatus();
        }, date.getHours(), date.getMinutes(), true).show();
    }

    public void clearOffDutyTimeSelection(){
        mTimeFrom.setText("");
        mTimeTo.setText("");
        mRepository.setDutyOffTimeFrom(null, this);
        mRepository.setDutyOffTimeTo(null, this);
    }

    public void selectDutyOffDays(){
        tempDaysOff = new ArrayList<>(mRepository.getDutyOffDays());
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.select_days_no_emergency)
                .setMultiChoiceItems(R.array.days, getDaysOffBool(), (dialog, which, isChecked) -> {
                    if (isChecked) {
                        tempDaysOff.add(which+1);
                    } else {
                        tempDaysOff.remove(Integer.valueOf(which+1));
                    }
                })
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    Collections.sort(tempDaysOff);
                    UserRepository userRepository = UserRepository.getInstance();
                    userRepository.updateStatus(view.getContext());
                    UserStatusRepository userStatusRepository = UserStatusRepository.getInstance();
                    userStatusRepository.updateUserStatus();
                    if (!tempDaysOff.equals(mRepository.getDutyOffDays())) {
                        mRepository.setDutyOffDays(tempDaysOff, this);
                        mDaysOff.setText(getDaysOffStr());
                    }
                }).show();
    }

    private void updateView(){
        mTimeFrom.setText(mRepository.getDutyOffTimeFrom());
        mTimeTo.setText(mRepository.getDutyOffTimeTo());
        mDaysOff.setText(getDaysOffStr());
        mDoNotNotifyMeAtHome.setChecked(mRepository.getDoNotNotifyMeAtHome());
    }

    private boolean[] getDaysOffBool(){
        boolean[] daysOffBool = new boolean[7];
        List<Integer> daysOffInt = mRepository.getDutyOffDays();
        for (int i: daysOffInt) {
            daysOffBool[i-1] = true;
        }
        return daysOffBool;
    }

    private String getDaysOffStr(){
        //  TextUtils.join(", ", list);
        List<Integer> daysOffInt = mRepository.getDutyOffDays();
        String[] daysOffArr = getResources().getStringArray(R.array.days);
        StringBuilder result = new StringBuilder();
        for (int i: daysOffInt) {
            if (result.length() != 0) {
                result.append(", ");
            }
            result.append(daysOffArr[i - 1]);
        }
        return result.toString();
    }

    public void doNotNotifyAtHome(boolean doNotNotifyAtHome){
        mRepository.setDoNotNotifyMeAtHome(doNotNotifyAtHome, this);
        mDoNotNotifyMeAtHome.setEnabled(false);
    }

    @Override
    public void onSuccess(String id, String message) {
        Toast.makeText(getContext(), getResources().getString(R.string.user_configuration_saved), Toast.LENGTH_SHORT).show();
        enableView(id);
    }

    @Override
    public void onError(String id, String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        enableView(id);
    }

    private void enableView(String id){
        if (id.equals("setDoNotNotifyMeAtHome")){
            mDoNotNotifyMeAtHome.setEnabled(true);
        }
    }

}
