package com.aurel.ecorescue.ui.news;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.data.DataRepository;
import com.aurel.ecorescue.model.News;
import com.aurel.ecorescue.utils.DateUtils;
import com.aurel.ecorescue.utils.NavigationDrawer;
import com.aurel.ecorescue.utils.ParseUtils;
import com.aurel.ecorescue.utils.StyleUtils;
import com.aurel.ecorescue.view.MainActivity;
import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;


public class NewsDetailsFragment extends Fragment {

    public NewsDetailsFragment() {}

    private NavController navController;
    private DataRepository mRepository;


    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news_details, container, false);
    }


    public void onViewCreated(@NotNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.news_tab_news);
        navController = Navigation.findNavController(view);
        mRepository = DataRepository.getInstance();
        if (getContext() != null) {
            toolbar.setNavigationIcon(StyleUtils.getHomeAsUpIndicator(getContext()));
        }
        toolbar.setNavigationOnClickListener(v -> {
            navController.navigateUp();
        });
        Bundle bundle = getArguments();
        if (bundle!=null) {
            News news = mRepository.getNews(ParseUtils.getString(bundle.getString("id")));

            if (news!=null) {
                ((TextView) view.findViewById(R.id.tv_title)).setText(ParseUtils.getString(news.title));
                ((TextView) view.findViewById(R.id.tv_title_second)).setText(ParseUtils.getString(news.subtitle));
                ((TextView) view.findViewById(R.id.tv_title_third)).setText(new DateUtils().getReadableDate(news.CreatedAt));
                ((TextView) view.findViewById(R.id.tv_content)).setText(ParseUtils.getString(news.Text));

                ImageView imageView = view.findViewById(R.id.iv_cover_image);
                if (news.imageObject!=null) {
                    Glide.with(getContext()).load(news.imageObject).into(imageView);
                    imageView.setVisibility(View.VISIBLE);
                }

                if (!ParseUtils.getString(news.Url).isEmpty()) {
                    view.findViewById(R.id.btn_continue_reading).setOnClickListener(v -> {
                        String url = ParseUtils.getString(news.Url);

                        if (!url.contains("http")) {
                            url = "https://" + url;
                        }
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    });
                } else {
                    view.findViewById(R.id.btn_continue_reading).setVisibility(View.GONE);
                }

            }

        }
    }


}
