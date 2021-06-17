package com.aurel.ecorescue;

import android.text.TextUtils;

import com.aurel.ecorescue.utils.EmergencyAcceptedOnTimeUtils;
import com.aurel.ecorescue.utils.ParseUtils;
import com.aurel.ecorescue.utils.SoundUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
//        assertEquals(4, 2 + 2);
//
//        ParseObject parseObject = ParseObject.create("Test");
//
//        parseObject.put("int", 5);
//
//
//        assertEquals("", ParseUtils.getString(null));


        List<String> list = new ArrayList<>();
        list.add("Item 1");
        list.add("Item 2");
        StringBuilder toSave = new StringBuilder();
        for (int i=0; i<list.size(); i++){
            toSave.append(list.get(i)).append("#");
        }
        String res = toSave.toString();
        String[] ids = res.split("#");
        assertEquals(ids.length, 2);
        assertEquals(ids[0], "Item 1");
        assertEquals(ids[1], "Item 2");
        assertEquals(toSave.toString(), "asd");



    }
}