package com.aurel.ecorescue.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aurel.ecorescue.utils.AppExecutors;
import com.aurel.ecorescue.utils.OffDutyUtils;
import com.aurel.ecorescue.utils.ParseUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import timber.log.Timber;

public class UserStatusRepository {

    private static UserStatusRepository sInstance;

    public static UserStatusRepository getInstance() {
        if (sInstance == null) {
            synchronized (UserStatusRepository.class) {
                if (sInstance == null) {
                    sInstance = new UserStatusRepository();
                }
            }
        }
        return sInstance;
    }

    private MutableLiveData<UserStatus> userStatus = new MutableLiveData<>();

    private boolean locationAccessPermitted = false;
    private boolean notificationsEnabled = false;

    private UserStatusRepository(){
        userStatus.setValue(UserStatus.INACTIVE);
    }

    public void updateUserStatus(){
        updateUserStatus(locationAccessPermitted, notificationsEnabled);
    }

    public void updateUserStatus(boolean locationAccessPermitted, boolean notificationsEnabled){
        this.locationAccessPermitted = locationAccessPermitted;
        this.notificationsEnabled = notificationsEnabled;
        ParseUser user = ParseUser.getCurrentUser();
        if (user == null) {
            userStatus.postValue(UserStatus.NOT_REGISTERED);
        } else {
            AppExecutors appExecutors = new AppExecutors();
            appExecutors.networkIO().execute(() -> {
                boolean pinSetUp = isPinSetUp();
                boolean certificateReviewed = isCertificateReviewed();
                boolean basicContractSigned = isBasicContractSigned();
                boolean personalDataFilled = isPersonalDataFilled();

                SettingsRepository settingsRepository = SettingsRepository.getInstance();
                boolean notInDutyOff = isNotInOffDuty();
                boolean notInOffDutyDays = !OffDutyUtils.isInOffDutyDays(settingsRepository.getDutyOffDays());
                boolean notInOffDutyHours = !OffDutyUtils.isInOffDutyHours(settingsRepository.getDutyOffTimeFrom(), settingsRepository.getDutyOffTimeTo());

                Timber.d("Status: pin=%s, certificate=%s, agreement=%s, personal=%s, offduty=%s, dutydays=%s, dutyhours=%s",
                        pinSetUp, certificateReviewed, basicContractSigned, personalDataFilled, notInDutyOff, notInOffDutyDays, notInOffDutyHours);
                if (pinSetUp && certificateReviewed && basicContractSigned && personalDataFilled) {
                    if (locationAccessPermitted && notificationsEnabled && notInDutyOff && notInOffDutyDays && notInOffDutyHours) {
                        userStatus.postValue(UserStatus.ACTIVE);
                    } else {
                        userStatus.postValue(UserStatus.TEMPORARY_INACTIVE);
                    }
                    user.put("activated", true);
                    user.saveInBackground();
                } else {
                    userStatus.postValue(UserStatus.INACTIVE);
                    user.put("activated", false);
                    user.saveInBackground();
                }
            });
        }
    }

    public boolean isPinSetUp(){
        ParseUser user = ParseUser.getCurrentUser();
        if (user == null) return false;
        return user.getString("code") != null;
    }

    public boolean isCertificateReviewed(){
        ParseUser user = ParseUser.getCurrentUser();
        if (user == null) return false;
        ParseObject certificate = user.getParseObject("certificateFR");
        try {
            if (certificate != null) {
                certificate.fetch();
                return certificate.getInt("state") == 2;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isBasicContractSigned(){
        ParseUser user = ParseUser.getCurrentUser();
        if (user == null) return false;
        return user.getParseObject("userContractBasic") != null;
    }

    public boolean isPersonalDataFilled(){
        ParseUser user = ParseUser.getCurrentUser();
        if (user == null) return false;
        return !ParseUtils.getString("firstname", user).isEmpty() &&
                !ParseUtils.getString("lastname", user).isEmpty() &&
                !ParseUtils.getString("qualification", user).isEmpty();
    }

    public boolean isNotInOffDuty(){
        ParseUser user = ParseUser.getCurrentUser();
        if (user == null) return false;
        return !user.getBoolean("dutyOff");
    }


    public LiveData<UserStatus> getUserStatus(){
        return userStatus;
    }

}
