package com.roni.haim.nearme;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.dd.processbutton.iml.ActionProcessButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;


public class SignUpActivity extends Activity {

    private EditText userID;
    private EditText userPass;
    private EditText userPassConfirm;
    private EditText userFullName;

    private TextView userDetails;
    private TextView interestsLabel;
    private TextView radiusLabel;
    private TextView settings_label;
    //private TextView login;
    private TextView radius;
    private ActionProcessButton buttonCreateAccount;
    private SeekBar radiusSeekBar;
    private String interests_selected;
    private LinearLayout sign_up_layout;
    private Spinner interests;
    private Button b;
    private boolean ans = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.gc();
        setContentView(R.layout.activity_sign_up);
        userFullName = (EditText)findViewById(R.id.editTextFullName);
        userID = (EditText)findViewById(R.id.editTextUserName);
        userPass = (EditText)findViewById(R.id.editTextPassword);
        userPassConfirm = (EditText)findViewById(R.id.editTextConfirmPassword);
        userDetails = (TextView) findViewById(R.id.editDetails);
        interestsLabel = (TextView) findViewById(R.id.interestsLabel);
        radiusLabel = (TextView) findViewById(R.id.radiusLabel);
        settings_label = (TextView) findViewById(R.id.settings_label);
        radiusSeekBar = (SeekBar) findViewById(R.id.radiusSeekBar);
        radius = (TextView) findViewById(R.id.radius);
        interests = (Spinner)findViewById(R.id.interests);
        sign_up_layout = (LinearLayout)findViewById(R.id.sign_up_layout);
        buttonCreateAccount = (ActionProcessButton) findViewById(R.id.buttonCreateAccount);

        Typeface mTypeface = Typeface.createFromAsset(getAssets(), "lobster.otf");
        userFullName.setTypeface(mTypeface);
        userID.setTypeface(mTypeface);
        userPass.setTypeface(mTypeface);
        userPassConfirm.setTypeface(mTypeface);
        userDetails.setTypeface(mTypeface);
        interestsLabel.setTypeface(mTypeface);
        radiusLabel.setTypeface(mTypeface);
        settings_label.setTypeface(mTypeface);
        radius.setTypeface(mTypeface);
        buttonCreateAccount.setTypeface(mTypeface);

        //mSignIn.setMode(ActionProcessButton.Mode.PROGRESS);
        buttonCreateAccount.setMode(ActionProcessButton.Mode.ENDLESS);
        // set progress > 0 to start progress indicator animation
        buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                buttonCreateAccount.setProgress(1);
                buttonCreateAccount.setEnabled(false);
                sign_up_layout.setEnabled(false);

