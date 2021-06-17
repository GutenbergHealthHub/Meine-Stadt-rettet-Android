package com.aurel.ecorescue.ui.information;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.utils.NavigationDrawer;
import com.aurel.ecorescue.utils.ParseUtils;
import com.aurel.ecorescue.view.MainActivity;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;


public class LegalNoticeFragment extends Fragment {

    public LegalNoticeFragment() {}

    private NavigationDrawer mDrawerHelper;

    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_legal_notice, container, false);
    }

    public void onViewCreated(@NotNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        mDrawerHelper = ((MainActivity) requireActivity()).getNavigationDrawer();
        toolbar.setNavigationOnClickListener(v -> {
            mDrawerHelper.openDrawer();
        });
        toolbar.setTitle(R.string.drawer_about_legal);

        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView tvError = view.findViewById(R.id.tv_error);

        WebView myWebView = view.findViewById(R.id.wv_info);


        ParseQuery<ParseObject> query = ParseQuery.getQuery("URLS");
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.whereEqualTo("type", "legal");
        query.findInBackground((list, e) -> {
            if (e == null) {
                for (ParseObject o: list) {
                    String url = ParseUtils.getString(getResources().getString(R.string.data_info_url), o);
                    myWebView.loadUrl(url);
                    myWebView.setWebViewClient(new WebViewClient() {
                        public void onPageFinished(WebView view, String url) {
                            // do your stuff here
                            Timber.d("Finished loading");
                            progressBar.setVisibility(View.GONE);
                            tvError.setVisibility(View.GONE);
                        }
                    });
                }

            } else {
                progressBar.setVisibility(View.GONE);
                tvError.setVisibility(View.VISIBLE);
                Timber.d("ParseException: " + e.getCode() + " " + e.getLocalizedMessage() + " error:" + e + " msg:" + e.getMessage());
            }

        });

    }


}
