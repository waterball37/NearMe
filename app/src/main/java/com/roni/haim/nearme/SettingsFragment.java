package com.roni.haim.nearme;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.dd.processbutton.iml.ActionProcessButton;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    private TextView radius;
    private String interests_selected;
    private Spinner interests;
    private TextView settingsLbl;
    private ActionProcessButton buttonSaveSettings;
    private RelativeLayout settings;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View frag = inflater.inflate(R.layout.fragment_settings, container, false);



        final TextView interestsLabel = (TextView) frag.findViewById(R.id.interestsLabel);
        final TextView radiusLabel = (TextView) frag.findViewById(R.id.radiusLabel);
        SeekBar radiusSeekBar = (SeekBar) frag.findViewById(R.id.radiusSeekBar);
        radius = (TextView) frag.findViewById(R.id.radius);
        interests = (Spinner) frag.findViewById(R.id.interests);
        settingsLbl = (TextView) frag.findViewById(R.id.settingsLbl);
        buttonSaveSettings = (ActionProcessButton) frag.findViewById(R.id.buttonSaveSettings);
        buttonSaveSettings.setMode(ActionProcessButton.Mode.ENDLESS);
        settings = (RelativeLayout)  frag.findViewById(R.id.settings);
        final Typeface mTypeface = Typeface.createFromAsset(getActivity().getAssets(), "lobster.otf");

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                interestsLabel.setTypeface(mTypeface);
                radiusLabel.setTypeface(mTypeface);
                radius.setTypeface(mTypeface);
                settingsLbl.setTypeface(mTypeface);
                buttonSaveSettings.setTypeface(mTypeface);
            }
        });

        int progressDB = ((FeedActivity) getActivity()).getUserRadius();
        radius.setText(String.valueOf(progressDB));
        radiusSeekBar.setProgress(progressDB);
        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                radius.setText(String.valueOf(progresValue + 1));
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
        MyCustomAdapter adapter = new MyCustomAdapter(getActivity(), values);
        interests.setAdapter(adapter);

        buttonSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean ans = true;
                buttonSaveSettings.setProgress(1);
                buttonSaveSettings.setEnabled(false);
                settings.setEnabled(false);
                interests_selected = ((MyCustomAdapter) interests.getAdapter()).getSelectedInterests();
                if (interests_selected.equals("")) {
                    ans = false;
                    ((FeedActivity) getActivity()).toast("Please select at least one interest");
                    buttonSaveSettings.setProgress(0);
                    buttonSaveSettings.setEnabled(true);
                    settings.setEnabled(true);
                }
                if (ans) {
                    Hashtable<String, String> params = new Hashtable<>();
                    params.put("interests", interests_selected);
                    params.put("radius", radius.getText().toString());
                    ((FeedActivity) getActivity()).setSpinner();
                    new DBHandler(((FeedActivity) getActivity()).getUser(), "set_user_settings", params, "setUserSettings", ((FeedActivity) getActivity()).getFeedClass()).execute();
                }
            }
        });

        return frag;
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

        public boolean useLoop(String[] arr, String targetValue) {
            for(String s: arr){
                if(s.equals(targetValue))
                    return true;
            }
            return false;
        }

        public MyCustomAdapter(Context context, ArrayList<String> objects) {
            super(context, 0, objects);
            this.context = context;
            list = objects;
            boolSelect = new ArrayList<>();
            String[] intrests = ((FeedActivity) getActivity()).getUserInterests().split(",");
            for (int i = 0 ; i < list.size() ; i++) {
                if(useLoop(intrests,list.get(i)))
                    boolSelect.add(true);
                else
                    boolSelect.add(false);
            }
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