                ans = true;
                userID.setError(null);
                userPass.setError(null);
                userPassConfirm.setError(null);
                userFullName.setError(null);
                String userName = userFullName.getText().toString();
                String user = userID.getText().toString();
                String pass = userPass.getText().toString();
                String passConfirm = userPassConfirm.getText().toString();
                if(userName.equals(""))
                {
                    userFullName.setError("You haven't enter a name");
                    ans = false;
                }
                if (!pass.equals(passConfirm))
                {
                    userPassConfirm.setError("the passwords are not equals");
                    ans = false;
                }
                check(user, pass);
                interests_selected = ((MyCustomAdapter)interests.getAdapter()).getSelectedInterests();
                if(interests_selected.equals(""))
                {
                    ans =  false;
                    AlertDialog.Builder builder = new AlertDialog.Builder(getSignUpClass());
                    builder.setTitle("Please select at least one interest");
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            return;
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                if(ans)
                    new DBHandler(user, "get_user", null, "getUser", getSignUpClass()).execute();
                else
                {
                    buttonCreateAccount.setProgress(0);
                    buttonCreateAccount.setEnabled(true);
                    sign_up_layout.setEnabled(true);
                }
            }
        });

        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                radius.setText(String.valueOf(progresValue+1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        String[] items = new String[]{"Alcohol", "Animals", "Art", "Business", "Cinema", "Food", "Music", "Night Life", "Sport", "Theater"};
        ArrayList<String> values = new ArrayList<>(Arrays.asList(items));
        MyCustomAdapter adapter = new MyCustomAdapter(this, values);
        interests.setAdapter(adapter);

        /*
       login.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent intent = new Intent(getBaseContext(), LoginActivity.class);
               startActivity(intent);
               finish();
           }
       });
       */
    }

    public SignUpActivity getSignUpClass()
    {
        return SignUpActivity.this;
    }

    @Override
    protected void onDestroy() {
        sign_up_layout.setBackgroundResource(0);
        System.gc();
        super.onDestroy();
    }

    public void getUser(JSONArray jsonArray) {

        if (jsonArray == null) {
                Hashtable<String,String> params = new Hashtable<>();
                params.put("name",userFullName.getText().toString());
                params.put("ID",userID.getText().toString());
                params.put("pass",BCrypt.hashpw(userPass.getText().toString(), BCrypt.gensalt()));
                new DBHandler(userID.getText().toString(),"set_user",params,"setUser",this).execute();
        }
        else {
            buttonCreateAccount.setProgress(0);
            buttonCreateAccount.setEnabled(true);
            sign_up_layout.setEnabled(true);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("USER EXISTS");
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    return;
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void setUser(JSONArray jsonArray) {
        for(int i=0; i<jsonArray.length();i++) {
            JSONObject json = null;
            try {
                json = jsonArray.getJSONObject(i);
                String result = json.getString("result");
                if (result.equals("success")) {
                    Hashtable<String,String> params = new Hashtable<>();
                    params.put("ID",userID.getText().toString());
                    params.put("interests",interests_selected);
                    params.put("radius",radius.getText().toString());
                    new DBHandler(userID.getText().toString(),"set_settings",params,"setSettings",this).execute();
                }
                else
                {
                    buttonCreateAccount.setProgress(0);
                    buttonCreateAccount.setEnabled(true);
                    sign_up_layout.setEnabled(true);
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("COULDN'T CREATE AN ACCOUNT");
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            return;
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void setSettings(JSONArray jsonArray) {
        for(int i=0; i<jsonArray.length();i++) {
            JSONObject json = null;
            try {
                json = jsonArray.getJSONObject(i);
                String result = json.getString("result");
                if (result.equals("success")) {
                    buttonCreateAccount.setProgress(100);
                    finish();
                    System.gc();
                    Intent intent = new Intent(this, FeedActivity.class);
                    intent.putExtra("USER_ID", userID.getText().toString());
                    startActivity(intent);
                }
                else
                {
                    buttonCreateAccount.setProgress(0);
                    buttonCreateAccount.setEnabled(true);
                    sign_up_layout.setEnabled(true);
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("COULDN'T SET SETTINGS");
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            return;
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void check(String id,String pass)
    {

        if (TextUtils.isEmpty(pass) || !isPasswordValid(pass)) {
            userPass.setError(getString(R.string.error_invalid_password));
            ans = false;
        }
        if(TextUtils.isEmpty(id) || !isEmailValid(id)) {
            userID.setError(getString(R.string.error_invalid_email));
            ans = false;
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 5;
    }

    private class MyCustomAdapter extends ArrayAdapter<String> {

        Context context;
        ArrayList<String> list;
        ArrayList<Boolean> boolSelect;
        private int defaultPosition;

        public int getDefaultPosition() {
            return defaultPosition;
        }

        public String getSelectedInterests()
        {
            boolean wasSelected = false;
            String selected = "";
            for(int i = 0 ; i< boolSelect.size() ; i++) {
                if (boolSelect.get(i)) {
                    if(wasSelected)
                        selected += "," + list.get(i);
                    else
                    {
                        wasSelected = true;
                        selected += list.get(i);
                    }
                }
            }
            return selected;
        }

        public MyCustomAdapter(Context context, ArrayList<String> objects) {
            super(context, 0, objects);
            this.context = context;
            list = objects;
            boolSelect = new ArrayList<>();
            for (int i = 0 ; i < list.size() ; i++)
                boolSelect.add(false);
        }

        public void setDefaultPostion(int position) {
            this.defaultPosition = position;
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustom(position, convertView, parent);
        }

        public View getCustom(int position, View convertView, ViewGroup parent) {

            View row = LayoutInflater.from(context).inflate(
                    R.layout.interests_item_signup, parent, false);

            TextView interestName = (TextView) row.findViewById(R.id.interestName);
            Typeface tf = Typeface.createFromAsset(context.getAssets(), "lobster.otf");
            interestName.setTypeface(tf);

            interestName.setText(list.get(position));

            return row;
        }

        public View getCustomView(final int position, View convertView,
                                  ViewGroup parent) {

            View row = LayoutInflater.from(context).inflate(
                    R.layout.interests_item, parent, false);

            final TextView interestName = (TextView) row.findViewById(R.id.interestName);
            final TextView interestColor = (TextView) row.findViewById(R.id.interestColor);
            final RelativeLayout iView = (RelativeLayout) row.findViewById(R.id.iView);
            Typeface tf = Typeface.createFromAsset(context.getAssets(), "lobster.otf");
            interestName.setTypeface(tf);
            interestName.setText(list.get(position));
            String color = "";
            switch (list.get(position)) {
                case "Music":
                    color = "#DF79C37D";
                    break;
                case "Sport":
                    color = "#DF61C7F2";
                    break;
                case "Alcohol":
                    color = "#DFBF5CA3";
                    break;
                case "Animals":
                    color = "#DFEF575C";
                    break;
                case "Art":
                    color = "#DFFAB15B";
                    break;
                case "Business":
                    color = "#DFFCF06B";
                    break;
                case "Cinema":
                    color = "#DFC12026";
                    break;
                case "Food":
                    color = "#DF00589C";
                    break;
                case "Night Life":
                    color = "#DF623894";
                    break;
                case "Theater":
                    color = "#DFE68B24";
                    break;
                default:
                    break;
            }
            interestColor.setBackgroundColor(Color.parseColor(color));

            if(boolSelect.get(position))
            {
                iView.setBackgroundColor(Color.parseColor(color));
                interestName.setTextColor(Color.WHITE);
            }

            final String finalColor = color;

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!boolSelect.get(position)) {
                        boolSelect.set(position, true);
                        iView.setBackgroundColor(Color.parseColor(finalColor));
                        interestName.setTextColor(Color.WHITE);
                    } else {
                        boolSelect.set(position, false);
                        iView.setBackgroundColor(Color.WHITE);
                        interestName.setTextColor(Color.BLACK);
                    }
                }
            });

            return row;
        }
    }
}
