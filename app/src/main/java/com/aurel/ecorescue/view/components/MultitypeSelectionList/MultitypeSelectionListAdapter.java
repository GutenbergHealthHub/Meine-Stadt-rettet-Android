package com.aurel.ecorescue.view.components.MultitypeSelectionList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.NumberPicker;
import android.widget.TextView;
import com.aurel.ecorescue.R;
import com.google.android.gms.common.internal.Asserts;

public class MultitypeSelectionListAdapter extends BaseExpandableListAdapter {

    private MultitypeSelectionListView mParent;
    private Context mContext;
    private List<String> mLabels;
    private Map<String, ItemTypes.ItemType> mItemComponentDefinitions;
    private Map<Integer, View> mViews;
    private static Map<Class<? extends ItemTypes.ItemType>, Integer> sItemTypeIdMap;
    static {
        sItemTypeIdMap = new HashMap<>();
        sItemTypeIdMap.put(ItemTypes.NumberPicker.class, R.id.MSLItemNumberPicker);
    }

    public MultitypeSelectionListAdapter(MultitypeSelectionListView parent, Context context, Map<String, ItemTypes.ItemType> itemComponentDefinitions) {
        mParent = parent;
        this.mContext = context;
        this.mItemComponentDefinitions = itemComponentDefinitions;
        this.mLabels= new ArrayList<>(itemComponentDefinitions.keySet());
        this.mViews = new HashMap<Integer, View>();
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return "unknown";/*this.mExpandableListDetail.get(this.mLabels.get(listPosition))
                .get(expandedListPosition);*/
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        ItemTypes.ItemType itemDefinition = mItemComponentDefinitions.get(mLabels.get(listPosition));
        Asserts.checkNotNull(itemDefinition);

        // final String expandedListText = (String) getChild(listPosition, expandedListPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            Asserts.checkState(sItemTypeIdMap.containsKey(itemDefinition.getClass()));

            Class<? extends ItemTypes.ItemType> itemClass = itemDefinition.getClass();
            int id = sItemTypeIdMap.get(itemClass);
            convertView = layoutInflater.inflate(R.layout.component_multiselectionlist_item, null).findViewById(id);
            itemDefinition.view = convertView;
            if(itemClass == ItemTypes.NumberPicker.class) {
                ItemTypes.NumberPicker definition = (ItemTypes.NumberPicker) itemDefinition;
                NumberPicker view = (NumberPicker) convertView;
                view.setMinValue(definition.min);
                view.setMaxValue(definition.max);
                view.setValue(definition.defVal);
                view.setWrapSelectorWheel(false);
                view.setOnValueChangedListener((NumberPicker picker, int oldVal, int newVal) -> {
                    definition.defVal = newVal;
                    this.onComponentUpdate(mViews.get(listPosition), itemDefinition);
                });
            }
        }
        itemDefinition.view = convertView;
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        Asserts.checkState(this.mLabels.size() > listPosition);
        return this.mItemComponentDefinitions.get(this.mLabels.get(listPosition)) != null ? 1: 0;
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.mLabels.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.mLabels.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.component_multiselectionlist_group, null);
        }
        TextView groupTextView = (TextView) convertView.findViewById(R.id.groupText);
        TextView groupValueTextView = (TextView) convertView.findViewById(R.id.groupValue);

        ItemTypes.ItemType itemDefinition = mItemComponentDefinitions.get(mLabels.get(listPosition));

        if(isExpanded) {
            groupTextView.setTypeface(null, Typeface.BOLD);
            groupTextView.setTextColor(ContextCompat.getColor(this.mContext, R.color.colorRed));
            onComponentUpdate(convertView, itemDefinition);
        } else {
            groupTextView.setTypeface(null, Typeface.NORMAL);
            groupTextView.setTextColor(ContextCompat.getColor(this.mContext, R.color.colorBlack));
            groupValueTextView.setText("");
        }
        groupTextView.setText(listTitle + ((itemDefinition != null) ? "..." : ""));
        mViews.put(listPosition, convertView);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }

    public void onComponentUpdate(View GroupView, ItemTypes.ItemType itemDefinition) {
        Object value = null;
        TextView groupValueTextView = GroupView.findViewById(R.id.groupValue);

        if(GroupView != null && itemDefinition != null && itemDefinition.view != null) {
            if (itemDefinition.view.getClass() == NumberPicker.class) {
                value = ((NumberPicker) itemDefinition.view).getValue();
            }
            if(groupValueTextView != null) {
                groupValueTextView.setText(String.format(itemDefinition .valueLabel, value));
            }

            mParent.setSecondaryValue(value);
        } else if(itemDefinition == null && groupValueTextView != null) {
            groupValueTextView.setText("");
        }
    }
}