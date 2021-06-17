package com.aurel.ecorescue;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.aurel.ecorescue.data.SettingsRepository;
import com.aurel.ecorescue.utils.SoundUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class SoundUtilsTest {

    SoundUtils soundUtils;

    @Before
    public void before(){
        soundUtils = new SoundUtils(InstrumentationRegistry.getTargetContext());
    }

    @Test
    public void test() {

//        assertEquals(1, soundUtils.);
        SettingsRepository settingsRepository = SettingsRepository.getInstance();
        assertEquals("alarm6", settingsRepository.getAlarmTone());
    }
}
