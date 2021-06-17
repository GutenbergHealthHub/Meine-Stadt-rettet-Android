package com.aurel.ecorescue.ui.account.agreement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.adapter.SubContractsAdapter;
import com.aurel.ecorescue.model.SubContract;
import com.aurel.ecorescue.profile.ProfileData;
import com.aurel.ecorescue.utils.AppExecutors;
import com.aurel.ecorescue.utils.StyleUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import timber.log.Timber;


public class AgreementFragment extends Fragment {

    public AgreementFragment() {}

    private NavController navController;

    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_agreement, container, false);
    }

    public void onViewCreated(@NotNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        navController = Navigation.findNavController(view);
        toolbar.setNavigationOnClickListener(v -> navController.navigateUp());
        if (getContext() != null) {
            toolbar.setNavigationIcon(StyleUtils.getHomeAsUpIndicator(getContext()));
        }

        ProfileData profileData = new ProfileData().createFromCurrentUser();

        ImageView basicAgreementStatus = view.findViewById(R.id.iv_agreement_status);
        boolean bas = profileData.getContractSigned();
        if (bas) {
            basicAgreementStatus.setImageResource(R.drawable.ic_check_circle_white_24dp);
            basicAgreementStatus.setColorFilter(view.getContext().getResources().getColor(R.color.md_green_700));
        } else {
            basicAgreementStatus.setImageResource(R.drawable.ic_cancel_white_24dp);
            basicAgreementStatus.setColorFilter(view.getContext().getResources().getColor(R.color.md_red_700));
        }

        view.findViewById(R.id.rl_basic_agreement).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("agreement", "basic");
            bundle.putBoolean("isSigned", bas);
            navController.navigate(R.id.agreementSignFragment, bundle);
        });

        RecyclerView rv = view.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        SubContractsAdapter adapter = new SubContractsAdapter(getContext(), navController);
        rv.setAdapter(adapter);


        ParseQuery<ParseObject> query = ParseQuery.getQuery("ContractSub");
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.whereGreaterThanOrEqualTo("validUntil", Calendar.getInstance().getTime());
        query.whereLessThanOrEqualTo("validFrom", Calendar.getInstance().getTime());
//        query.whereEqualTo("activated", true);
        AppExecutors appExecutors = new AppExecutors();
        View additionalAgreementContainer = view.findViewById(R.id.ll_additional_agreement_container);
        query.findInBackground((list, e) -> {
            if (e == null) {
                List<SubContract> items = new ArrayList<>();
                if (list.size()>0) {
                    Timber.d("Contracts loaded: %s", list.size());
                    additionalAgreementContainer.setVisibility(View.VISIBLE);
                    appExecutors.networkIO().execute(() -> {
                        for (ParseObject object: list){
                            SubContract item = new SubContract();
                            item.setObjectId(object.getObjectId());
                            item.setControlCenter(object.getParseObject("controlCenter").getObjectId());
                            item.setTitle(object.getString("title"));
                            item.setSubtitle(object.getString("subtitle"));
                            item.setUrl(object.getString("url"));
                            item.setVersion(object.getString("version"));
                            item.setValidFrom(object.getDate("validFrom"));
                            item.setValidUntil(object.getDate("validUntil"));
                            ParseQuery<ParseObject> query2 = ParseQuery.getQuery("UserContract");
                            query2.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);
                            query2.whereEqualTo("contract", object);
                            query2.whereEqualTo("user", ParseUser.getCurrentUser());
                            try {
                                item.setState(query2.count()); // TODO: Set state
                            } catch (ParseException ex) {
//                                ex.printStackTrace();
                                Timber.d(ex, "ParseCount error");
                            }
                            Timber.d("Got: %s", item);
                            items.add(item);
                        }
                        appExecutors.mainThread().execute(() -> adapter.setItems(items));
                    });
                } else {
                    additionalAgreementContainer.setVisibility(View.GONE);
                }


            } else {
                Timber.d("ParseException: " + e.getCode() + " " + e.getLocalizedMessage() + " error:" + e + " msg:" + e.getMessage());
            }
        });
    }


}
