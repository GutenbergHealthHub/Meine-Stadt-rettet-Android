package com.aurel.ecorescue.ui.account.profile;

import android.Manifest;
import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;


import com.aurel.ecorescue.R;
import com.aurel.ecorescue.data.SettingsRepository;
import com.aurel.ecorescue.data.UserRepository;
import com.aurel.ecorescue.data.UserStatus;
import com.aurel.ecorescue.data.UserStatusRepository;
import com.aurel.ecorescue.model.CertificateStatus;
import com.aurel.ecorescue.profile.CertificateStatusData;
import com.aurel.ecorescue.profile.ProfileData;
import com.aurel.ecorescue.utils.AppExecutors;
import com.aurel.ecorescue.utils.OffDutyUtils;
import com.aurel.ecorescue.utils.ParseUtils;
import com.aurel.ecorescue.utils.PermissionUtils;
import com.aurel.ecorescue.view.LocationPermissionHelper;
import com.aurel.ecorescue.view.MainActivity;
import com.aurel.ecorescue.x_tools.x_EcoPreferences;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;

import timber.log.Timber;


public class ProfileViewModel extends AndroidViewModel {

    private MutableLiveData<Profile> profile = new MutableLiveData<>();
    private Profile profileValue = new Profile();
    private UserRepository userRepository = UserRepository.getInstance();
    private UserStatusRepository userStatusRepository = UserStatusRepository.getInstance();

    public ProfileViewModel(Application app){
        super(app);
        profile.setValue(profileValue);
        fillUserProfile();

    }

    private void fillUserProfile(){
        String fullName = userRepository.getNameSurname().getValue();
        if (fullName!=null) profileValue.setFullName(fullName);

        String email = userRepository.getEmail().getValue();
        if (email!=null) profileValue.setEmail(email);

        ParseUser u = ParseUser.getCurrentUser();
        profileValue.setImageUrl(ParseUtils.getUrl("profilePicture", u));

//        ProfileData profileData = new ProfileData().createFromCurrentUser();


        SharedPreferences sharedPreferences = x_EcoPreferences.GetSharedPreferences(getApplication());

//        boolean agreementSigned = profileData.getContractSigned();
//        boolean personalDataComplete = profileData.getPersonalInformationFilled();
//
//
//        boolean pinSetUp = u.getString("code")!=null;
        boolean notificationsEnabled = sharedPreferences.getBoolean("bypassDND", false);
        boolean locationAuthorized = LocationPermissionHelper.isLocationAuthorized(getApplication());

        userStatusRepository.updateUserStatus(locationAuthorized, notificationsEnabled);
        profileValue.setStatusAgreementSigned(userStatusRepository.isBasicContractSigned());
        profileValue.setStatusPersonalDataComplete(userStatusRepository.isPersonalDataFilled());

        profileValue.setStatusNotificationsOn(notificationsEnabled);
        profileValue.setStatusAccessSetUp(userStatusRepository.isPinSetUp());
        profileValue.setStatusLocationAuthorized(locationAuthorized);

        AppExecutors executors = new AppExecutors();
        executors.networkIO().execute(() -> {
            CertificateStatusData certificateStatusData = new CertificateStatusData().createFromCurrentUser();
//            boolean certificateReviewed = (certificateStatusData.getCertificateStatus().value()==2);
            profileValue.setStatusCertificate(certificateStatusData.getCertificateStatus().value());
            userRepository.refreshData(getApplication().getApplicationContext());

//            boolean activated = false;
//            if (agreementSigned && personalDataComplete && certificateReviewed && pinSetUp && notificationsEnabled && locationAuthorized) {
//                activated = true;
//            }
//            SettingsRepository settingsRepository = SettingsRepository.getInstance();
//            if (activated && !u.getBoolean("dutyOff") && !OffDutyUtils.isInOffDutyDays(settingsRepository.getDutyOffDays()) && !OffDutyUtils.isInOffDutyHours(settingsRepository.getDutyOffTimeFrom(), settingsRepository.getDutyOffTimeTo())) {
//                profileValue.setStatus(getApplication().getResources().getString(R.string.user_active));
//                userRepository.setStatus(UserStatus.ACTIVE, getApplication().getApplicationContext());
//            } else {
//                profileValue.setStatus(getApplication().getResources().getString(R.string.user_inactive));
//                userRepository.setStatus(UserStatus.INACTIVE, getApplication().getApplicationContext());
//            }
//            u.put("activated", activated);
//            u.saveInBackground();
            profile.postValue(profileValue);
        });

        profile.postValue(profileValue);
    }

    public LiveData<Profile> getProfile(){
        return profile;
    }

    public void deleteProfilePicture(){
        ParseUser u = ParseUser.getCurrentUser();
        u.put("profilePicture", JSONObject.NULL);
        u.saveInBackground();
        Profile p = profile.getValue();
        if (p!=null) {
            p.setImageUrl("");
            profile.postValue(p);
            userRepository.setProfileImageUrl("");
        }
    }

    public void uploadProfilePicture(File file){
        ParseUser u = ParseUser.getCurrentUser();
        u.put("profilePicture", new ParseFile(file));
        Profile p = profile.getValue();
        u.saveInBackground(e -> {
            if (e != null) {
                Timber.d("Error uploading image");
                if (p!=null) {
                    p.setImageUrl("");
                    profile.postValue(p);
                    userRepository.setProfileImageUrl("");
                }
            } else {
                Timber.d("Success");
                String url = ParseUtils.getUrl("profilePicture", u);
                Timber.d("URL Posting: %s", url);
                userRepository.setProfileImageUrl(url);
                if (p!=null) {
                    p.setImageUrl(url);
                    profile.postValue(p);
                }
            }
        });
    }

    public void updateStatusLocation(boolean status){
        Profile p = profile.getValue();
        if (p!=null) {
            p.setStatusLocationAuthorized(status);
            profile.postValue(p);
        }
    }

    public void onRefresh(){
        ParseUser user = ParseUser.getCurrentUser();
        user.fetchInBackground((object, e) -> {
            if (e==null) {
                fillUserProfile();
            } else {
                Timber.d("Error: %s", e.toString());
            }
        });
    }

    public LiveData<UserStatus> getUserStatus(){
        return userStatusRepository.getUserStatus();
    }


    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        public Factory(@NonNull Application application) { mApplication = application; }

        @NotNull
        @Override
        public <T extends ViewModel> T create(@NotNull Class<T> modelClass) {
            return (T) new ProfileViewModel(mApplication);
        }
    }
}
