package com.aurel.ecorescue.ui.account;

import android.content.Intent;
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
import com.aurel.ecorescue.data.UserRepository;
import com.aurel.ecorescue.data.UserStatus;
import com.aurel.ecorescue.data.UserStatusRepository;
import com.aurel.ecorescue.interfaces.Callback;
import com.aurel.ecorescue.service.SessionManager;
import com.aurel.ecorescue.utils.BlueDialogListener;
import com.aurel.ecorescue.utils.BlueDialogUtils;
import com.aurel.ecorescue.utils.NavigationDrawer;
import com.aurel.ecorescue.utils.SoundUtils;
import com.aurel.ecorescue.view.MainActivity;
import com.aurel.ecorescue.view.f_login_register.LoginRegisterActivity;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import timber.log.Timber;


public class SettingsFragment extends Fragment implements Callback {

    public SettingsFragment() {}

    private NavigationDrawer mDrawerHelper;

    private View view;

    private SettingsRepository mRepository;
    private CheckBox mCheckBox;
    private TextView mAlarmTone;

    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
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

        mCheckBox = view.findViewById(R.id.cb_receive_test_alarm);
        mAlarmTone = view.findViewById(R.id.tv_selected_tone);


        setUpSelectableTv();


        view.findViewById(R.id.btn_delete_account).setOnClickListener(v -> deleteAccount());
        view.findViewById(R.id.btn_log_out).setOnClickListener(v -> confirmLogOut());
        view.findViewById(R.id.tv_selected_tone).setOnClickListener(v -> selectTone());


        mAlarmTone.setText(SoundUtils.convertToReadableSoundName(mRepository.getAlarmTone()));
        mCheckBox.setChecked(mRepository.getReceiveTestAlarmOnSaturday());

        mCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            receiveTestAlarm(isChecked);
            mCheckBox.setEnabled(false);
        });

    }

    private void setUpSelectableTv(){
        view.findViewById(R.id.btn_test_alarm).setOnClickListener(v -> testEmergencyNow());
    }

    private void receiveTestAlarm(boolean receive){
        mRepository.setReceiveTestAlarmOnSaturday(receive, this);
    }

    private void changeTone(String sound){
        mRepository.setAlarmTone(sound, this);
    }

    private void selectTone(){
        SoundUtils soundUtils = new SoundUtils(getContext());

        String[] sounds = SoundUtils.getReadableSoundNames();
        List<String> list = Arrays.asList(sounds);
        int itemSelected = list.indexOf(SoundUtils.convertToReadableSoundName(mRepository.getAlarmTone()));

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext())
                .setTitle(R.string.select_one_of_the_tones_from_the_list)
                .setSingleChoiceItems(sounds, itemSelected, (dialogInterface, selectedIndex) -> {
                    soundUtils.stopSound();
                    soundUtils.playSound(SoundUtils.convertToServerSoundName(sounds[selectedIndex]), false);
                    mAlarmTone.setText(sounds[selectedIndex]);
                })
                .setPositiveButton("Ok", (dialog, which) -> {
                    soundUtils.stopSound();
                    mRepository.setAlarmTone(SoundUtils.convertToServerSoundName(mAlarmTone.getText().toString()), this);

                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    soundUtils.stopSound();
                    mAlarmTone.setText(sounds[itemSelected]);
                })
                .setOnDismissListener(dialog -> soundUtils.stopSound())
                .setOnCancelListener(dialog -> soundUtils.stopSound());
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setAllCaps(false);
    }

    private void testEmergencyNow(){
        mRepository.testEmergencyNow(this);
    }

    private void deleteAccount(){
//        new AlertDialog.Builder(view.getContext())
//                .setTitle(R.string.delete_account)
//                .setMessage(R.string.are_you_sure_to_delete_account)
//                .setPositiveButton(R.string.ok, (dialog, which) -> {
//                    Timber.d("Clicked: Delete account!");
//                    mRepository.deleteAccount(this);
//                    logOut();
//                })
//                .setNegativeButton(R.string.cancel, (dialog, which) -> {})
//                .show();

        Callback callback = this;
        BlueDialogUtils blueDialog = new BlueDialogUtils(getContext(), new BlueDialogListener() {
            @Override
            public void onPositiveClick() {
                Timber.d("Clicked: Delete account!");
                mRepository.deleteAccount(callback);
                logOut();
            }

            @Override
            public void onNegativeClick() {

            }

            @Override
            public void onNeutralClick() {

            }
        }, true);
        blueDialog.setContentText(R.string.are_you_sure_to_delete_account);
        blueDialog.setPositiveText(view.getContext().getResources().getString(R.string.ok));
        blueDialog.setNegativeText(view.getContext().getResources().getString(R.string.cancel));
        blueDialog.show();
    }

    private void confirmLogOut(){
//        new AlertDialog.Builder(view.getContext())
//                .setTitle(R.string.log_out)
//                .setMessage(R.string.are_you_sure_to_log_out)
//                .setPositiveButton(R.string.ok, (dialog, which) -> {
//                    Timber.d("Clicked: Log out!");
//                    logOut();
//                })
//                .setNegativeButton(R.string.cancel, (dialog, which) -> {})
//                .show();

        BlueDialogUtils blueDialog = new BlueDialogUtils(getContext(), new BlueDialogListener() {
            @Override
            public void onPositiveClick() {
                Timber.d("Clicked: Log out!");
                logOut();
            }

            @Override
            public void onNegativeClick() {

            }

            @Override
            public void onNeutralClick() {

            }
        }, true);
        blueDialog.setContentText(R.string.are_you_sure_to_log_out);
        blueDialog.setPositiveText(view.getContext().getResources().getString(R.string.ok));
        blueDialog.setNegativeText(view.getContext().getResources().getString(R.string.cancel));
        blueDialog.show();
    }

    private void logOut(){
        SessionManager sessionManager = new SessionManager(getContext());
        ParseUser.logOut();
        sessionManager.logoutUser(getActivity());
        UserRepository userRepository = UserRepository.getInstance();
        userRepository.setStatus(UserStatus.NOT_REGISTERED, getContext());
        userRepository.setSigned(false);
        UserStatusRepository userStatusRepository = UserStatusRepository.getInstance();
        userStatusRepository.updateUserStatus();
        startActivity(new Intent(getContext(), LoginRegisterActivity.class));
        getActivity().finish();
    }


    @Override
    public void onSuccess(String id, String message) {
        if (id.equals("testEmergencyNow")) {
            Toast.makeText(view.getContext(), getResources().getString(R.string.test_emergency_will_soon_start), Toast.LENGTH_SHORT).show();
            enableView(id);
        } else if (!id.equals("deleteAccount")) {
            Toast.makeText(view.getContext(), getResources().getString(R.string.user_configuration_saved), Toast.LENGTH_SHORT).show();
            enableView(id);
        }
    }

    @Override
    public void onError(String id, String message) {
        if (id.equals("testEmergencyNow") && message.equals("not_activated")) {
            Toast.makeText(view.getContext(), this.getString(R.string.user_must_be_activated), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(view.getContext(), message, Toast.LENGTH_SHORT).show();
            enableView(id);
        }
        
    }

    private void enableView(String id){
        if (id.equals("receivesPracticeAlarm")){
            mCheckBox.setEnabled(true);
        }
    }


}
