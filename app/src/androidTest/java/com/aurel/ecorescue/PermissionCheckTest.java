package com.aurel.ecorescue;

import android.content.Context;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.aurel.ecorescue.utils.EmergencyAcceptedOnTimeUtils;
import com.aurel.ecorescue.utils.PermissionUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class PermissionCheckTest {

    private Context context;

    @Before
    public void before(){
        context = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void test() {

        EmergencyAcceptedOnTimeUtils.addAcceptedEmergency("asd", context);
        EmergencyAcceptedOnTimeUtils.addAcceptedEmergency("asd2", context);
        EmergencyAcceptedOnTimeUtils.addAcceptedEmergency("asd3", context);
        EmergencyAcceptedOnTimeUtils.addAcceptedEmergency("asd4", context);
        EmergencyAcceptedOnTimeUtils.addAcceptedEmergency("qwe", context);
        EmergencyAcceptedOnTimeUtils.addAcceptedEmergency("asd2", context);

        assertEquals(EmergencyAcceptedOnTimeUtils.isEmergencyAccepted("asd6", context), true);

//        assertTrue(PermissionUtils.checkPermission(context, android.Manifest.permission.READ_CONTACTS));
//        assertTrue(PermissionUtils.checkPermission(context, android.Manifest.permission.WRITE_CONTACTS));
//        assertTrue(PermissionUtils.checkPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE));
//        assertTrue(PermissionUtils.checkPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION));
//        assertTrue(PermissionUtils.checkPermission(context, android.Manifest.permission.CAMERA));


//        assertTrue(PermissionUtils.checkPermissions(context, LoginRegisterActivity.PERMISSIONS));
//        assertTrue(LoginRegisterActivity.hasPermissions(context, LoginRegisterActivity.PERMISSIONS));

    }
}
