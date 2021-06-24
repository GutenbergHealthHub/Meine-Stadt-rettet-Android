package com.aurel.ecorescue.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;

import com.aurel.ecorescue.BuildConfig;
import com.aurel.ecorescue.R;
import com.aurel.ecorescue.profile.UserViewPopulator;
import com.aurel.ecorescue.service.SessionManager;
import com.aurel.ecorescue.view.IntroActivity;
import com.aurel.ecorescue.x_tools.x_EcoPreferences;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.parse.ParseUser;

import timber.log.Timber;

public class NavigationDrawer {

    public static final int HOME = 1;
    public static final int AED_MAP = 2;
    public static final int NEWS_AND_EVENTS = 3;
    public static final int COURSES = 4;

    public static final int PROFILE = 5;
    public static final int SETTINGS = 6;
    public static final int STANDBY_MODE = 7;
    public static final int SPONTANEOUS_HELPER = 14;
    public static final int LOGIN = 11;

    public static final int ABOUT_APP = 12;
    public static final int ABOUT_MeineStadtRettet = 13;
    public static final int FAQ = 8;
    public static final int CONTACT_AND_IMPRINT = 9;
    public static final int LEGAL_NOTICE = 10;


    private Drawer drawer;
    private NavController navController;
    private Context context;

    private PrimaryDrawerItem itemHome = new PrimaryDrawerItem().withIdentifier(HOME).withName(R.string.drawer_home);
    private PrimaryDrawerItem itemMap = new PrimaryDrawerItem().withIdentifier(AED_MAP).withName(R.string.drawer_map);
    private PrimaryDrawerItem itemNews = new PrimaryDrawerItem().withIdentifier(NEWS_AND_EVENTS).withName(R.string.drawer_news);
    private PrimaryDrawerItem itemCourses = new PrimaryDrawerItem().withIdentifier(COURSES).withName(R.string.drawer_courses);
    private PrimaryDrawerItem itemLogin = new PrimaryDrawerItem().withIdentifier(LOGIN).withName(R.string.drawer_login);
    private PrimaryDrawerItem itemProfile = new PrimaryDrawerItem().withIdentifier(PROFILE).withName(R.string.drawer_account_profile);
    private PrimaryDrawerItem itemSettings = new PrimaryDrawerItem().withIdentifier(SETTINGS).withName(R.string.drawer_account_settings);
    private PrimaryDrawerItem itemStandby = new PrimaryDrawerItem().withIdentifier(STANDBY_MODE).withName(R.string.drawer_account_standby);
    private PrimaryDrawerItem itemSpontaneousHelper = new PrimaryDrawerItem().withIdentifier(SPONTANEOUS_HELPER).withName(R.string.spontaneous_helper);
    private PrimaryDrawerItem itemAboutApp = new PrimaryDrawerItem().withIdentifier(ABOUT_APP).withName(R.string.drawer_about).withSelectable(false);
    private PrimaryDrawerItem itemAboutMeineStadtRettet = new PrimaryDrawerItem().withIdentifier(ABOUT_MeineStadtRettet).withName(R.string.about_meinestadtrettet);
    private PrimaryDrawerItem itemFAQ = new PrimaryDrawerItem().withIdentifier(FAQ).withName(R.string.drawer_about_faq);
    private PrimaryDrawerItem itemContact = new PrimaryDrawerItem().withIdentifier(CONTACT_AND_IMPRINT).withName(R.string.drawer_about_contact);
    private PrimaryDrawerItem itemLegal = new PrimaryDrawerItem().withIdentifier(LEGAL_NOTICE).withName(R.string.drawer_about_legal);

    private DrawerBuilder builder;

    public NavigationDrawer(Activity activity, NavController navController){
        BadgeStyle badgeStyle = new BadgeStyle();
        badgeStyle.withColor(activity.getResources().getColor(R.color.md_red_700));
        badgeStyle.withTextColor(activity.getResources().getColor(R.color.md_white_1000));
        itemHome.withBadgeStyle(badgeStyle);
        this.navController = navController;
        context = activity;
        builder = new DrawerBuilder().withActivity(activity);
        addHeader(builder);
        addMainItems(builder);
        addAccountSection(builder);
        addAboutSection(builder);
        setupNavigationDrawer(builder, (AppCompatActivity) activity);
        builder.withOnDrawerItemClickListener((view, position, drawerItem) -> onDrawerItemSelected(drawerItem));
        SharedPreferences pref = x_EcoPreferences.GetSharedPreferences(context);
        int badgeNumber = pref.getInt("badge", 0);
//        if (badgeNumber!=0) {
//            itemHome.withBadge(String.valueOf(badgeNumber));
//        }
        setBadgeNumber(badgeNumber);
    }

    public void setBadgeNumber(int number) {
        Timber.d("Badge updated: %s", number);
//        if (number == 0) {
//            itemHome = new PrimaryDrawerItem().withIdentifier(HOME).withName(R.string.drawer_home).withSelectedTextColor(context.getResources().getColor(R.color.colorPrimary));
//        } else {
//            itemHome.withBadge(String.valueOf(number));
//        }
//
        SessionManager sessionManager = new SessionManager(context);

        if (number>0 && ParseUser.getCurrentUser()!=null && sessionManager.isLoggedIn()) {
                itemHome.withBadge(String.valueOf(number));
        } else {
            itemHome = new PrimaryDrawerItem().withIdentifier(HOME).withName(R.string.drawer_home).withSelectedTextColor(context.getResources().getColor(R.color.colorPrimary));
        }
        drawer.updateItem(itemHome);
//        addMainItems(builder);
    }



