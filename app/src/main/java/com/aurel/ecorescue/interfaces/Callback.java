package com.aurel.ecorescue.interfaces;

public interface Callback {
    void onSuccess(String id, String message);
    void onError(String id, String message);
}
