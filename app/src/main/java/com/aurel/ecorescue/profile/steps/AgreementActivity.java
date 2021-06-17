package com.aurel.ecorescue.profile.steps;

import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.interfaces.OnContractLoadedListener;
import com.aurel.ecorescue.service.ContractParser;
import com.aurel.ecorescue.view.dialogs.InformationDialog;
import com.aurel.ecorescue.view.dialogs.LoadingDialog;
import com.aurel.ecorescue.view.x_emergency.EmergencyAppCompatActivity;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Date;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class AgreementActivity extends EmergencyAppCompatActivity implements OnContractLoadedListener {


    Toolbar toolbar;

    // Step 1

    LinearLayout step1;

    WebView webView;

    Button signAgreement;

    // Step 2

    LinearLayout step2;

    SignaturePad signaturePad;

    Button clear;

    // Step 3

    LinearLayout step3;

    private ParseObject basicContract;
    private String subContractId;

    private void setUpView(){

        toolbar = findViewById(R.id.toolbar);

        // Step 1
        step1 = findViewById(R.id.step1);
        webView = findViewById(R.id.contract_webview);
        signAgreement = findViewById(R.id.sign_agreement);

        // Step 2
        step2 = findViewById(R.id.step2);
        signaturePad = findViewById(R.id.signature_pad);
        clear = findViewById(R.id.clear_signature);

        // Step 3
        step3 = findViewById(R.id.step3);


        signAgreement.setOnClickListener(v->clickSignAgreement());
        findViewById(R.id.save_signature).setOnClickListener(v->clickSaveSignature());
        findViewById(R.id.back_to_profile).setOnClickListener(v->onClickBackToProfile());
        findViewById(R.id.clear_signature).setOnClickListener(v->onClearedClick());
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);
        setUpView();

        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setTitle(getString(R.string.agreement_settings));

        loadAgreement();
        ParseUser user = ParseUser.getCurrentUser();
        boolean agreementSigned = user.getParseObject("userContractBasic") != null;
        signAgreement.setVisibility(agreementSigned ? GONE : VISIBLE);
        showStep(1);

        if (getIntent().hasExtra("id")) {
            subContractId = getIntent().getStringExtra("id");
        } else {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("ContractBasic");
            query.whereGreaterThanOrEqualTo("validUntil", Calendar.getInstance().getTime());
            query.whereLessThanOrEqualTo("validFrom", Calendar.getInstance().getTime());
            query.getFirstInBackground((parseObject, e) -> {
                if (e == null) {
                    basicContract = parseObject;
                }
            });
        }
//        signAgreement.setOnClickListener(v -> saveAgreement());
    }

    private void loadAgreement() {
        if (getIntent().getStringExtra("url") == null) {
            ContractParser contractParser = new ContractParser(this);
            contractParser.LoadBasicAgreement(this, false);
        } else {
            subContractId = getIntent().getStringExtra("id");
            webView.loadUrl(getIntent().getStringExtra("url"));
            toolbar.setTitle(getIntent().getStringExtra("title"));
        }
    }


    void clickSignAgreement() {
        showStep(2);
    }


    void clickSaveSignature() {
        showStep(3);
        saveAgreement();
    }

    void onClickBackToProfile() {
        finish();
    }

    private void showStep(int step) {
        switch (step) {
            case 1:
                step1.setVisibility(VISIBLE);
                step2.setVisibility(GONE);
                step3.setVisibility(GONE);
                break;
            case 2:
                step1.setVisibility(GONE);
                step2.setVisibility(VISIBLE);
                step3.setVisibility(GONE);
                break;
            case 3:
                step1.setVisibility(GONE);
                step2.setVisibility(GONE);
                step3.setVisibility(VISIBLE);
                break;
            default:
                finish();
        }
    }

    public void onClearedClick() {
        signaturePad.clear();
    }

    private void saveAgreement(){
        Bitmap bitmap = signaturePad.getSignatureBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] image = stream.toByteArray();
        ParseFile signatureImage = new ParseFile("0000signature.png", image);

        final LoadingDialog loadingDialog = new LoadingDialog();
        loadingDialog.show(getFragmentManager(), "uploadingSignature");

        if (subContractId == null) {//safe the basic contract
            if (basicContract == null) {
                return;
            }
            final ParseObject contract = new ParseObject("UserContractBasic");
            contract.put("contract", basicContract);
            contract.put("signature", signatureImage);
            contract.put("signedAt", new Date());

            contract.saveInBackground(e -> {
                if (e == null) {
                    ParseUser user = ParseUser.getCurrentUser();
                    user.put("userContractBasic", contract);
                    user.saveInBackground(e1 -> {
                        loadingDialog.dismiss();
                        if (e1 == null) {
                            Log.d("EcoRescue", "successfully uploaded signature.");
                            setResult(1);
                            finish();
                        } else {
                            setResult(0);
                            InformationDialog informationDialog = new InformationDialog();
                            informationDialog.setTitleAndMessage(getString(R.string.error), getString(R.string.error_uploading_signature));
//                            informationDialog.show(getFragmentManager(), "error");
                            informationDialog.show(getSupportFragmentManager(), "error");
                            Log.d("EcoRescue", "Error uploading signature. " + e1.getLocalizedMessage());
                        }
                    });

                } else {
                    loadingDialog.dismiss();
                    setResult(0);
                    InformationDialog informationDialog = new InformationDialog();
                    informationDialog.setTitleAndMessage(getString(R.string.error), getString(R.string.error_uploading_signature));
//                    informationDialog.show(getFragmentManager(), "error");
                    informationDialog.show(getSupportFragmentManager(), "error");
                    Log.d("EcoRescue", "Error uploading signature. " + e.getLocalizedMessage());
                }
            });
        } else { //Save a sub contract
            ParseObject sub = new ParseObject("UserContract");
            sub.put("contract", ParseObject.createWithoutData("ContractSub", subContractId));
            sub.put("signature", signatureImage);
            sub.put("signedAt", new Date());
            sub.put("user", ParseUser.getCurrentUser());
            sub.saveInBackground(e -> {
                loadingDialog.dismiss();
                if (e == null) {
                    Log.d("EcoRescue", "successfully uploaded signature.");
                    setResult(1);
                    finish();
                } else {
                    setResult(0);
                    InformationDialog informationDialog = new InformationDialog();
                    informationDialog.setTitleAndMessage(getString(R.string.error), getString(R.string.error_uploading_signature));
//                    informationDialog.show(getFragmentManager(), "error");
                    informationDialog.show(getSupportFragmentManager(), "error");
                    Log.d("EcoRescue", "Error uploading signature. " + e.getLocalizedMessage());
                }
            });
        }
    }


    @Override
    public void BasicAgreementPreloaded(boolean success) {

    }

    @Override
    public void AgreementLoaded(boolean success, String url, String title) {
        Log.d("EcoRescue", "Basic agreement loaded from cache. success=" + success);
        if (!success) {
            //Dialog
            finish();
        } else {
            toolbar.setTitle(title);
            webView.loadUrl(url);
        }
    }

}
