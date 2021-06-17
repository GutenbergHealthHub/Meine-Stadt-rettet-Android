package com.aurel.ecorescue.view.protocols;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.service.SessionManager;
import com.aurel.ecorescue.view.components.MultiLineEditTextWithDone;
import com.aurel.ecorescue.view.components.MultitypeSelectionList.ItemTypes;
import com.aurel.ecorescue.view.components.MultitypeSelectionList.MultitypeSelectionListView;
import com.aurel.ecorescue.view.x_emergency.EmergencyAppCompatActivity;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by aurel on 10-Oct-16.
 */

public class ProtocolActivity extends EmergencyAppCompatActivity {

    private int mCurrentPageIndex;
    private Set<Integer> mVisiblePages;
    private Map<Integer, ProtocolPage> mPagesMap;
    private ParseObject mEmergencyState;
    private SessionManager mSessionManager;

    private AlertDialog mAlertDialog;

    protected TextView mTitle;

    protected TextView mDescription;

    protected TextView mPageIndicator;

    LinearLayout mContentArea;


    protected TextView mNextPageButton;

    protected TextView mPreviousPageButton;


    protected View mProgressBarTopDone;

    protected View mProgressBarBottomDone;


    protected View mProgressBarTopTodo;

    protected View mProgressBarBottomTodo;

    private static Map<Integer, String> PAGE_TO_PARSE_MAP_PRIMARY = new HashMap();
    private static Map<Integer, String> PAGE_TO_PARSE_MAP_SECONDARY = new HashMap();
    static {
        PAGE_TO_PARSE_MAP_PRIMARY.put(R.string.protocol_arrival_time, "startLaterRelAmbulance" );
        PAGE_TO_PARSE_MAP_PRIMARY.put(R.string.protocol_age_of_patient, "ageCategory" );
        PAGE_TO_PARSE_MAP_PRIMARY.put(R.string.protocol_sex_title, "sex" );
        PAGE_TO_PARSE_MAP_PRIMARY.put(R.string.protocol_resuscitation_title, "reanimationValue" );
        PAGE_TO_PARSE_MAP_PRIMARY.put(R.string.report_location, "startLocationValueN" );
        PAGE_TO_PARSE_MAP_PRIMARY.put(R.string.protocol_schoolbuilding_title, "schoolBuilding" );
        PAGE_TO_PARSE_MAP_PRIMARY.put(R.string.report_conscious, "startReactionValue" );
        PAGE_TO_PARSE_MAP_PRIMARY.put(R.string.protocol_breathing, "startRespirationValue" );
        PAGE_TO_PARSE_MAP_PRIMARY.put(R.string.report_diagnosis, "startDiagnoseValue" );
        PAGE_TO_PARSE_MAP_PRIMARY.put(R.string.protocol_orientation, "startOrientationValueN" );
        PAGE_TO_PARSE_MAP_PRIMARY.put(R.string.protocol_sports_title, "relationWithSport" );
        PAGE_TO_PARSE_MAP_PRIMARY.put(R.string.protocol_collapse_title, "collapseObserved" );
        PAGE_TO_PARSE_MAP_PRIMARY.put(R.string.protocol_cardiac_title, "measureChestCompressionValueN" );
        PAGE_TO_PARSE_MAP_PRIMARY.put(R.string.protocol_ventilation_title, "measureRespirationValue" );
        PAGE_TO_PARSE_MAP_PRIMARY.put(R.string.protocol_telemedicine_title, "telemedicin" );
        PAGE_TO_PARSE_MAP_PRIMARY.put(R.string.protocol_defibrillator_title, "measureDefiValueN" );
        PAGE_TO_PARSE_MAP_PRIMARY.put(R.string.protocol_aedshocks_title, "measureDefiShockCount" );
        PAGE_TO_PARSE_MAP_PRIMARY.put(R.string.protocol_manufacturer_title, "producerDefiValue" );
        PAGE_TO_PARSE_MAP_PRIMARY.put(R.string.protocol_publicaed_title, "publicDefi" );
        PAGE_TO_PARSE_MAP_PRIMARY.put(R.string.report_conscious_end_title, "endStatusA" );
        PAGE_TO_PARSE_MAP_PRIMARY.put(R.string.report_cardiovascular_title, "endRespirationValue" );
        PAGE_TO_PARSE_MAP_PRIMARY.put(R.string.protocol_remarks, "endComment" );
        PAGE_TO_PARSE_MAP_PRIMARY.put(R.string.cancel_reason, "cancelComment" );

        PAGE_TO_PARSE_MAP_SECONDARY.put(R.string.protocol_arrival_time, "startMinutesRelAmbulance");
        PAGE_TO_PARSE_MAP_SECONDARY.put(R.string.protocol_age_of_patient, "age" );
    }

