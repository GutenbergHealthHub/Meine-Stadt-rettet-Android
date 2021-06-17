package com.aurel.ecorescue.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Html;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.aurel.ecorescue.R;

public class HtmlTextView extends androidx.appcompat.widget.AppCompatTextView {


    public HtmlTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HtmlTextView, 0, 0);
        String text = a.getString(R.styleable.HtmlTextView_android_text);
        if (text != null) {
            setText(Html.fromHtml(text));
        }
        a.recycle();
    }
}
