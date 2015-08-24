package com.aptitekk.binghamapp.SkywardFragmentClasses;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;

import com.aptitekk.binghamapp.MainActivity;
import com.aptitekk.binghamapp.R;
import com.aptitekk.binghamapp.Utilities.HttpHandler;
import com.rengwuxian.materialedittext.MaterialEditText;

public class SkywardFragment extends Fragment implements View.OnClickListener, PINEditText.PINListener, HttpHandler.HttpHandlerListener, MainActivity.BackButtonListener {

    private RelativeLayout usernamePasswordLayout;
    private MaterialEditText usernameEditText;
    private MaterialEditText passwordEditText;
    private CheckBox rememberDetailsCheckBox;

    private RelativeLayout rememberLayout;
    private PINEditText[] PINEditTexts1;
    private PINEditText[] PINEditTexts2;
    private Button loginButton;

    public SkywardFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_skyward, container, false);

        //TODO: Determine if user has saved their login details or not

        //Define variables
        this.usernamePasswordLayout = (RelativeLayout) view.findViewById(R.id.usernamePasswordLayout);
        this.usernameEditText = (MaterialEditText) view.findViewById(R.id.usernameEditText);
        this.usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkValidity();
            }
        });

        this.passwordEditText = (MaterialEditText) view.findViewById(R.id.passwordEditText);
        this.passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkValidity();
            }
        });

        this.rememberDetailsCheckBox = (CheckBox) view.findViewById(R.id.rememberCheckBox);
        this.rememberDetailsCheckBox.setOnClickListener(this);

        this.rememberLayout = (RelativeLayout) view.findViewById(R.id.rememberLayout);
        this.PINEditTexts1 = new PINEditText[]{
                (PINEditText) view.findViewById(R.id.PINEditText1),
                (PINEditText) view.findViewById(R.id.PINEditText2),
                (PINEditText) view.findViewById(R.id.PINEditText3),
                (PINEditText) view.findViewById(R.id.PINEditText4),
        };

        this.PINEditTexts2 = new PINEditText[]{
                (PINEditText) view.findViewById(R.id.PINEditText1V),
                (PINEditText) view.findViewById(R.id.PINEditText2V),
                (PINEditText) view.findViewById(R.id.PINEditText3V),
                (PINEditText) view.findViewById(R.id.PINEditText4V),
        };

        for (int i = 0; i < PINEditTexts1.length; i++) {
            PINEditTexts1[i].addTextChangedListener(new PINTextWatcher(PINEditTexts1, i, PINEditTexts2));
            PINEditTexts1[i].setPINListener(this, PINEditTexts1, i);
        }
        for (int i = 0; i < PINEditTexts2.length; i++) {
            PINEditTexts2[i].addTextChangedListener(new PINTextWatcher(PINEditTexts2, i, PINEditTexts1));
            PINEditTexts2[i].setPINListener(this, PINEditTexts2, i);
        }

        this.loginButton = (Button) view.findViewById(R.id.loginButton);
        this.loginButton.setOnClickListener(this);

        //Hide, disable certain views
        this.rememberDetailsCheckBox.setEnabled(false);
        this.loginButton.setEnabled(false);

        this.rememberLayout.setVisibility(View.GONE);

        //Add back button listener
        ((MainActivity) getActivity()).addBackButtonListener(this);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        /*//TODO: Notify if skyward can't be loaded
        //TODO: Automatically log user in and skip all the bull poop
        WebViewFragment webViewFragment = new WebViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString("URL", "https://skystu.jordan.k12.ut.us/scripts/wsisa.dll/WService=wsEAplus/mobilelogin.w");
        //bundle.putString("POSTData", "login="+username+"&password="+password+"&autologin=true&hideui=true&mobiledevice=android&version=1.20");
        bundle.putBoolean("useJavaScript", true);
        webViewFragment.setArguments(bundle);

        getFragmentManager().beginTransaction().add(R.id.fragmentSpace, webViewFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).addToBackStack(null).commit();*/
    }

    @Override
    public void onClick(View v) {
        if (v.equals(this.loginButton)) {
            /*HashMap<String, String> postData = new HashMap<>();
            postData.put("login", usernameEditText.getText().toString());
            postData.put("password", passwordEditText.getText().toString());
            postData.put("autologin", "true");
            postData.put("hideui", "true");
            postData.put("mobiledevice", "android");
            postData.put("version", "1.20");
            new HttpHandler(this, HttpHandler.HttpHandlerAction.POST, "https://skystu.jordan.k12.ut.us/scripts/wsisa.dll/WService=wsEAplus/mobilelogin.w", postData).execute();*/

            SkywardWebview skywardWebview = new SkywardWebview();

            Bundle bundle = new Bundle();
            bundle.putString("username", usernameEditText.getText().toString());
            bundle.putString("password", passwordEditText.getText().toString());
            skywardWebview.setArguments(bundle);

            getChildFragmentManager().beginTransaction().add(R.id.fragmentSpaceSkyward, skywardWebview).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).addToBackStack(null).commit();

            this.usernamePasswordLayout.setVisibility(View.GONE);
            this.rememberLayout.setVisibility(View.GONE);
            this.loginButton.setVisibility(View.GONE);
        } else
            checkValidity();
    }

    @Override
    public void onHttpTransactionComplete(String response) {
        Log.v(MainActivity.LOG_NAME, "Response: " + response);
    }

    @Override
    public void onPINPrevious(PINEditText currentPINEditText, PINEditText[] parentArray, int index) {
        if (index > 0) {
            parentArray[index - 1].requestFocus();
        }
    }

    private void checkValidity() {
        boolean loginEnabled = true;
        boolean rememberCheckBoxEnabled = true;

        if (this.usernameEditText.length() == 0 || this.passwordEditText.length() == 0) {
            loginEnabled = false;
            rememberCheckBoxEnabled = false;
        }

        if (this.rememberDetailsCheckBox.isChecked() && this.rememberDetailsCheckBox.isEnabled()) {
            for (PINEditText editText : PINEditTexts1) {
                if (editText.isInvalid() || editText.length() == 0) {
                    loginEnabled = false;
                    break;
                }
            }
            for (PINEditText editText : PINEditTexts2) {
                if (editText.isInvalid() || editText.length() == 0) {
                    loginEnabled = false;
                    break;
                }
            }
        }

        this.loginButton.setEnabled(loginEnabled);
        this.rememberDetailsCheckBox.setEnabled(rememberCheckBoxEnabled);
        rememberDetailsCheckBox.setChecked(rememberCheckBoxEnabled && rememberDetailsCheckBox.isChecked());
        this.rememberLayout.setVisibility((this.rememberDetailsCheckBox.isChecked()) ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onBackPressed() {
        boolean popped = getChildFragmentManager().popBackStackImmediate();
        if (popped) {
            this.usernamePasswordLayout.setVisibility(View.VISIBLE);
            this.rememberLayout.setVisibility(rememberDetailsCheckBox.isChecked() ? View.VISIBLE : View.GONE);
            this.loginButton.setVisibility(View.VISIBLE);
        }
        return !popped;
    }

    private class PINTextWatcher implements TextWatcher {

        private final PINEditText[] parentArray;
        private final int index;
        private final PINEditText[] verifyArray;

        public PINTextWatcher(PINEditText[] parentArray, int index, PINEditText[] verifyArray) {
            this.parentArray = parentArray;
            this.index = index;
            this.verifyArray = verifyArray;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (parentArray[index].length() > 0) {
                if (verifyArray != null) {
                    if (!parentArray[index].getText().toString().equals(verifyArray[index].getText().toString()) && verifyArray[index].length() > 0) {
                        parentArray[index].setInvalid(true);
                        verifyArray[index].setInvalid(true);
                        checkValidity();
                    } else {
                        parentArray[index].setInvalid(false);
                        verifyArray[index].setInvalid(false);
                        checkValidity();
                    }
                }
                if (index < parentArray.length - 1) {
                    parentArray[index + 1].requestFocus();
                }
            } else {
                if (verifyArray != null) {
                    parentArray[index].setInvalid(false);
                    verifyArray[index].setInvalid(false);
                    checkValidity();
                }
            }
        }
    }
}
