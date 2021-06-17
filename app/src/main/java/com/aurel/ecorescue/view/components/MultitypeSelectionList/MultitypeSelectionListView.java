package com.aurel.ecorescue.view.components.MultitypeSelectionList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.NumberPicker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MultitypeSelectionListView extends ExpandableListView {
    private int mSelectedGroup = 0;
    private Object mPrimaryValue = null;
    private Object mSecondaryValue = null;
    private Set<Object> mMultiSelectValues = null;
    private Map<Integer, Object> mItemValues = new LinkedHashMap<>();
    private Map<String, ItemTypes.ItemType> mItemComponentDefinitions = new LinkedHashMap<>();
    private boolean mMultiselectMode = false;
    private List<String> mLabels;

    public MultitypeSelectionListView(Context context) {
        super(context);
    }

    public MultitypeSelectionListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public <T> T getValue() {
        return (T) mPrimaryValue;
    }

    public <T> T getSecondaryValue() {
        return (T) mSecondaryValue;
    }

    public <T> Set<T> getValues() {
        return (Set<T>) mMultiSelectValues;
    }

    public boolean isMultiselectMode() {
        return mMultiselectMode;
    }

    public MultitypeSelectionListView addItem(String label, Object value) {
        return addItem(label, value, null);
    }

    public MultitypeSelectionListView addItem(String label, Object value, ItemTypes.ItemType itemComponentDefinition) {
        mItemValues.put(mItemValues.size(), value);
        mItemComponentDefinitions.put(label, itemComponentDefinition);
        return this;
    }

    public MultitypeSelectionListView create(int defaultGroup, boolean multiselectMode) {
        this.mLabels = new ArrayList<>(mItemComponentDefinitions.keySet());
        mMultiselectMode = multiselectMode;
        MultitypeSelectionListAdapter adapter = new MultitypeSelectionListAdapter(this, getContext(), mItemComponentDefinitions);
        this.setAdapter(adapter);
        if (!multiselectMode) {
            this.mSelectedGroup = defaultGroup;
            if (this.mSelectedGroup >= 0) {
                this.setValues(mItemValues.get(this.mSelectedGroup), null);
                this.expandGroup(this.mSelectedGroup);
            }
            this.setOnGroupExpandListener((int groupPosition) -> {
                if (groupPosition != mSelectedGroup) {
                    this.setValues(mItemValues.get(groupPosition), null);
                    this.collapseGroup(this.mSelectedGroup);
                    this.mSelectedGroup = groupPosition;
                }
            });
            this.setOnGroupClickListener((ExpandableListView parent, View v, int groupPosition, long id) -> (groupPosition == this.mSelectedGroup));

        } else if (multiselectMode) {
            mMultiSelectValues = new HashSet<>();
            if (defaultGroup >= 0) {
                this.expandGroup(defaultGroup);
                this.addMultiSelectValue(mItemValues.get(defaultGroup));
            }
            this.setOnGroupExpandListener((int groupPosition) -> {
                this.addMultiSelectValue(mItemValues.get(groupPosition));
            });
            this.setOnGroupCollapseListener((int groupPosition) -> {
                this.removeMultiSelectValue(mItemValues.get(groupPosition));
            });
        }
        return this;
    }

    public void setValue(Object primaryValue) {
        boolean changed = false;
        if ((primaryValue != null && !primaryValue.equals(mPrimaryValue)) || (primaryValue == null && mPrimaryValue != null)) {
            changed = true;
            mPrimaryValue = primaryValue;
        }

        if (changed) {
            this.expandGroup(getKey(mItemValues, primaryValue));
            if (mOnValuesChangedListener != null) {
                mOnValuesChangedListener.OnValuesChanged(this.mPrimaryValue, this.mSecondaryValue);
            }
        }
    }

    public void setValues(Object primaryValue, Object secondaryValue) {
        boolean changed = false;
        if ((primaryValue != null && !primaryValue.equals(mPrimaryValue)) || (primaryValue == null && mPrimaryValue != null)) {
            changed = true;
            mPrimaryValue = primaryValue;
        }

        if ((secondaryValue != null && !secondaryValue.equals(mSecondaryValue)) || (secondaryValue == null && mSecondaryValue != null)) {
            changed = true;
            mSecondaryValue = secondaryValue;
        }

        if (changed && mOnValuesChangedListener != null) {
            mOnValuesChangedListener.OnValuesChanged(this.mPrimaryValue, this.mSecondaryValue);
        }
    }

    public void setSecondaryValue(Object secondaryValue) {
        if ((secondaryValue != null && !secondaryValue.equals(mSecondaryValue)) || (secondaryValue == null && mSecondaryValue != null)) {
            mSecondaryValue = secondaryValue;
            if (mOnValuesChangedListener != null) {
                mOnValuesChangedListener.OnValuesChanged(this.mPrimaryValue, this.mSecondaryValue);
            }

            String label = this.mLabels.get(mSelectedGroup);
            ItemTypes.ItemType it = mItemComponentDefinitions.get(label);
            if (it instanceof ItemTypes.NumberPicker) {
                NumberPicker numberPicker = (NumberPicker) it.view;
                if (numberPicker != null) {
                    if (numberPicker.getValue() != (Integer) secondaryValue) {
                        numberPicker.setValue((Integer) secondaryValue);
                    }
                } else {
                    ((ItemTypes.NumberPicker) it).defVal = (Integer) secondaryValue;
                }
            }
        }
    }

    public void setMultiSelectValues(Set<Object> values) {

        mMultiSelectValues.clear();
        for (int i = 0; i < this.mItemComponentDefinitions.size(); ++i) {
            this.collapseGroup(i);
        }

        for (Object value : values) {
            expandGroup(getKey(mItemValues, value));
        }
        mMultiSelectValues.addAll(values);

        if (mOnMultiselectValuesChangedListener != null) {
            mOnMultiselectValuesChangedListener.OnMultiselectValuesChanged(mMultiSelectValues);
        }
    }


    public void addMultiSelectValue(Object value) {
        if (!mMultiSelectValues.contains(value)) {
            mMultiSelectValues.add(value);
            if (mOnMultiselectValuesChangedListener != null) {
                mOnMultiselectValuesChangedListener.OnMultiselectValuesChanged(mMultiSelectValues);
            }
        }
    }

    public void removeMultiSelectValue(Object value) {
        if (mMultiSelectValues.contains(value)) {
            mMultiSelectValues.remove(value);
            if (mOnMultiselectValuesChangedListener != null) {
                mOnMultiselectValuesChangedListener.OnMultiselectValuesChanged(mMultiSelectValues);
            }
        }
    }

    public interface OnMultiselectValuesChangedListener {
        void OnMultiselectValuesChanged(Set<Object> values);
    }

    private OnMultiselectValuesChangedListener mOnMultiselectValuesChangedListener;

    public void setOnMultiselectValuesChangedListener(OnMultiselectValuesChangedListener onMultiselectValuesChangedListener) {
        mOnMultiselectValuesChangedListener = onMultiselectValuesChangedListener;
    }

    public interface OnValuesChangedListener {
        void OnValuesChanged(Object primaryValue, Object secondaryValue);
    }

    private OnValuesChangedListener mOnValuesChangedListener;

    public void setOnValuesChangedListener(OnValuesChangedListener onValuesChangedListener) {
        mOnValuesChangedListener = onValuesChangedListener;
    }

    private <K, V> K getKey(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
