package com.aurel.ecorescue.ui.account.profile;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.data.UserRepository;
import com.aurel.ecorescue.data.UserStatusRepository;
import com.aurel.ecorescue.databinding.FragmentProfileBinding;
import com.aurel.ecorescue.profile.steps.AgreementActivity;
import com.aurel.ecorescue.utils.BlueDialogListener;
import com.aurel.ecorescue.utils.BlueDialogUtils;
import com.aurel.ecorescue.utils.NavigationDrawer;
import com.aurel.ecorescue.utils.PermissionUtils;
import com.aurel.ecorescue.view.MainActivity;
import com.aurel.ecorescue.x_tools.x_EcoPreferences;
import com.bumptech.glide.Glide;
import com.parse.ParseUser;

import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import timber.log.Timber;


public class ProfileFragment extends Fragment {

    public ProfileFragment() {}

    private NavigationDrawer mDrawerHelper;
    private NavController navController;
    private Context context;
    private View view;

    private ProfileViewModel mViewModel;
    private FragmentProfileBinding mBinding;
    private ImageView userImage;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ProfileViewModel.Factory factory = new ProfileViewModel.Factory(requireActivity().getApplication());
        mViewModel = new ViewModelProvider(this, factory).get(ProfileViewModel.class);
        mBinding.setViewmodel(mViewModel);
        mViewModel.onRefresh();
    }

    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false);
        mBinding.setLifecycleOwner(this);
        return mBinding.getRoot();
    }

    public void onViewCreated(@NotNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        this.context = getContext();
        this.view = view;
        hideSoftKeyboard();
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        navController = Navigation.findNavController(view);
        mDrawerHelper = ((MainActivity) requireActivity()).getNavigationDrawer();
        toolbar.setNavigationOnClickListener(v -> {
            mDrawerHelper.openDrawer();
        });

        SharedPreferences sharedPreferences = x_EcoPreferences.GetSharedPreferences(view.getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        ParseUser u = ParseUser.getCurrentUser();
        boolean pinSetUp = u.getString("code")!=null;
        if (pinSetUp) {
            editor.putInt("access_pin", 2);
            editor.apply();
        } else {
            editor.putInt("access_pin", 0);
            editor.apply();
        }

        setUpView();
        UserRepository userRepository = UserRepository.getInstance();
        userRepository.refreshData(view.getContext().getApplicationContext());
    }



    private void setUpView(){

        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (mViewModel!=null) mViewModel.onRefresh();
            swipeRefreshLayout.setRefreshing(false);
        });

        userImage = view.findViewById(R.id.iv_user_image);

        Fragment fragment = this;
        userImage.setOnClickListener(v -> {
//            new MaterialDialog.Builder(context).title(R.string.profile_photo).positiveText(R.string.change_photo).negativeText(R.string.delete_photo).neutralText(R.string.cancel).onPositive((dialog, which) -> {
//                EasyImage.openChooserWithGallery(this, "Select from gallery", 0);
//            }).onNegative((dialog, which) -> {
//                mViewModel.deleteProfilePicture();
//                userImage.setImageResource(R.drawable.logo_v2);
//            }).show();

            BlueDialogUtils blueDialog = new BlueDialogUtils(getContext(), new BlueDialogListener() {
                @Override
                public void onPositiveClick() {
//                    PermissionUtils.checkPermission(context, Manifest.permission.READ)
                    EasyImage.openChooserWithGallery(fragment, getResources().getString(R.string.select_one_of_the_options), 0);
                }

                @Override
                public void onNegativeClick() {
                    mViewModel.deleteProfilePicture();
                    userImage.setImageResource(R.drawable.logo_v2);
                }

                @Override
                public void onNeutralClick() {

                }
            }, true);
            blueDialog.setContentText(R.string.profile_photo);
            blueDialog.setPositiveText(view.getContext().getResources().getString(R.string.change_photo));
            blueDialog.setNegativeText(view.getContext().getResources().getString(R.string.delete_photo));
            blueDialog.setNeutralText(view.getContext().getResources().getString(R.string.cancel));
            blueDialog.show();
        });
        view.findViewById(R.id.cl_agreement).setOnClickListener(v -> {
//            startActivity(new Intent(context, AgreementActivity.class));
            navController.navigate(R.id.action_profileFragment_to_agreementFragment);
        });
        view.findViewById(R.id.cl_personal_data).setOnClickListener(v -> {
//            startActivity(new Intent(context, PersonalDataActivity.class));
            navController.navigate(R.id.action_profileFragment_to_personalDataFragment);
        });
        view.findViewById(R.id.cl_certificate).setOnClickListener(v -> navController.navigate(R.id.action_profileFragment_to_certificationFragment));
        view.findViewById(R.id.cl_access).setOnClickListener(v -> navController.navigate(R.id.accessFragment));
        view.findViewById(R.id.cl_notifications).setOnClickListener(v -> {
//
//            SharedPreferences sharedPreferences = x_EcoPreferences.GetSharedPreferences(context);
//            boolean bypassDND = sharedPreferences.getBoolean("bypassDND", false);
//            SharedPreferences.Editor editor = x_EcoPreferences.GetEditor(context);
//            editor.putBoolean("bypassDND", !bypassDND);
//            editor.commit();
//            mViewModel.onRefresh();
            BlueDialogUtils blueDialog = new BlueDialogUtils(getContext(), new BlueDialogListener() {
                @Override
                public void onPositiveClick() {
                    enableNotifications();
                }

                @Override
                public void onNegativeClick() {
                    disableNotifications();
                }

                @Override
                public void onNeutralClick() {

                }
            }, true);
            blueDialog.setContentText(R.string.notifications_dialog_info);
            blueDialog.setPositiveText(view.getContext().getResources().getString(R.string.enable));
            blueDialog.setNegativeText(view.getContext().getResources().getString(R.string.disable));
            blueDialog.show();
        });
        view.findViewById(R.id.cl_location).setOnClickListener(v -> {
            BlueDialogUtils blueDialog = new BlueDialogUtils(getContext(), new BlueDialogListener() {
                @Override public void onPositiveClick() {
                    MainActivity.checkFineLocationPermission(requireActivity());
                }
                @Override public void onNegativeClick() {}
                @Override public void onNeutralClick() {}
            }, true);
            blueDialog.setContentText(R.string.location_permission_dialog);
            blueDialog.setPositiveText(view.getContext().getResources().getString(R.string.ok));
            blueDialog.show();
            if (!PermissionUtils.checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
            }
        });
