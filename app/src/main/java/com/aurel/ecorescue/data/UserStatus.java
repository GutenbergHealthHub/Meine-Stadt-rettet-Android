package com.aurel.ecorescue.data;

public enum UserStatus {
    ACTIVE,                 // account activated and ready to receive emergency
    TEMPORARY_INACTIVE,     // account activated but currently not ready to receive emergency
    INACTIVE,               // account created but not activated
    NOT_REGISTERED          // no account
}
