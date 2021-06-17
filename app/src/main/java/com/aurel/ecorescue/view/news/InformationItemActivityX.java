package com.aurel.ecorescue.view.news;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.enums.InformationItemType;
import com.aurel.ecorescue.interfaces.OnInformationItemsLoadedListener;
import com.aurel.ecorescue.model.InformationItem;
import com.aurel.ecorescue.service.InformationItemParser;
import com.aurel.ecorescue.view.ImageDetailActivity;
import com.aurel.ecorescue.view.x_emergency.EmergencyAppCompatActivity;
import com.aurel.ecorescue.x_tools.x_DateFormatter;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class InformationItemActivityX extends EmergencyAppCompatActivity implements OnInformationItemsLoadedListener {

    private InformationItem item;
    String email_string, phone_string;
    TextView title, subTitle, timestamp, location, email, phone, content;
    ImageView image, imagePlaceholder;
    LinearLayout eventDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.x_activity_informationitem);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        LoadItem();
        RelativeLayout imageViewPlaceholder = (RelativeLayout) findViewById(R.id.image_placeholder_container);
        final Context ctx = this;
        final Intent getIntent = getIntent();
        imageViewPlaceholder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ctx, ImageDetailActivity.class);
                intent.putExtra("id", getIntent.getStringExtra("id"));
                intent.putExtra("type", getIntent.getSerializableExtra("type"));
                ctx.startActivity(intent);
            }
        });
        title = (TextView) findViewById(R.id.title);
        subTitle = (TextView) findViewById(R.id.subtitle);
        timestamp = (TextView) findViewById(R.id.timestamp);
        location = (TextView) findViewById(R.id.location_text);
        email = (TextView) findViewById(R.id.email);
        phone = (TextView) findViewById(R.id.phone);
        content = (TextView) findViewById(R.id.text);
        image = (ImageView) findViewById(R.id.image);
        imagePlaceholder = (ImageView) findViewById(R.id.image_placeholder);


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (item != null && item.Url != null) {
            menu.add(R.string.continue_reading).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void LoadItem() {
        String id = this.getIntent().getStringExtra("id");
        InformationItemType type = (InformationItemType) this.getIntent().getSerializableExtra("type");
        if (type == InformationItemType.NEWS) {
            eventDetails = (LinearLayout) findViewById(R.id.event_info);
            eventDetails.setVisibility(View.GONE);
        }
        Log.d("EcoRescue", "news id=" + id + "type " + type);
        if (id == null) {
            Log.d("EcoRescue", "error. no valid information item id");
            finish();
        }
        InformationItemParser parser = new InformationItemParser(this);
        parser.GetItemFromLocalDatastore(type, id);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (this.item.Url == null) {
            return super.onOptionsItemSelected(item);
        }
        if (!this.item.Url.contains("http")) {
            this.item.Url = "http://" + this.item.Url;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(this.item.Url));
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void informationItemsLoaded(InformationItemType type, ArrayList<InformationItem> informationItemList) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void itemLoadedFromCache(InformationItemType type, InformationItem item) {
        Log.d("EcoRescue", "retrieved information item from cache.");
        this.item = item;
        supportInvalidateOptionsMenu();

        title.setText(item.title);
        subTitle.setText(item.description);
        String street, zip, city, organizer;
        street = item.Street;
        city = item.City;
        zip = item.Zip;
        organizer = item.Organizer;
        email_string = item.Email;
        phone_string = item.Phone;

        if (organizer == null)
            organizer = "";
        if (street == null)
            street = "";
        if (city == null)
            city = "";
        if (zip == null)
            zip = "";
        if (email_string == null)
            email.setVisibility(View.GONE);
        if (phone_string == null)
            phone.setVisibility(View.GONE);

        location.setText(organizer + "\n" + street + ", " + city + " " + zip);
        email.setText(email_string);
        phone.setText(phone_string);

        if (item.description == null || item.description.length() == 0) {
            subTitle.setVisibility(View.GONE);
            findViewById(R.id.divider1).setVisibility(View.GONE);
        }
        if (item.From != null && item.To != null) {
            timestamp.setText("Von " + x_DateFormatter.GetStandardDateFormatter().format(item.From) + " bis " + x_DateFormatter.GetStandardDateFormatter().format(item.To));
        } else {
            timestamp.setVisibility(View.GONE);
        }


        content.setText(item.Text);
        if (item.Image != null) {
            image.setVisibility(View.VISIBLE);
            imagePlaceholder.setVisibility(View.INVISIBLE);
            Glide.with(this).load(item.Image).into(image);
            Glide.with(this).load(item.Image).into(imagePlaceholder);
        } else {
            image.setVisibility(View.GONE);
            imagePlaceholder.setVisibility(View.GONE);

        }
    }


    public void sendEmail(View view) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + email_string));
        try {
            startActivity(emailIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
        }
    }


    public void makePhoneCall(View view) {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone_string, null)));

    }

}
