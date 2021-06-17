package com.aurel.ecorescue.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aurel.ecorescue.R;
import com.bumptech.glide.Glide;

public class BlueDialogUtils {

    private BlueDialogListener mListener;
    private View view;
    private Context context;
    private TextView mContentText;
    private Button mPositiveButton, mNegativeButton, mNeutralButton;
    private MaterialDialog.Builder mDialog;

    public BlueDialogUtils(Context context, BlueDialogListener listener, boolean cancelable) {
        this.context = context;
        this.mListener = listener;
        LayoutInflater inflater = LayoutInflater.from(context);
        this.view = inflater.inflate(R.layout.dialog_basic, null);
        mContentText = view.findViewById(R.id.tv_content);
        mPositiveButton = view.findViewById(R.id.btn_positive);
        mNegativeButton = view.findViewById(R.id.btn_negative);
        mNeutralButton = view.findViewById(R.id.btn_neutral);
        mNegativeButton.setVisibility(View.GONE);
        mNeutralButton.setVisibility(View.GONE);

        mDialog = new MaterialDialog.Builder(context).cancelable(cancelable).customView(view, false);
    }



    public void setContentText(String s){
        if (s!=null) mContentText.setText(s);
    }

    public void setContentText(int id){
        mContentText.setText(mContentText.getContext().getResources().getString(id));
    }

    public void setPositiveText(String s){
        if (s!=null) {
            mPositiveButton.setText(s);
            mPositiveButton.setVisibility(View.VISIBLE);
        }
    }

    public void setNegativeText(String s){
        if (s!=null) {
            mNegativeButton.setText(s);
            mNegativeButton.setVisibility(View.VISIBLE);
        }
    }

    public void setNeutralText(String s){
        if (s!=null) {
            mNeutralButton.setText(s);
            mNeutralButton.setVisibility(View.VISIBLE);
        }
    }


    public void show(){
        MaterialDialog materialDialog = mDialog.build();
        mPositiveButton.setOnClickListener(v -> {
            mListener.onPositiveClick();
            materialDialog.dismiss();

        });
        mNegativeButton.setOnClickListener(v -> {
            mListener.onNegativeClick();
            materialDialog.dismiss();
        });
        mNeutralButton.setOnClickListener(v -> {
            mListener.onNeutralClick();
            materialDialog.dismiss();
        });
        materialDialog.show();
    }


}

