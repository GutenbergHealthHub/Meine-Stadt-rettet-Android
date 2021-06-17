package com.aurel.ecorescue;

import com.aurel.ecorescue.utils.SoundUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SoundUtilsUnitTest {

    @Test
    public void test() {

        assertEquals("Horn", SoundUtils.convertToReadableSoundName("horn"));
        assertEquals("Alarm 8", SoundUtils.convertToReadableSoundName("alarm8"));
        assertNull(SoundUtils.convertToReadableSoundName("alarm9"));


        assertEquals("alarm6", SoundUtils.convertToServerSoundName("Alarm 6"));
        assertEquals("alarm9", SoundUtils.convertToServerSoundName("Alarm 9"));


    }
}
