package com.aurel.ecorescue;

import com.aurel.ecorescue.utils.DateUtils;
import com.aurel.ecorescue.utils.OffDutyUtils;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OffDutyUtilsUnitTest {

    private OffDutyUtils offDutyUtils;

    @Before
    public void before(){

    }

    @Test
    public void test() {

        assertEquals(700, Integer.parseInt("0700"));
        assertEquals(1430, Integer.parseInt("1430"));

        assertFalse(OffDutyUtils.isInOffDutyHours("07:00", "12:40"));
        assertTrue(OffDutyUtils.isInOffDutyHours("23:00", "16:40"));
//        assertFalse(OffDutyUtils.isInOffDutyHours("07:00", null));

//        assertTrue(OffDutyUtils.isInOffDutyDays(asList(1, 2, 3)));

    }
}
