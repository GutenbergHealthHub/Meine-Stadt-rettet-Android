package com.aurel.ecorescue.utils;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.aurel.ecorescue.R;

public class StyleUtils {

    public static Drawable getHomeAsUpIndicator(Context context){
        TypedArray a = context.getTheme().obtainStyledAttributes(R.style.AppTheme, new int[] {R.attr.homeAsUpIndicator});
        int attributeResourceId = a.getResourceId(0, 0);
        Drawable drawable = context.getResources().getDrawable(attributeResourceId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable.setTint(context.getResources().getColor(R.color.icons));
        }
        return drawable;
    }

    public static Drawable getCircularProgressDrawable(Context context){
        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(30f);
        circularProgressDrawable.start();
        return circularProgressDrawable;
    }
}
