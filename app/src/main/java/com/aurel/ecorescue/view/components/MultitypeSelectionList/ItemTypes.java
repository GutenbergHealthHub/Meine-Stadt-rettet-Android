package com.aurel.ecorescue.view.components.MultitypeSelectionList;

import android.view.View;

public class ItemTypes {

    public static abstract class ItemType {
        public String valueLabel;
        public View view;
    }

    public static class NumberPicker extends ItemType {
        public Integer min;
        public Integer max;
        public Integer defVal;

        public NumberPicker(String valueLabel, Integer min, Integer max, Integer defVal) {
            this.valueLabel = valueLabel;
            this.min = min;
            this.max = max;
            this.defVal= defVal;
        }
    }
}