//        UserStatusRepository userStatusRepository = UserStatusRepository.getInstance();
//        userStatusRepository.updateUser();
        if (mViewModel!=null) {
            mViewModel.onRefresh();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        EasyImage.handleActivityResult(requestCode, resultCode, data, getActivity(), new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {

            }

            @Override
            public void onImagePicked(File file, EasyImage.ImageSource imageSource, int i) {
                Glide.with(view.getContext())
                        .load(file)
                        .centerCrop()
                        .placeholder(R.drawable.logo_v2)
                        .into(userImage);
                mViewModel.uploadProfilePicture(file);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        if (mViewModel!=null) mViewModel.onRefresh();
    }

    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 9;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mViewModel.updateStatusLocation(PermissionUtils.checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION));
            }
        }
    }

    private void enableNotifications(){
        SharedPreferences.Editor editor = x_EcoPreferences.GetEditor(context);
        editor.putBoolean("bypassDND", true);
//        editor.commit();
        editor.apply();
        mViewModel.onRefresh();
    }

    private void openNotificationSettings(){
        try {
            context.startActivity(new Intent("android.settings.NOTIFICATION_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName()));
        } catch (Exception e) {
            Timber.e(e, "NotificationIntentError");
        }

    }

    private void disableNotifications(){
        SharedPreferences.Editor editor = x_EcoPreferences.GetEditor(context);
        editor.putBoolean("bypassDND", false);
//        editor.commit();
        editor.apply();
        mViewModel.onRefresh();
    }

    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
