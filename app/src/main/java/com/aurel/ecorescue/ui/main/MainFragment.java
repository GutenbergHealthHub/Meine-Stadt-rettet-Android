package com.aurel.ecorescue.ui.main;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aurel.ecorescue.R;
import com.aurel.ecorescue.data.SettingsRepository;
import com.aurel.ecorescue.data.UserRepository;
import com.aurel.ecorescue.data.UserStatus;
import com.aurel.ecorescue.data.UserStatusRepository;
import com.aurel.ecorescue.utils.AppExecutors;
import com.aurel.ecorescue.utils.NavigationDrawer;
import com.aurel.ecorescue.utils.OffDutyUtils;
import com.aurel.ecorescue.utils.PermissionUtils;
import com.aurel.ecorescue.view.MainActivity;
import com.aurel.ecorescue.view.f_login_register.LoginRegisterActivity;
import com.aurel.ecorescue.view.protocols.ListIncompleteProtocolsActivity;
import com.aurel.ecorescue.x_tools.x_EcoPreferences;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

import static com.aurel.ecorescue.StarterApplication.appExecutors;


public class MainFragment extends Fragment {

    public MainFragment() {}

    private NavController navController;
    private View view;
    private UserRepository mRepository;
    private UserStatusRepository userStatusRepository;
    private Context context;
    private boolean inEmergency = false;

