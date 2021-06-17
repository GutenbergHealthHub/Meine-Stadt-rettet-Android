package com.aurel.ecorescue.data;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aurel.ecorescue.interfaces.Callback;
import com.aurel.ecorescue.model.Certificate;
import com.aurel.ecorescue.utils.AppExecutors;
import com.aurel.ecorescue.utils.ParseUtils;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class CertificatesRepository {

    private static CertificatesRepository sInstance;

    public static CertificatesRepository getInstance() {
        if (sInstance == null) {
            synchronized (CertificatesRepository.class) {
                if (sInstance == null) {
                    sInstance = new CertificatesRepository();
                }
            }
        }
        return sInstance;
    }

    private MutableLiveData<List<Certificate>> certificates = new MutableLiveData<>();

    private CertificatesRepository(){
        certificates.setValue(new ArrayList<>());
    }

    public void reset(){
        certificates.setValue(new ArrayList<>());
    }

    public LiveData<List<Certificate>> getCertificates(){
        return certificates;
    }

    public Certificate getCertificate(String id){
        List<Certificate> items = certificates.getValue();
        if (items!=null) {
            for (Certificate item: items) {
                if (id.equals(item.getObjectId())) {
                    return item;
                }
            }
        }
        return null;
    }

    public void deleteCertificate(String id){
        if (id==null || id.isEmpty()) return;
        ParseUser user = ParseUser.getCurrentUser();
        List<ParseObject> certificates_list = user.getList("certificates");
        Timber.d("Relation: %s", certificates);
        ParseObject toRemove = null;
        if (certificates!=null) {
            for (ParseObject object: certificates_list) {
                if (ParseUtils.getString(object.getObjectId()).equals(id)) {
                    toRemove = object;
                }
            }
            if (toRemove!=null) {
                certificates_list.remove(toRemove);
                user.put("certificates", certificates_list);
                user.saveInBackground();
                toRemove.deleteInBackground();
            }
        }

        List<Certificate> items = null;
        Certificate itemToRemove = null;
        if (certificates != null) {
            items = certificates.getValue();
            if (items!=null) {
                for (Certificate item: items) {
                    if (id.equals(item.getObjectId())) {
                        itemToRemove = item;
                    }
                }
                items.remove(itemToRemove);
                certificates.postValue(items);
            }
        }
    }

    public void addCertificate(File file, @NonNull Callback callback){
        ParseUser user = ParseUser.getCurrentUser();
        List<ParseObject> certificates = user.getList("certificates");
        Timber.d("Relation: %s", certificates);
        if (certificates==null) certificates = new ArrayList<>();

        ParseObject certificate = new ParseObject("Certificate");
        certificate.put("file", new ParseFile(file));
        certificate.put("state", 1);
        certificate.put("title", "Other document");
        certificate.put("type", 2);
        certificates.add(certificate);

        user.put("certificates", certificates);

        user.saveInBackground(e -> {
            if (e != null) {
                Timber.d("Error uploading image %s", e);
                callback.onError("addCertificate", "error");
            } else {
                Timber.d("Success");
                callback.onSuccess("addCertificate", certificate.getObjectId());
                loadCertificates();
            }
        });
    }


    public void loadCertificates(){
        ParseUser user = ParseUser.getCurrentUser();
        final List<ParseObject> certificates_list = user.getList("certificates");
        AppExecutors executors = new AppExecutors();
        executors.networkIO().execute(() -> {
            List<Certificate> cList = new ArrayList<>();
            if (certificates_list!=null) {
                Timber.d("List: %s", certificates_list.size());
                for (ParseObject object: certificates_list) {
                    try {
                        object.fetch();
                        Certificate certificate = new Certificate();
                        certificate.setObjectId(ParseUtils.getString(object.getObjectId()));
                        certificate.setTitle(ParseUtils.getString(object.getString("title")));
                        certificate.setState(object.getInt("state"));
                        certificate.setUrl(ParseUtils.getUrl("file", object));
                        cList.add(certificate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
            certificates.postValue(cList);
        });
    }
}
