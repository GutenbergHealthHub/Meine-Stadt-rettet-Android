package com.aurel.ecorescue.data;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.enums.InformationItemType;
import com.aurel.ecorescue.interfaces.Callback;
import com.aurel.ecorescue.model.Course;
import com.aurel.ecorescue.model.Events;
import com.aurel.ecorescue.model.News;
import com.aurel.ecorescue.utils.OffDutyUtils;
import com.aurel.ecorescue.utils.ParseUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

public class UserRepository {

    private static UserRepository sInstance;

    public static UserRepository getInstance() {
        if (sInstance == null) {
            synchronized (UserRepository.class) {
                if (sInstance == null) {
                    sInstance = new UserRepository();
                }
            }
        }
        return sInstance;
    }


    private MutableLiveData<String> nameSurname = new MutableLiveData<>();
    private MutableLiveData<String> email = new MutableLiveData<>();
    private MutableLiveData<String> status = new MutableLiveData<>();
    private MutableLiveData<String> profileImageUrl = new MutableLiveData<>();
    private MutableLiveData<Boolean> isSigned = new MutableLiveData<>();

    private UserRepository(){
        nameSurname.setValue("");
        email.setValue("");
        status.setValue("");
        profileImageUrl.setValue("");
        isSigned.setValue(false);
    }

    public void reset(){
        nameSurname.setValue("");
        email.setValue("");
        status.setValue("");
        profileImageUrl.setValue("");
        isSigned.setValue(false);
    }


    public void refreshData(Context context){
        ParseUser user = ParseUser.getCurrentUser();
        if (user != null) {
            try {
                user.fetchIfNeeded();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            isSigned.postValue(true);
            String firstName = ParseUtils.getString("firstname", user);
            String lastName = ParseUtils.getString("lastname", user);
            String nameString = firstName + " " + lastName;
            nameSurname.postValue(nameString);
            email.postValue(ParseUtils.getString(user.getEmail()));
            profileImageUrl.postValue(ParseUtils.getUrl("profilePicture", user));
            boolean isActivated = user.getBoolean("activated");
            boolean dutyOff = user.getBoolean("dutyOff");
            if (isActivated && !dutyOff) {
                status.postValue(context.getString(R.string.user_active));
            } else {
                status.postValue(context.getString(R.string.user_inactive));
            }
            updateStatus(context);
        } else {
            nameSurname.postValue(context.getString(R.string.app_name));
            email.postValue("");
            status.postValue(context.getString(R.string.user_not_registered));
            profileImageUrl.postValue("");
            isSigned.postValue(false);
        }
    }

    public LiveData<String> getNameSurname(){
        return nameSurname;
    }

    public LiveData<String> getEmail(){
        return email;
    }

    public LiveData<String> getStatus(){
        return status;
    }

    public LiveData<String> getProfileImageUrl(){
        return profileImageUrl;
    }

    public LiveData<Boolean> isSigned() {
        return isSigned;
    }

    public void setNameSurname(String nameSurname) {
        this.nameSurname.postValue(nameSurname);
    }

    public void setEmail(String email) {
        this.email.postValue(email);
    }

    public void setProfileImageUrl(String url) {
        this.profileImageUrl.postValue(url);
    }

    public void setStatus(UserStatus status, Context context) {
        switch (status) {
            case ACTIVE:
                this.status.postValue(context.getString(R.string.user_active));
                break;
            case INACTIVE:
                this.status.postValue(context.getString(R.string.user_inactive));
                break;
            case NOT_REGISTERED:
                this.status.postValue(context.getString(R.string.user_not_registered));
                this.nameSurname.postValue(context.getString(R.string.app_name));
                this.email.postValue("");
                this.profileImageUrl.postValue("");
                setSigned(false);
                break;
        }
    }

    public void setSigned(Boolean isSigned) {
        this.isSigned.postValue(isSigned);
    }

    public void updateStatus(Context context){
        // If in offdutyHours
        ParseUser user = ParseUser.getCurrentUser();
        boolean isActivated = user.getBoolean("activated");
        boolean dutyOff = user.getBoolean("dutyOff");

        SettingsRepository settingsRepository = SettingsRepository.getInstance();
        if (OffDutyUtils.isInOffDutyHours(settingsRepository.getDutyOffTimeFrom(), settingsRepository.getDutyOffTimeTo()) || OffDutyUtils.isInOffDutyDays(settingsRepository.getDutyOffDays())) {
            this.status.postValue(context.getString(R.string.user_inactive));
        } else if (isActivated && !dutyOff){
            status.postValue(context.getString(R.string.user_active));
        } else {
            status.postValue(context.getString(R.string.user_inactive));
        }
    }

}
