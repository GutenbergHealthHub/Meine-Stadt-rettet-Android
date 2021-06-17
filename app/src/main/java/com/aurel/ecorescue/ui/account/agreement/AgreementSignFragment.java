package com.aurel.ecorescue.ui.account.agreement;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.utils.StyleUtils;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import timber.log.Timber;


public class AgreementSignFragment extends Fragment {

    public AgreementSignFragment() {}

    private NavController navController;
    private View view;

    private SignaturePad mPad;
    private ParseObject mContract;
    private boolean isBasicAgreement = true;
    private WebView webView;

    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_agreement_sign, container, false);
    }

    public void onViewCreated(@NotNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        navController = Navigation.findNavController(view);
        toolbar.setNavigationOnClickListener(v -> navController.navigateUp());
        if (getContext() != null) {
            toolbar.setNavigationIcon(StyleUtils.getHomeAsUpIndicator(getContext()));
        }

        mPad = view.findViewById(R.id.signature_pad);
        webView = view.findViewById(R.id.wv_agreement);
        view.findViewById(R.id.btn_sign).setOnClickListener(v -> showSignContract());
        view.findViewById(R.id.clear_signature).setOnClickListener(v -> mPad.clear());
        view.findViewById(R.id.save_signature).setOnClickListener(v -> {
            if (!mPad.isEmpty()) {
                sendSignature();
            } else {
                Toast.makeText(view.getContext(), getString(R.string.agreement_not_signed), Toast.LENGTH_SHORT).show();
            }
        });
        Bundle bundle = getArguments();
        if (bundle!=null) {
            String agreementType = bundle.getString("agreement");
            boolean agreementStatus = bundle.getBoolean("isSigned");
            showAgreement(agreementStatus);
            if (agreementType!=null && agreementType.equals("basic")) {
                isBasicAgreement = true;
                toolbar.setTitle(getString(R.string.agreement_basic));
                ParseQuery<ParseObject> query = ParseQuery.getQuery("ContractBasic");
                query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
                query.getFirstInBackground((object, e) -> {
                    if (e==null) {
                        mContract = object;
                        String url = object.getString("url");
                        if (url==null) url = "";
                        webView.loadUrl(url);
                        webView.setWebViewClient(new WebViewClient() {
                            public void onPageFinished(WebView view, String url) {
                                Timber.d("Finished loading");
                            }
                        });
                    } else {
                        Timber.d("ParseException: " + e.getCode() + " " + e.getLocalizedMessage() + " error:" + e + " msg:" + e.getMessage());
                    }
                });
            } else {
                isBasicAgreement = false;
                toolbar.setTitle(getString(R.string.agreement_additional));
                String url = bundle.getString("url");
                if (url==null) url = "";
                webView.loadUrl(url);
                webView.setWebViewClient(new WebViewClient() {
                    public void onPageFinished(WebView view, String url) {
                        Timber.d("Finished loading");
                    }
                });

            }
        }
    }

    private void showAgreement(boolean agreementStatusSigned){
        if (agreementStatusSigned) {
            view.findViewById(R.id.btn_sign).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.btn_sign).setVisibility(View.VISIBLE);
        }
        view.findViewById(R.id.content_agreement_info).setVisibility(View.VISIBLE);
        view.findViewById(R.id.content_agreement_sign).setVisibility(View.GONE);
    }

    private void showSignContract(){
        view.findViewById(R.id.content_agreement_info).setVisibility(View.GONE);
        view.findViewById(R.id.content_agreement_sign).setVisibility(View.VISIBLE);
    }


    private void sendSignature(){
        Bitmap bitmap = mPad.getSignatureBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] image = stream.toByteArray();
        ParseFile signatureImage = new ParseFile("0000signature.png", image);

        if (isBasicAgreement) {
            final ParseObject contract = new ParseObject("UserContractBasic");
            contract.put("contract", mContract);
            contract.put("signature", signatureImage);
            contract.put("signedAt", new Date());
            contract.saveInBackground(e -> {
                if (e == null) {
                    ParseUser user = ParseUser.getCurrentUser();
                    user.put("userContractBasic", contract);
                    user.saveInBackground(e1 -> {
//                    loadingDialog.dismiss();
                        if (e1 == null) {
                            Timber.d("successfully uploaded signature.");
                            Toast.makeText(view.getContext().getApplicationContext(), getString(R.string.save), Toast.LENGTH_SHORT).show();
                            navController.navigateUp();
                        } else {
                            signatureUploadError(e1);
                        }
                    });

                } else {
                    signatureUploadError(e);
                }
            });
        } else {
            ParseObject sub = new ParseObject("UserContract");
            Bundle bundle = getArguments();
            if (bundle!=null){
                String id = bundle.getString("cc");
                if (id==null) id = "-1";
                sub.put("contract", ParseObject.createWithoutData("ContractSub", id));
            }
            sub.put("signature", signatureImage);
            sub.put("signedAt", new Date());
            sub.put("user", ParseUser.getCurrentUser());
            sub.saveInBackground(e -> {
                if (e == null) {
                    Timber.d("successfully uploaded additional signature.");
                    Toast.makeText(view.getContext().getApplicationContext(), getString(R.string.save), Toast.LENGTH_SHORT).show();
                    navController.navigateUp();
                } else {
                    signatureUploadError(e);
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        if (webView != null)
            webView.destroy();
        super.onDestroy();
    }

    private void signatureUploadError(ParseException e){
        Timber.d(e, "Error uploading signature");
        Toast.makeText(view.getContext().getApplicationContext(), getString(R.string.error_uploading_signature), Toast.LENGTH_SHORT).show();
        navController.navigateUp();
    }
}
