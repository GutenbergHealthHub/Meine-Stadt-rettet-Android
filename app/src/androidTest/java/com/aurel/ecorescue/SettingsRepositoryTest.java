package com.aurel.ecorescue;

import androidx.test.runner.AndroidJUnit4;

import com.aurel.ecorescue.data.SettingsRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class SettingsRepositoryTest {

    SettingsRepository settingsRepository;

    @Before
    public void before(){
        settingsRepository = SettingsRepository.getInstance();
    }

    @Test
    public void test() {

        assertEquals("alarm6", settingsRepository.getAlarmTone());
    }
}