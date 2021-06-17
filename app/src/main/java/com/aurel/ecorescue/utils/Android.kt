package com.aurel.ecorescue.utils

import android.os.Build

object Android {

    @JvmStatic
    fun isAndroid6AndAbove() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    @JvmStatic
    fun isAndroid10() = Build.VERSION.SDK_INT == Build.VERSION_CODES.Q

    @JvmStatic
    fun isAndroid10AndAbove() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    @JvmStatic
    fun isAndroid11AndAbove() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

}