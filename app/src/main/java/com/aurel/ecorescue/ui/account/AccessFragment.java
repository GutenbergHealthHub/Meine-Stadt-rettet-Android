package com.aurel.ecorescue.ui.account;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.utils.NavigationDrawer;
import com.aurel.ecorescue.utils.StyleUtils;
import com.aurel.ecorescue.view.MainActivity;
import com.aurel.ecorescue.x_tools.x_EcoPreferences;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;


public class AccessFragment extends Fragment {

    public AccessFragment() {}

    private NavController navController;

    private EditText mEditTextPin;
    private ViewGroup mPinIndicator;
    private TextView mInstructions;
    private View view;

    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_access, container, false);
    }

    public void onViewCreated(@NotNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        navController = Navigation.findNavController(view);

        toolbar.setNavigationOnClickListener(v -> onNavigateBack());
        if (getContext() != null) {
            toolbar.setNavigationIcon(StyleUtils.getHomeAsUpIndicator(getContext()));
        }
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                Timber.d("onBackPressed::AccessFragment");
                onNavigateBack();
                // Handle the back button event
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        mInstructions = view.findViewById(R.id.instructions);
        SharedPreferences sharedPreferences = x_EcoPreferences.GetSharedPreferences(view.getContext());
        // 0 -> enter your new pin
        // 1 -> reenter your new pin
        // 2 -> enter your current pin
        int pinStatus = sharedPreferences.getInt("access_pin", 0);
        if (pinStatus==0) {
            mInstructions.setText(view.getContext().getResources().getString(R.string.please_enter_your_pin));
        } else if (pinStatus==1) {
            mInstructions.setText(view.getContext().getResources().getString(R.string.please_reenter_your_pin));
        } else {
            mInstructions.setText(view.getContext().getResources().getString(R.string.please_enter_your_current_pin));
        }
        ParseUser u = ParseUser.getCurrentUser();

        mPinIndicator = view.findViewById(R.id.pinIndicator);
        mEditTextPin = view.findViewById(R.id.pin_textview);
        mEditTextPin.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String pin = s.toString();
                for (int i = 0; i < mPinIndicator.getChildCount(); ++i) {
                    mPinIndicator.getChildAt(i).setBackground(getResources().getDrawable((i < pin.length()) ? R.drawable.msr_button_blue : R.drawable.msr_button_outline_blue));
                }
                if (pin.length() == 6) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (pinStatus == 0) {
                        Timber.d("Status: 0");
                        editor.putString("last_password", pin);
                        editor.putInt("access_pin", 1); // Save entered pin and Go to reenter
                        editor.apply();
                        // go to reenter
                        navController.navigate(R.id.action_accessFragment_self);
//                        navController.popBackStack(R.id.accessFragment, true);
                    } else if (pinStatus == 1) {
                        // If pin matches => Save pin & Go back to profile page (Toast it)
                        String last_pin = sharedPreferences.getString("last_password", "-1");
                        if (last_pin==null) last_pin = "-1";
                        if (last_pin.equals(pin)) {
                            Timber.d("Status: 1 True");
                            // Reentered pin matches with last pin
                            ParseUser.getCurrentUser().put("code", pin);
                            ParseUser.getCurrentUser().saveInBackground();
                            editor.putInt("access_pin", 2);
                            editor.apply();
                            showMessage(R.string.pin_saved);
//                            navController.popBackStack();
//                            navController.navigate(R.id.profileFragment);
                            onNavigateBack();
                        } else {
                            Timber.d("Status: 1 False");
                            // If pin doesn't match => Don't save pin & Go back to profile page
                            showMessage(R.string.pins_doesnt_match);
//                            navController.popBackStack();
//                            navController.navigate(R.id.profileFragment);
                            onNavigateBack();
                        }

                    } else {
                        String currentPin = u.getString("code");
                        if (currentPin==null) currentPin = "";
                        if (currentPin.equals(pin)) {
                            Timber.d("Status: 2 True");
                            // PINs match, continue
                            editor.putInt("access_pin", 0); // Go enter new new pin
                            editor.apply();
                            navController.navigate(R.id.action_accessFragment_self);
//                            navController.popBackStack(R.id.accessFragment, true);
                        } else {
                            Timber.d("Status: 2 False");
                            showMessage(R.string.pins_doesnt_match);
                            // PINs doesn't match, go back to profile page
//                            navController.popBackStack();
//                            navController.navigate(R.id.profileFragment);
                            onNavigateBack();
                        }

                    }
                    //navController.navigateUp();
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        mEditTextPin.setOnKeyListener((v, keyCode, event) -> keyCode == KeyEvent.KEYCODE_ENTER);

        mEditTextPin.setText("");
        mEditTextPin.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditTextPin, InputMethodManager.SHOW_IMPLICIT);

        view.findViewById(R.id.ll_container).setOnClickListener(v -> {
            mEditTextPin.requestFocus();
            InputMethodManager imm2 = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm2.showSoftInput(mEditTextPin, InputMethodManager.SHOW_IMPLICIT);
        });

    }

    private void onNavigateBack(){
        navController.popBackStack();
        navController.navigate(R.id.profileFragment);
    }



    private void showMessage(int id) {
        String message = view.getContext().getResources().getString(id);
        Toast.makeText(view.getContext().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}