    private class ProtocolPage {
        String mTopic;
        String mDescription;
        View mView;

        ProtocolPage(int topicResourceId, int descriptionResourceId, View view) {
            this.mTopic = getResources().getString(topicResourceId);
            this.mDescription = getResources().getString(descriptionResourceId);
            this.mView = view;
        }
    }

    private void setUpView(){
        mTitle = findViewById(R.id.title);
        mDescription = findViewById(R.id.description);
        mPageIndicator = findViewById(R.id.pageIndicator);
        mContentArea = findViewById(R.id.contentArea);

        mNextPageButton = findViewById(R.id.nextPageButton);
        mPreviousPageButton = findViewById(R.id.previousPageButton);

        mProgressBarTopDone = findViewById(R.id.progressBarTopDone);
        mProgressBarBottomDone = findViewById(R.id.progressBarBottomDone);

        mProgressBarTopTodo = findViewById(R.id.progressBarTopTodo);
        mProgressBarBottomTodo = findViewById(R.id.progressBarBottomTodo);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protocol);
        setUpView();

        String emergencyStateId = getIntent().getStringExtra("emergencyStateId");
        if (emergencyStateId == null || emergencyStateId.isEmpty()) {
            finish();
        } else {
            try {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setView(getLayoutInflater().inflate(R.layout.alert_protocol_save, null));
                mAlertDialog = dialogBuilder.create();

                mSessionManager = new SessionManager(this);
                ParseQuery<ParseObject> emergencyStateQuery = new ParseQuery<>("EmergencyState");
                emergencyStateQuery.whereEqualTo("objectId", emergencyStateId);
                mEmergencyState = emergencyStateQuery.getFirst();
                if (mEmergencyState != null) {
                    mCurrentPageIndex = -1;
                    createPages();
                    loadTemporary();
                    updatePageVisibility();
                    setPage(0);
                } else {
                    finish();
                }
            } catch (ParseException ex) {
                finish();
            }
        }
    }

    private MultitypeSelectionListView createMSLView() {
        return (MultitypeSelectionListView) getLayoutInflater().inflate(R.layout.activity_protocol_multiselect_view, null);
    }

    public void nextPage(View view) {
        setPage(mCurrentPageIndex + 1);
    }

    public void previousPage(View view) {
        setPage(mCurrentPageIndex - 1);
    }

    @SuppressLint("SetTextI18n")
    private void setPage(int pageIndex) {
        ProtocolPage lastPage = getPageAtIndex(mCurrentPageIndex);
        updatePageVisibility();
        ProtocolPage page = getPageAtIndex(pageIndex);
        if (page != null && page != lastPage) {
            mCurrentPageIndex = pageIndex;

            //hide previous page
            if (lastPage != null) {
                if (lastPage.mView instanceof MultitypeSelectionListView) {
                    mContentArea.removeView(lastPage.mView);
                    findViewById(R.id.pageMultiSelectionList).setVisibility(View.GONE);
                } else {
                    lastPage.mView.setVisibility(View.GONE);
                }
            }

            mTitle.setText(page.mTopic);
            mDescription.setText(page.mDescription);
            mPageIndicator.setText((mCurrentPageIndex + 1) + "/" + mVisiblePages.size());

            //show next page
            if (page.mView instanceof MultitypeSelectionListView) {
                findViewById(R.id.pageMultiSelectionList).setVisibility(View.VISIBLE);
                mContentArea.addView(page.mView);
            } else {
                page.mView.setVisibility(View.VISIBLE);
            }

            //Next button validations
            if (page.mView instanceof MultitypeSelectionListView) {
                MultitypeSelectionListView view = (MultitypeSelectionListView) page.mView;

                if (view.getValue() != null || (view.getValues() != null && view.getValues().size() > 0)) {
                    mNextPageButton.setClickable(true);
                    mNextPageButton.setAlpha(1.0f);
                } else {
                    mNextPageButton.setClickable(false);
                    mNextPageButton.setAlpha(0.5f);
                }

                view.setOnValuesChangedListener((Object primaryValue, Object secondaryValue) -> {
                    if (primaryValue != null) {
                        mNextPageButton.setClickable(true);
                        mNextPageButton.setAlpha(1.0f);
                    } else {
                        mNextPageButton.setClickable(false);
                        mNextPageButton.setAlpha(0.5f);
                    }
                });

                view.setOnMultiselectValuesChangedListener((Set<Object> values) -> {
                    if (values != null && values.size() > 0) {
                        mNextPageButton.setClickable(true);
                        mNextPageButton.setAlpha(1.0f);
                    } else {
                        mNextPageButton.setClickable(false);
                        mNextPageButton.setAlpha(0.5f);
                    }
                });
            } else if ( page.mView.getId() == R.id.pageArrivedAt) {
                TextView dateView = findViewById(R.id.selectedDate);
                TextView timeView = findViewById(R.id.selectedTime);

                TextWatcher tw = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if(!dateView.getText().equals(getResources().getString(R.string.not_yet_selected)) && !timeView.getText().equals(getResources().getString(R.string.not_yet_selected))) {
                            mNextPageButton.setClickable(true);
                            mNextPageButton.setAlpha(1.0f);
                        } else {
                            mNextPageButton.setClickable(false);
                            mNextPageButton.setAlpha(0.5f);
                        }
                    }
                };
                dateView.addTextChangedListener(tw);
                timeView.addTextChangedListener(tw);
                tw.afterTextChanged(null);
            }

            mNextPageButton.setVisibility((pageIndex + 1 >= mVisiblePages.size()) ? View.GONE : View.VISIBLE);
            mPreviousPageButton.setVisibility((pageIndex <= 0) ? View.GONE : View.VISIBLE);

            float progress = (pageIndex + 1) / (float) mVisiblePages.size();
            int height = mProgressBarTopDone.getLayoutParams().height;
            mProgressBarTopDone.setLayoutParams(new LinearLayout.LayoutParams(0, height, progress));
            mProgressBarBottomDone.setLayoutParams(new LinearLayout.LayoutParams(0, height, progress));
            mProgressBarTopTodo.setLayoutParams(new LinearLayout.LayoutParams(0, height, 1 - progress));
            mProgressBarBottomTodo.setLayoutParams(new LinearLayout.LayoutParams(0, height, 1 - progress));
        }
    }

    @SuppressLint("SetTextI18n")
    private ProtocolPage getPageAtIndex(int pageIndex) {
        ArrayList<Integer> pages = new ArrayList<>(mVisiblePages);
        if (pageIndex >= 0 && pageIndex < pages.size()) {
            return mPagesMap.get(pages.get(pageIndex));
        }
        return null;
    }

    private void updatePageVisibility() {
        mVisiblePages = new LinkedHashSet<>();
        if (mEmergencyState.get("endedAt") != null) {
            if (mEmergencyState.get("arrivedAt") == null) {
                mVisiblePages.add(R.string.protocol_absolute_arrival_time);
            }
            mVisiblePages.add(R.string.protocol_arrival_time);
            mVisiblePages.add(R.string.protocol_age_of_patient);
            mVisiblePages.add(R.string.protocol_sex_title);
            mVisiblePages.add(R.string.protocol_resuscitation_title);
            mVisiblePages.add(R.string.report_location);
            mVisiblePages.add(R.string.protocol_schoolbuilding_title);
            mVisiblePages.add(R.string.report_diagnosis);
            mVisiblePages.add(R.string.protocol_orientation);
            mVisiblePages.add(R.string.report_conscious);
            mVisiblePages.add(R.string.protocol_breathing);
            mVisiblePages.add(R.string.protocol_sports_title);
            mVisiblePages.add(R.string.protocol_collapse_title);
            mVisiblePages.add(R.string.protocol_cardiac_title);
            mVisiblePages.add(R.string.protocol_ventilation_title);
            mVisiblePages.add(R.string.protocol_telemedicine_title);
            mVisiblePages.add(R.string.protocol_defibrillator_title);

            Integer defiSelection = ((MultitypeSelectionListView) mPagesMap.get(R.string.protocol_defibrillator_title).mView).<Integer>getValue();
            if (defiSelection != null && defiSelection >= 1 && defiSelection <= 2) {
                mVisiblePages.add(R.string.protocol_aedshocks_title);
                mVisiblePages.add(R.string.protocol_manufacturer_title);
                mVisiblePages.add(R.string.protocol_publicaed_title);
            }
            mVisiblePages.add(R.string.report_cardiovascular_title);
            mVisiblePages.add(R.string.report_conscious_end_title);
            mVisiblePages.add(R.string.protocol_remarks);
        } else if (mEmergencyState.get("cancelledAt") != null) {
            mVisiblePages.add(R.string.cancel_reason);
        }
    }

    private void createPages() {
        mPagesMap = new LinkedHashMap<>();

        View pageArrivedAt =findViewById(R.id.pageArrivedAt);
        if(mEmergencyState.getDate("acceptedAt") != null) {
            Calendar accepted = Calendar.getInstance();
            accepted.setTime(mEmergencyState.getDate("acceptedAt"));
            int day = accepted.get(Calendar.DAY_OF_MONTH);
            ((TextView) pageArrivedAt.findViewById(R.id.emergencyAcceptedLabel)).setText(String.format("%02d/%02d/%04d %02d:%02d", accepted.get(Calendar.DAY_OF_MONTH), accepted.get(Calendar.MONTH) + 1, accepted.get(Calendar.YEAR), accepted.get(Calendar.HOUR_OF_DAY), accepted.get(Calendar.MINUTE)));
        }
        if(mEmergencyState.getDate("endedAt") != null) {
            Calendar finished = Calendar.getInstance();
            finished.setTime(mEmergencyState.getDate("endedAt"));
            ((TextView) pageArrivedAt.findViewById(R.id.emergencyFinishedLabel)).setText(String.format("%02d/%02d/%04d %02d:%02d", finished.get(Calendar.DAY_OF_MONTH), finished.get(Calendar.MONTH) + 1, finished.get(Calendar.YEAR), finished.get(Calendar.HOUR_OF_DAY), finished.get(Calendar.MINUTE)));
        }
        mPagesMap.put(R.string.protocol_absolute_arrival_time, new ProtocolPage(R.string.protocol_absolute_arrival_time, R.string.protocol_absolute_arrival_time_helptext, findViewById(R.id.pageArrivedAt)));

        mPagesMap.put(R.string.protocol_arrival_time, new ProtocolPage(R.string.protocol_arrival_time, R.string.protocol_relative_arrival_time, createMSLView()
                .addItem(getResources().getString(R.string.later_than_ambulance), false)
                .addItem(getResources().getString(R.string.earlier_than_ambulance), true, new ItemTypes.NumberPicker("%d min", 1, 40, 3))
                .create(1, false)));

        mPagesMap.put(R.string.protocol_age_of_patient, new ProtocolPage(R.string.protocol_age_of_patient, R.string.protocol_age_helptext, createMSLView()
                .addItem(getResources().getString(R.string.protocol_age_days_1to7), 1)
                .addItem(getResources().getString(R.string.protocol_age_days_8to28), 2)
                .addItem(getResources().getString(R.string.protocol_age_younger_than_1_year), 3)
                .addItem(getResources().getString(R.string.protocol_age_typein), 4, new ItemTypes.NumberPicker("%d", 1, 120, 30))
                .create(3, false)));

        mPagesMap.put(R.string.protocol_sex_title, new ProtocolPage(R.string.protocol_sex_title, R.string.protocol_sex_helptext, createMSLView()
                .addItem(getResources().getString(R.string.protocol_sex_male), "m")
                .addItem(getResources().getString(R.string.protocol_sex_female), "f")
                .create(0, false)));

        mPagesMap.put(R.string.protocol_resuscitation_title, new ProtocolPage(R.string.protocol_resuscitation_title, R.string.protocol_resuscitation_helptext, createMSLView()
                .addItem(getResources().getString(R.string.protocol_resuscitation_01), 1)
                .addItem(getResources().getString(R.string.protocol_resuscitation_02), 2)
                .addItem(getResources().getString(R.string.protocol_resuscitation_03), 3)
                .addItem(getResources().getString(R.string.protocol_resuscitation_04), 4)
                .addItem(getResources().getString(R.string.protocol_unknown), -1)
                .create(4, false)));

        mPagesMap.put(R.string.report_location, new ProtocolPage(R.string.report_location, R.string.protocol_location_helptext, createMSLView()
                .addItem(getResources().getString(R.string.not_documented), 99)
                .addItem(getResources().getString(R.string.report_location_apartment), 1)
                .addItem(getResources().getString(R.string.report_location_nursinghome), 2)
                .addItem(getResources().getString(R.string.report_location_workplace), 3)
                .addItem(getResources().getString(R.string.report_location_docotorsoffice), 4)
                .addItem(getResources().getString(R.string.report_location_street), 5)
                .addItem(getResources().getString(R.string.report_location_public), 6)
                .addItem(getResources().getString(R.string.report_location_hospital), 7)
                .addItem(getResources().getString(R.string.report_location_massevent), 8)
                .addItem(getResources().getString(R.string.report_location_others), 9)
                .addItem(getResources().getString(R.string.report_location_educational), 10)
                .addItem(getResources().getString(R.string.report_location_sportsclub), 11)
                .addItem(getResources().getString(R.string.report_location_birthhouse), 12)
                .addItem(getResources().getString(R.string.protocol_unknown), -1)
                .create(0, false)));

        mPagesMap.put(R.string.protocol_schoolbuilding_title, new ProtocolPage(R.string.protocol_schoolbuilding_title, R.string.protocol_schoolbuilding_helptext, createMSLView()
                .addItem(getResources().getString(R.string.no), false)
                .addItem(getResources().getString(R.string.yes), true)
                .create(0, false)));

        mPagesMap.put(R.string.report_diagnosis, new ProtocolPage(R.string.report_diagnosis, R.string.protocol_diagnosis_helptext, createMSLView()
                .addItem(getResources().getString(R.string.report_diagnosis_cardial), 1)
                .addItem(getResources().getString(R.string.report_diagnosis_trauma), 2)
                .addItem(getResources().getString(R.string.report_diagnosis_drowning), 3)
                .addItem(getResources().getString(R.string.report_diagnosis_hypoxia), 4)
                .addItem(getResources().getString(R.string.report_diagnosis_intoxication), 5)
                .addItem(getResources().getString(R.string.report_diagnosis_isbsab), 6)
                .addItem(getResources().getString(R.string.report_diagnosis_sudden_death), 7)
                .addItem(getResources().getString(R.string.report_diagnosis_bleed), 8)
                .addItem(getResources().getString(R.string.report_diagnosis_stroke), 9)
                .addItem(getResources().getString(R.string.report_diagnosis_metabolic), 10)
                .addItem(getResources().getString(R.string.report_diagnosis_others), 11)
                .addItem(getResources().getString(R.string.report_diagnosis_sepsis), 12)
                .addItem(getResources().getString(R.string.protocol_unknown), 99)
                .create(12, true)));

        mPagesMap.put(R.string.protocol_orientation, new ProtocolPage(R.string.protocol_orientation, R.string.protocol_orientation_helptext, createMSLView()
                .addItem(getResources().getString(R.string.report_orientation_normal), 1)
                .addItem(getResources().getString(R.string.report_orientation_limited), 2)
                .addItem(getResources().getString(R.string.disoriented), 3)
                .addItem(getResources().getString(R.string.report_conscious_unconscious), 4)
                .addItem(getResources().getString(R.string.report_orientation_unknown), 99)
                .create(3, false)));

        mPagesMap.put(R.string.report_conscious, new ProtocolPage(R.string.report_conscious, R.string.protocol_conscious_helptext, createMSLView()
                .addItem(getResources().getString(R.string.not_documented), 0)
                .addItem(getResources().getString(R.string.report_conscious_narcotized), 1)
                .addItem(getResources().getString(R.string.report_conscious_awake), 2)
                .addItem(getResources().getString(R.string.report_conscious_responds_speech), 3)
                .addItem(getResources().getString(R.string.report_conscious_responds_pain), 4)
                .addItem(getResources().getString(R.string.report_speech_unconscious), 5)
                .addItem(getResources().getString(R.string.report_conscious_sleepy), 6)
                .addItem(getResources().getString(R.string.report_conscious_dead), 7)
                .addItem(getResources().getString(R.string.protocol_unknown), 99)
                .create(0, true)));


        mPagesMap.put(R.string.protocol_breathing, new ProtocolPage(R.string.protocol_breathing, R.string.protocol_breathing_helptext, createMSLView()
                .addItem(getResources().getString(R.string.not_documented), 0) // previously -1, in excel file not documented is always 0
                .addItem(getResources().getString(R.string.report_breathing_gasping), 8)
                .addItem(getResources().getString(R.string.report_breathing_apnea), 9)
                .addItem(getResources().getString(R.string.report_breathing_respiration), 10)
                .addItem(getResources().getString(R.string.report_breathing_normal), 11)
                .addItem(getResources().getString(R.string.report_breathing_not_assessable), 99)
                .create(0, true)));

        mPagesMap.put(R.string.protocol_sports_title, new ProtocolPage(R.string.protocol_sports_title, R.string.protocol_sport_helptext, createMSLView()
                .addItem(getResources().getString(R.string.no), false)
                .addItem(getResources().getString(R.string.yes), true)
                .create(0, false)));

        mPagesMap.put(R.string.protocol_collapse_title, new ProtocolPage(R.string.protocol_collapse_title, R.string.protocol_collapse_helptext, createMSLView()
                .addItem(getResources().getString(R.string.protocol_witness), 1)
                .addItem(getResources().getString(R.string.protocol_firstresponder), 2)
                .addItem(getResources().getString(R.string.protocol_notobserved), 3)
                .addItem(getResources().getString(R.string.relative), 4)
                .addItem(getResources().getString(R.string.protocol_unknown), 99)
                .create(4, false)));

        mPagesMap.put(R.string.protocol_cardiac_title, new ProtocolPage(R.string.protocol_cardiac_title, R.string.protocol_cardiac_helptext, createMSLView()
                .addItem(getResources().getString(R.string.protocol_witness), 1)
                .addItem(getResources().getString(R.string.protocol_firstresponder), 2)
                .addItem(getResources().getString(R.string.report_cardiac_not), 3)
                .addItem(getResources().getString(R.string.relative), 4)
                .addItem(getResources().getString(R.string.no_statement_patient_not_resusciated), 98)
                .create(4, false)));

        mPagesMap.put(R.string.protocol_ventilation_title, new ProtocolPage(R.string.protocol_ventilation_title, R.string.protocol_ventilation_helptext, createMSLView()
                .addItem(getResources().getString(R.string.protocol_witness), 1)
                .addItem(getResources().getString(R.string.protocol_firstresponder), 2)
                .addItem(getResources().getString(R.string.report_cardiac_not), 3)
                .addItem(getResources().getString(R.string.relative), 4)
                .addItem(getResources().getString(R.string.no_statement_patient_not_resusciated), 98)
                .create(4, false)));

        mPagesMap.put(R.string.protocol_telemedicine_title, new ProtocolPage(R.string.protocol_telemedicine_title, R.string.protocol_telemedicine_helptext, createMSLView()
                .addItem(getResources().getString(R.string.no), false)
                .addItem(getResources().getString(R.string.yes), true)
                .create(0, false)));

        mPagesMap.put(R.string.protocol_defibrillator_title, new ProtocolPage(R.string.protocol_defibrillator_title, R.string.protocol_defibrillator_helptext, createMSLView()
                .addItem(getResources().getString(R.string.protocol_witness), 1)
                .addItem(getResources().getString(R.string.protocol_firstresponder), 2)
                .addItem(getResources().getString(R.string.report_cardiac_not), 3)
                .addItem(getResources().getString(R.string.relative), 4)
                .addItem(getResources().getString(R.string.ambulance_service), 5)
                .addItem(getResources().getString(R.string.no_statement_patient_not_resusciated), 98)
                .create(5, false)));

        mPagesMap.put(R.string.protocol_aedshocks_title, new ProtocolPage(R.string.protocol_aedshocks_title, R.string.protocol_aedshocks_helptext, createMSLView()
                .addItem(getResources().getString(R.string.protocol_aedshocks_notspecified), 0)
                .addItem(getResources().getString(R.string.protocol_aedshocks_1), 1)
                .addItem(getResources().getString(R.string.protocol_aedshocks_upto3), 2)
                .addItem(getResources().getString(R.string.protocol_aedshocks_4to6), 3)
                .addItem(getResources().getString(R.string.protocol_aedshocks_7to9), 4)
                .addItem(getResources().getString(R.string.protocol_aedshocks_more), 5)
                .create(0, false)));


        MultitypeSelectionListView msvDefiManufacturers = createMSLView();
        String[] manufacturers = getResources().getStringArray(R.array.defibrillator_array);
        for (int index = 0; index < manufacturers.length; index++) {
            msvDefiManufacturers.addItem(manufacturers[index], (index < manufacturers.length - 1) ? index : 99);
        }
        mPagesMap.put(R.string.protocol_manufacturer_title, new ProtocolPage(R.string.protocol_manufacturer_title, R.string.protocol_aedmanufacturer_helptext, msvDefiManufacturers
                .create(0, false)));

        mPagesMap.put(R.string.protocol_publicaed_title, new ProtocolPage(R.string.protocol_publicaed_title, R.string.protocol_publicaed_helptext, createMSLView()
                .addItem(getResources().getString(R.string.protocol_publicaed_private), false)
                .addItem(getResources().getString(R.string.protocol_publicaed_public), true)
                .create(0, false)));

        mPagesMap.put(R.string.report_conscious_end_title, new ProtocolPage(R.string.report_conscious_end_title, R.string.protocol_conscious_end_helptext, createMSLView()
                .addItem(getResources().getString(R.string.not_documented), 0)
                .addItem(getResources().getString(R.string.report_conscious_narcotized), 1)
                .addItem(getResources().getString(R.string.report_conscious_awake), 2)
                .addItem(getResources().getString(R.string.report_conscious_responds_speech), 3)
                .addItem(getResources().getString(R.string.report_conscious_responds_pain), 4)
                .addItem(getResources().getString(R.string.report_speech_unconscious), 5)
                .addItem(getResources().getString(R.string.report_conscious_sleepy), 6)
                .addItem(getResources().getString(R.string.report_conscious_dead), 7)
                .addItem(getResources().getString(R.string.protocol_unknown), 99)
                .create(0, true)));

        mPagesMap.put(R.string.report_cardiovascular_title, new ProtocolPage(R.string.report_cardiovascular_title, R.string.protocol_cardiovascular_helptext, createMSLView()
                .addItem(getResources().getString(R.string.not_documented), 0)
                .addItem(getResources().getString(R.string.report_cardiovascular_running), 1)
                .addItem(getResources().getString(R.string.report_cardiovascular_normal), 2)
                .addItem(getResources().getString(R.string.report_cardiovascular_conscious), 3)
                .addItem(getResources().getString(R.string.report_cardiovascular_died), 4)
                .addItem(getResources().getString(R.string.unknown), 99)
                .create(0, true)));

        mPagesMap.put(R.string.protocol_remarks, new ProtocolPage(R.string.protocol_remarks, R.string.protocol_comment_helptext, findViewById(R.id.pageFreeText)));

        mPagesMap.put(R.string.cancel_reason, new ProtocolPage(R.string.cancel_reason, R.string.protocol_comment_helptext, findViewById(R.id.pageFreeText)));
    }


    public void showDatePicker(View view) {

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view1, year1, monthOfYear, dayOfMonth) -> ((TextView)findViewById(R.id.selectedDate)).setText(String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year1)), year, month, day);
        datePickerDialog.getDatePicker().setMinDate(mEmergencyState.getDate("acceptedAt").getTime());
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    public void showTimePicker(View view) {

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        int currentHour = c.get(Calendar.HOUR_OF_DAY);
        int currentMinute= c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog (this,
                (TimePicker timePickerView, int hourOfDay, int minute) -> ((TextView)findViewById(R.id.selectedTime)).setText(String.format("%02d:%02d", hourOfDay, minute)), currentHour, currentMinute, true);
        timePickerDialog.show();
    }

    Object getValue(ProtocolPage page) {
        if (page.mView instanceof MultitypeSelectionListView) {
            MultitypeSelectionListView view = (MultitypeSelectionListView) page.mView;
            if (view.isMultiselectMode()) {
                return new ArrayList(view.getValues());
            } else {
                return view.getValue();
            }
        } else if(page.mView.getId() == R.id.pageArrivedAt) {
            return ((TextView)findViewById(R.id.selectedDate)).getText();
        } else if(page.mView.getId() == R.id.pageFreeText) {
            return ((MultiLineEditTextWithDone)findViewById(R.id.longtext)).getText().toString();
        }
        return null;
    }

    Object getSecondaryValue(ProtocolPage page) {
        if (page.mView instanceof MultitypeSelectionListView) {
            MultitypeSelectionListView view = (MultitypeSelectionListView) page.mView;
            if (!view.isMultiselectMode()) {
                return view.getSecondaryValue();
            }
        } else if(page.mView.getId() == R.id.pageArrivedAt) {
            return ((TextView)findViewById(R.id.selectedTime)).getText();
        }
        return null;
    }

    public void saveTemporary(View sourceView) {
        try {
            JSONObject data = new JSONObject();
            for (int pageId : mVisiblePages) {
                ProtocolPage page = mPagesMap.get(pageId);
                String key = Integer.toString(pageId);
                Object value = getValue(page);
                if(value != null) {
                    if(value instanceof Collection) {
                        data.put(key, new JSONArray((Collection) value));
                    } else {
                        data.put(key, value);
                    }
                }

                Object secondaryValue = getSecondaryValue(page);
                if(secondaryValue != null) {
                    data.put(key+"_secondary", secondaryValue);
                }
            }

            mSessionManager.saveProtocol(mEmergencyState.getObjectId(), data);
            mAlertDialog.show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void loadTemporary() {
        try {
            JSONObject data = mSessionManager.getProtocol(mEmergencyState.getObjectId());
            if(data != null) {
                Iterator it = data.keys();
                while(it.hasNext()) {
                    String key = (String) it.next();
                    int indexOfSecondary = key.indexOf('_');
                    boolean isSecondary = indexOfSecondary >= 0;
                    String pageId = (!isSecondary) ? key : key.substring(0, indexOfSecondary);
                    ProtocolPage page = mPagesMap.get(Integer.valueOf(pageId));
                    if(page != null) {
                        if (page.mView instanceof MultitypeSelectionListView) {
                            MultitypeSelectionListView view = (MultitypeSelectionListView) page.mView;
                            if (view.isMultiselectMode()) {
                                Set<Object> values = new LinkedHashSet<>();
                                JSONArray jArr = data.getJSONArray(key);
                                for(int i = 0; i < jArr.length();++i) {
                                    values.add(jArr.get(i));
                                }
                                view.setMultiSelectValues(values);
                            } else {
                                if(!isSecondary) {
                                    view.setValue(data.get(key));
                                } else {
                                    view.setSecondaryValue(data.get(key));
                                }
                            }
                        } else if(page.mView.getId() == R.id.pageArrivedAt) {
                            if(!isSecondary) {
                                ((TextView)findViewById(R.id.selectedDate)).setText(data.getString(key));
                            } else {
                                ((TextView)findViewById(R.id.selectedTime)).setText(data.getString(key));
                            }
                        } else if(page.mView.getId() == R.id.pageFreeText) {
                            ((TextView)findViewById(R.id.longtext)).setText(data.getString(key));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createProtocol(View view) {

        if(mVisiblePages.contains(R.string.protocol_absolute_arrival_time)) {
            try {
                ProtocolPage page = mPagesMap.get(R.string.protocol_absolute_arrival_time);
                mEmergencyState.put("arrivedAt", new SimpleDateFormat("dd/MM/yyyy hh:mm").parse(getValue(page ) + " " + getSecondaryValue(page )));
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
        }

        final ParseObject protocol = new ParseObject("Protocol");
        protocol.put("done", true);

        // object.put("protocolRelation", newReport);
        for (int pageId : mVisiblePages) {
            String primaryKey = PAGE_TO_PARSE_MAP_PRIMARY.get(pageId);
            if(primaryKey  != null && !primaryKey.isEmpty()) {
                Object value = getValue(mPagesMap.get(pageId));
                if(value != null) {
                    protocol.put(primaryKey, value);
                }
            }

            String secondaryKey = PAGE_TO_PARSE_MAP_SECONDARY.get(pageId);
            if(secondaryKey != null && !secondaryKey.isEmpty()) {
                Object value = getSecondaryValue(mPagesMap.get(pageId));
                if(value != null) {
                    protocol.put(secondaryKey, value);
                }
            }
        }

        try {
            protocol.save();
            protocol.setACL(getACLPublicAccess());
            mEmergencyState.put("protocolRelation", protocol);
            mEmergencyState.save();
        }catch (ParseException e) {
            e.printStackTrace();
            saveTemporary(view);
        }
        startActivity(new Intent(this, ProtocolFinishedActivity.class));
        finish();
    }

    public ParseACL getACLPublicAccess() {
        ParseACL acl = new ParseACL();
        acl.setPublicWriteAccess(true);
        acl.setPublicReadAccess(true);
        return acl;
    }

    public void finishActivity(View view) {
        mAlertDialog.dismiss();
        finish();
    }

    public void closeAlert(View view) {
        mAlertDialog.hide();
    }
}