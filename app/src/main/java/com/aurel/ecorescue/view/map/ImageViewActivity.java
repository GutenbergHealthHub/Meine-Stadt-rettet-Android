package com.aurel.ecorescue.view.map;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.aurel.ecorescue.R;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

public class ImageViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        String url = getIntent().getStringExtra("url");
        if(url==null){
            finish();
        }
        PhotoView photoView = findViewById(R.id.photo_view);
        Glide.with(photoView.getContext()).load(url).into(photoView);
    }
}
