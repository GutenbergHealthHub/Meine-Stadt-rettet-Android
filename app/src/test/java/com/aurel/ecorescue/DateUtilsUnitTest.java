package com.aurel.ecorescue;

import com.aurel.ecorescue.utils.DateUtils;
import com.aurel.ecorescue.utils.SoundUtils;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DateUtilsUnitTest {

    private DateUtils dateUtils;

    @Before
    public void before(){
        dateUtils = new DateUtils();
    }

    @Test
    public void test() {
        Date date = new Date();
        assertEquals("", dateUtils.getReadableDate(null));
        assertEquals("21.07.2019", dateUtils.getReadableDate(date));


    }
}