    private void addHeader(DrawerBuilder builder) {
        builder.withHeader(R.layout.drawer_header);
    }

    private void addMainItems(DrawerBuilder builder) {
        builder.addDrawerItems(
                itemHome.withSelectedTextColor(context.getResources().getColor(R.color.colorPrimary)),
                itemMap.withSelectedTextColor(context.getResources().getColor(R.color.colorPrimary)),
                itemNews.withSelectedTextColor(context.getResources().getColor(R.color.colorPrimary)),
                itemCourses.withSelectedTextColor(context.getResources().getColor(R.color.colorPrimary)));
    }



    private void addAccountSection(DrawerBuilder builder){
        ParseUser user = ParseUser.getCurrentUser();
        if (user!=null) {
            builder.addDrawerItems(
                    new SectionDrawerItem().withName(R.string.drawer_account).withTextColorRes(R.color.colorPrimary),
                    itemProfile.withSelectedTextColor(context.getResources().getColor(R.color.colorPrimary)),
                    itemSettings.withSelectedTextColor(context.getResources().getColor(R.color.colorPrimary)),
                    itemStandby.withSelectedTextColor(context.getResources().getColor(R.color.colorPrimary)),
                    itemSpontaneousHelper.withSelectedTextColor(context.getResources().getColor(R.color.colorPrimary)));
        }

    }


    private void addAboutSection(DrawerBuilder builder){
        if (BuildConfig.APPLICATION_ID.equals("ecorium.MeineStadtRettet")){
            builder.addDrawerItems(
//                new SectionDrawerItem(),
                    new SectionDrawerItem().withName(R.string.text_about).withTextColorRes(R.color.colorPrimary),
                    itemAboutApp.withSelectedTextColor(context.getResources().getColor(R.color.colorPrimary)),
                    itemFAQ.withSelectedTextColor(context.getResources().getColor(R.color.colorPrimary)),
                    itemContact.withSelectedTextColor(context.getResources().getColor(R.color.colorPrimary)),
                    itemLegal.withSelectedTextColor(context.getResources().getColor(R.color.colorPrimary)),
                    itemAboutMeineStadtRettet.withSelectedTextColor(context.getResources().getColor(R.color.colorPrimary)));
        } else {
            builder.addDrawerItems(
//                new SectionDrawerItem(),
                    new SectionDrawerItem().withName(R.string.text_about).withTextColorRes(R.color.colorPrimary),
                    itemAboutApp.withSelectedTextColor(context.getResources().getColor(R.color.colorPrimary)),
                    itemFAQ.withSelectedTextColor(context.getResources().getColor(R.color.colorPrimary)),
                    itemContact.withSelectedTextColor(context.getResources().getColor(R.color.colorPrimary)),
                    itemLegal.withSelectedTextColor(context.getResources().getColor(R.color.colorPrimary)));
        }

    }

    private void setupNavigationDrawer(DrawerBuilder builder, AppCompatActivity activity){
        drawer = builder.build();
        UserViewPopulator.PopulateUserView(drawer.getHeader(), activity);
//        drawer.getHeader().setOnClickListener(view -> {
//            if (!UserDao.isAnonymous()) {//User is logged in
//                Intent i = new Intent(activity, LoginRegisterActivity.class);
//                activity.startActivity(i);
//            } else {
////                ActivityCompat.finishAffinity(activity);
//                Intent i = new Intent(activity, LoginRegisterActivity.class);
//                activity.startActivity(i);
//            }
//        });
//        drawer.getHeader().setOnClickListener(view -> {
//            navController.navigate(R.id.profileFragment);
//            drawer.closeDrawer();
//        });
    }

    private boolean onDrawerItemSelected(IDrawerItem drawerItem) {
        if (drawerItem == null) {
            return false;
        }
        navController.popBackStack();
        switch ((int) drawerItem.getIdentifier()) {
            case HOME:
                navController.navigate(R.id.mainFragment);
                return false;
            case AED_MAP:
                navController.navigate(R.id.aedMapFragment);
                return false;
            case NEWS_AND_EVENTS:
                navController.navigate(R.id.newsFragment);
                return false;
            case COURSES:
                navController.navigate(R.id.coursesFragment);
                return false;
            case PROFILE:
                navController.navigate(R.id.profileFragment);
                return false;
            case SETTINGS:
                navController.navigate(R.id.settingsFragment);
                return false;
            case STANDBY_MODE:
                navController.navigate(R.id.standbyModeFragment);
                return false;
            case SPONTANEOUS_HELPER:
                navController.navigate(R.id.spontaneousHelperFragment);
                return false;
            case LOGIN:
//                navController.navigate(R.id.mainFragment);
//                Intent i = new Intent(activity, LoginRegisterActivity.class);
//                activity.startActivity(i);
                return false;
            case FAQ:
                navController.navigate(R.id.faqFragment);
                return false;
            case CONTACT_AND_IMPRINT:
                navController.navigate(R.id.contactImprintFragment);
                return false;
            case LEGAL_NOTICE:
                navController.navigate(R.id.legalNoticeFragment);
                return false;
            case ABOUT_APP:
                context.startActivity(new Intent(context, IntroActivity.class));
                return false;
            case ABOUT_MeineStadtRettet:
                navController.navigate(R.id.aboutMeineStadtRettetFragment);
                return false;
        }
        return false;
    }

    public void openDrawer(){
        drawer.openDrawer();;
    }


    public void setSelection(int selection){
//        drawer.setSelectionAtPosition(selection, false);
    }
}
