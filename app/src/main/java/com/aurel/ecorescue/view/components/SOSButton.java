package com.aurel.ecorescue.view.components;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import androidx.appcompat.widget.AppCompatButton;

import timber.log.Timber;

public class SOSButton extends AppCompatButton {

    public SOSButton(Context context) {
        super(context);
    }

    public SOSButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        int margin = (int) (2 * 64 * displayMetrics.density);
        int size = displayMetrics.widthPixels - margin;

        int widthSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        int heightSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        super.onMeasure(widthSpec, heightSpec);
    }
}
