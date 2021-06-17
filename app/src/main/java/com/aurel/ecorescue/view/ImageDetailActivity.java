package com.aurel.ecorescue.view;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.enums.InformationItemType;
import com.aurel.ecorescue.interfaces.OnInformationItemsLoadedListener;
import com.aurel.ecorescue.model.InformationItem;
import com.aurel.ecorescue.service.InformationItemParser;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;

/**
 * Created by daniel on 6/14/17.
 */

public class ImageDetailActivity extends AppCompatActivity implements OnInformationItemsLoadedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagedetail);
        LoadItem();

    }

    private void LoadItem() {
        String id = this.getIntent().getStringExtra("id");
        InformationItemType type = (InformationItemType) this.getIntent().getSerializableExtra("type");
        Log.d("EcoRescue", "news id=" + id + "type " + type);
        if (id == null) {
            Log.d("EcoRescue", "error. no valid information item id");
            finish();
        }
        InformationItemParser parser = new InformationItemParser(this);
        parser.GetItemFromLocalDatastore(type, id);
    }

    @Override
    public void informationItemsLoaded(InformationItemType type, ArrayList<InformationItem> informationItemList) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void itemLoadedFromCache(InformationItemType type, InformationItem item) {
        Log.d("EcoRescue", "retrieved information item from cache.");
        PhotoView photoView = (PhotoView) findViewById(R.id.photo_view);
        Glide.with(this).load(item.Image).into(photoView);

    }
}
