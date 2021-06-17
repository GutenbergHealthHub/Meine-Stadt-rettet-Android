package com.aurel.ecorescue.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.transition.TransitionManager;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.utils.HtmlTextView;
import com.aurel.ecorescue.view.f_login_register.LoginRegisterActivity;
import com.aurel.ecorescue.x_tools.x_EcoPreferences;


public class IntroActivity extends AppCompatActivity {

    private int mCurrentPage = 1;
    private TextView mTitle;
    private HtmlTextView mContent;
    private ImageView mScheme;
    private Button mButton;
    private RelativeLayout mContainer;
    private SharedPreferences pref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        pref = x_EcoPreferences.GetSharedPreferences(this);

        mTitle = findViewById(R.id.tv_title);
        mContent = findViewById(R.id.tv_content);
        mScheme = findViewById(R.id.iv_scheme);
        mButton = findViewById(R.id.btn_close);
        mContainer = findViewById(R.id.container);

        mTitle.setText(Html.fromHtml(getString(R.string.intro_title_1)));
        mContent.setText(Html.fromHtml(getString(R.string.intro_content_1)));
        mScheme.setVisibility(View.GONE);
        mButton.setText(getString(R.string.continue_button));
    }

    public void onNextClicked(View v){

        TransitionManager.beginDelayedTransition(mContainer);
//        TransitionManager.beginDelayedTransition(mContainer, new ChangeText().setChangeBehavior(ChangeText.CHANGE_BEHAVIOR_OUT_IN));
        switch (mCurrentPage){
            case 1:
                mCurrentPage++;
                mTitle.setText(Html.fromHtml(getString(R.string.intro_title_2)));
                mContent.setText(Html.fromHtml(getString(R.string.intro_content_2)));
//                mContent.setText(getString(R.string.intro_content_2));
                mScheme.setVisibility(View.GONE);
                break;
            case 2:
                mCurrentPage++;
                mTitle.setText(Html.fromHtml(getString(R.string.intro_title_3)));
                mContent.setText(Html.fromHtml(getString(R.string.intro_content_3)));
//                mContent.setText(getString(R.string.intro_content_3));
                mScheme.setVisibility(View.VISIBLE);
                break;
            case 3:
                mCurrentPage++;
                mTitle.setText(Html.fromHtml(getString(R.string.intro_title_4)));
                mContent.setText(Html.fromHtml(getString(R.string.intro_content_4)));
//                mContent.setText((getString(R.string.intro_content_4)));
                mScheme.setVisibility(View.GONE);
                break;
            case 4:
                mCurrentPage++;
                mTitle.setText(Html.fromHtml(getString(R.string.intro_title_5)));
                mContent.setText(Html.fromHtml(getString(R.string.intro_content_5)));
//                mContent.setText(getString(R.string.intro_content_5));
                mScheme.setVisibility(View.GONE);
                break;
            case 5:
                mCurrentPage++;
                mTitle.setText(Html.fromHtml(getString(R.string.intro_title_6)));
                mContent.setText(Html.fromHtml(getString(R.string.intro_content_6)));
//                mContent.setText(getString(R.string.intro_content_6));
                mScheme.setVisibility(View.GONE);
                if (pref.getBoolean("not_first_time", false)) {
                    mButton.setText(getString(R.string.close));
                } else {
                    mButton.setText(getString(R.string.register));
                }
                break;
            default:
                if (!pref.getBoolean("not_first_time", false)) {
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean("not_first_time", true);
                    editor.apply();
                    startActivity(new Intent(this, MainActivity.class));
                    Intent intent = new Intent(this, LoginRegisterActivity.class);
                    startActivity(intent);
                }
                finish();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