    private boolean locationAccessPermitted = false;
    private boolean notificationsEnabled = false;

    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    public void onViewCreated(@NotNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        MainActivity.checkFineLocationPermission(requireActivity());
        this.view = view;
        this.context = getContext();
        mRepository = UserRepository.getInstance();
        mRepository.refreshData(getContext());
        userStatusRepository = UserStatusRepository.getInstance();
        SharedPreferences sharedPreferences = x_EcoPreferences.GetSharedPreferences(view.getContext());
        notificationsEnabled = sharedPreferences.getBoolean("bypassDND", false);
        locationAccessPermitted = PermissionUtils.checkPermission(view.getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        userStatusRepository.updateUserStatus(locationAccessPermitted, notificationsEnabled);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        navController = Navigation.findNavController(view);
        Timber.d("Activity: %s", getActivity());
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() !=null)
                ((MainActivity) getActivity()).getNavigationDrawer().openDrawer();
        });
        Button button = view.findViewById(R.id.btn_sos);
        button.setOnClickListener(v -> {
            if (!PermissionUtils.checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)){
                inEmergency = true;
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
            } else {
                navController.navigate(R.id.action_mainFragment_to_emergencyFragment);
            }

        });

        setUpUI();
        if (ParseUser.getCurrentUser()!=null) {
            ParseUser.getCurrentUser().fetchInBackground((object, e) -> {
                if (e == null) {
                    setUpUI();
                }
            });
        }
    }

    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 9;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Timber.d("Gained permission");
                if (inEmergency) {
                    inEmergency = false;
                    Timber.d("Navigating..");
                    navController.navigate(R.id.action_mainFragment_to_emergencyFragment);
                }
            }
        }
    }

    private void setUpUI(){
        View layoutNotSignedIn = view.findViewById(R.id.layoutNotSignedIn);
        View layoutSignedIn = view.findViewById(R.id.layoutSignedIn);
        TextView statusTextView = view.findViewById(R.id.statusText);
        Switch offDutySwitch = view.findViewById(R.id.offDutySwitch);
        TextView switchTextView = view.findViewById(R.id.switchHeadline);
        Button btnSignIn = view.findViewById(R.id.signInButton);
        mRepository.isSigned().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                layoutNotSignedIn.setVisibility(View.GONE);
                layoutSignedIn.setVisibility(View.VISIBLE);
            } else {
                layoutNotSignedIn.setVisibility(View.VISIBLE);
                layoutSignedIn.setVisibility(View.GONE);
            }
        });

        userStatusRepository.getUserStatus().observe(getViewLifecycleOwner(), status -> {
            switch (status) {
                case ACTIVE:
                    statusTextView.setTextColor(ContextCompat.getColor(view.getContext(), R.color.md_green_700));
                    statusTextView.setText(view.getContext().getResources().getString(R.string.user_active));
                    break;
                case INACTIVE:
                case TEMPORARY_INACTIVE:
                    statusTextView.setTextColor(ContextCompat.getColor(view.getContext(), R.color.md_red_700));
                    statusTextView.setText(view.getContext().getResources().getString(R.string.user_inactive));
                    break;
                case NOT_REGISTERED:
                    statusTextView.setTextColor(ContextCompat.getColor(view.getContext(), R.color.md_red_700));
                    statusTextView.setText(view.getContext().getResources().getString(R.string.user_not_registered));
                    break;
            }
        });
        btnSignIn.setOnClickListener(v -> startActivity(new Intent(getContext(), LoginRegisterActivity.class)));
        offDutySwitch.setChecked(false);

        ParseUser user = ParseUser.getCurrentUser();
        if (user!=null) {
            // User is signed

            offDutySwitch.setOnClickListener(v -> {
                user.put("dutyOff", offDutySwitch.isChecked());
                user.saveInBackground();
                switchTextView.setText(getResources().getString((!offDutySwitch.isChecked()) ? R.string.disable_emergency_recipience : R.string.off_duty_activated));
                boolean activated = user.getBoolean("activated");
//                mRepository.setStatus(((activated && !offDutySwitch.isChecked()) ? UserStatus.ACTIVE : UserStatus.INACTIVE), context);
                userStatusRepository.updateUserStatus(locationAccessPermitted, notificationsEnabled);
            });

            layoutNotSignedIn.setVisibility(View.GONE);
            layoutSignedIn.setVisibility(View.VISIBLE);
            mRepository.setSigned(true);

            Date pausedUntil = user.getDate("pausedUntil");
            boolean activated = user.getBoolean("activated");
            boolean isInOffTime = !pausedUntil.before(new Date());
            boolean dutyOff = user.getBoolean("dutyOff");

            SettingsRepository settingsRepository = SettingsRepository.getInstance();
            if (activated && !isInOffTime && !dutyOff && !OffDutyUtils.isInOffDutyDays(settingsRepository.getDutyOffDays()) && !OffDutyUtils.isInOffDutyHours(settingsRepository.getDutyOffTimeFrom(), settingsRepository.getDutyOffTimeTo())) {
                mRepository.setStatus(UserStatus.ACTIVE, context);
            } else {
                mRepository.setStatus(UserStatus.INACTIVE, context);
            }

            offDutySwitch.setChecked(dutyOff);
            switchTextView.setText(view.getContext().getResources().getString((!dutyOff) ? R.string.disable_emergency_recipience : R.string.off_duty_activated));

        } else {
            // User is not signed
            mRepository.setSigned(false);
            mRepository.setStatus(UserStatus.NOT_REGISTERED, context);
            layoutNotSignedIn.setVisibility(View.VISIBLE);
            layoutSignedIn.setVisibility(View.GONE);
        }

        view.findViewById(R.id.ll_status_container).setOnClickListener(v -> {
            String str = statusTextView.getText().toString();
            if (str.equals(getResources().getString(R.string.user_inactive))) {
                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.dialog_status, null);
                MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context).customView(dialogView, false);
                MaterialDialog materialDialog = dialogBuilder.build();
                materialDialog.show();

                dialogView.findViewById(R.id.btn_neutral).setOnClickListener(v1 -> materialDialog.dismiss());
                TextView tv1 = dialogView.findViewById(R.id.tv1);
                TextView tv2 = dialogView.findViewById(R.id.tv2);
                TextView tv3 = dialogView.findViewById(R.id.tv3);
                TextView tv4 = dialogView.findViewById(R.id.tv4);

                tv1.setVisibility(offDutySwitch.isChecked() ? View.VISIBLE : View.GONE);
                if (user!=null) {
                    boolean activated = user.getBoolean("activated");
                    UserStatusRepository userStatusRepository = UserStatusRepository.getInstance();
                    if (activated) {
                        if (locationAccessPermitted && notificationsEnabled) {
                            tv2.setVisibility(View.GONE);
                        } else {
                            tv2.setVisibility(View.VISIBLE);
                        }
                    } else {
                        tv2.setVisibility(View.VISIBLE);
                    }
//                    tv2.setVisibility(!activated ? View.VISIBLE : View.GONE);
                }
                SettingsRepository settingsRepository = SettingsRepository.getInstance();
                if (OffDutyUtils.isInOffDutyHours(settingsRepository.getDutyOffTimeFrom(), settingsRepository.getDutyOffTimeTo())){
                    tv3.setVisibility(View.VISIBLE);
//                    getResources().getString(R.string.days)
                    tv3.setText(getString(R.string.sir_standby_hours, settingsRepository.getDutyOffTimeFrom(), settingsRepository.getDutyOffTimeTo()));
//                    tv3.setText("-(Standby Mode Page) You set off duty hours between " + settingsRepository.getDutyOffTimeFrom() + " and " + );
                } else {
                    tv3.setVisibility(View.GONE);
                }
                if (OffDutyUtils.isInOffDutyDays(settingsRepository.getDutyOffDays())){

                    List<Integer> daysOffInt = settingsRepository.getDutyOffDays();
                    String[] daysOffArr = getResources().getStringArray(R.array.days);
                    StringBuilder result = new StringBuilder();
                    for (int i: daysOffInt) {
                        if (result.length() != 0) {
                            result.append(", ");
                        }
                        result.append(daysOffArr[i - 1]);
                    }

                    tv4.setVisibility(View.VISIBLE);
                    tv4.setText(getString(R.string.sir_standby_days, result.toString()));
//                    tv4.setText("-(Standby Mode Page) You set off duty days in " + result.toString());
                } else {
                    tv4.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        loadOpenProtocols();
    }


    private void loadOpenProtocols() {
        ParseQuery<ParseObject> queryFinished = new ParseQuery<>("EmergencyState");
        ParseQuery<ParseObject> queryCanceled = new ParseQuery<>("EmergencyState");

        queryFinished.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        queryCanceled.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);

        queryFinished.whereEqualTo("userRelation", ParseUser.getCurrentUser());
        queryCanceled.whereEqualTo("userRelation", ParseUser.getCurrentUser());

        queryFinished.whereExists("endedAt");
        queryCanceled.whereExists("cancelledAt");

        ParseQuery<ParseObject> queryProtocol = ParseQuery.or(Arrays.asList(queryFinished, queryCanceled));
        queryProtocol.whereDoesNotExist("protocolRelation");
        ParseQuery<ParseObject> innerQuery = ParseQuery.getQuery("ControlCenter");
        innerQuery.whereEqualTo("reportRequired", true);
        queryProtocol.whereMatchesQuery("controlCenterRelation", innerQuery);
//        queryProtocol.whereEqualTo("controlCenterRelation.reportRequired", true);

//        float dip = 36f;
//        Resources r = getResources();
//        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());

        int height = (int) getResources().getDimension(R.dimen.incomplete_protocols_view);
        // TODO: Uncomment views
        LinearLayout layout = view.findViewById(R.id.openProtocols);
        ValueAnimator vaShow = ValueAnimator.ofInt(0, height);
        vaShow.setDuration(700);
        vaShow.addUpdateListener(animation -> {
            Integer value = (Integer) animation.getAnimatedValue();
            if (layout.getLayoutParams().height < value) {
                layout.getLayoutParams().height = value;
                layout.requestLayout();
            }
        });

        ValueAnimator vaHide = ValueAnimator.ofInt(height, 0);
        vaHide.setDuration(700);
        vaHide.addUpdateListener(animation -> {
            Integer value = (Integer) animation.getAnimatedValue();
            if (layout.getLayoutParams().height > value) {
                layout.getLayoutParams().height = value;
                layout.requestLayout();
            }
        });

        layout.setOnClickListener((View view) -> {
            startActivity(new Intent(getContext(), ListIncompleteProtocolsActivity.class));
        });
        queryProtocol.countInBackground((incompleteProtocolCount, e) -> {
            if (e == null) {
                if (incompleteProtocolCount > 0) {
                    TextView text = view.findViewById(R.id.protocolsToCompleteText);
                    if (getActivity() !=null)
                        ((MainActivity) getActivity()).getNavigationDrawer().setBadgeNumber(incompleteProtocolCount);
                    SharedPreferences.Editor editor = x_EcoPreferences.GetEditor(context);
                    editor.putInt("badge", incompleteProtocolCount);
                    editor.apply();
                    text.setText(view.getContext().getResources().getQuantityString(R.plurals.protocols_to_complete, incompleteProtocolCount, incompleteProtocolCount));

                    vaShow.start();
                } else {
                    if (getActivity() !=null)
                        ((MainActivity) getActivity()).getNavigationDrawer().setBadgeNumber(0);
                    SharedPreferences.Editor editor = x_EcoPreferences.GetEditor(context);
                    editor.putInt("badge", 0);
                    editor.apply();
                    vaHide.start();
                }
            } else {
                Timber.d("error getting protocols todo. %s", e.getLocalizedMessage());
            }
        });

    }

}
