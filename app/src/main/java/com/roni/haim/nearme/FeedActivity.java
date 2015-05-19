package com.roni.haim.nearme;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.dd.processbutton.iml.ActionProcessButton;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.johnpersano.supertoasts.SuperToast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

public class FeedActivity extends Activity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener  {

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private Location mLastLocation;
    private HashMap<String,Marker> markers;
    private ArrayList<ArrayList<String>> events;
    private String user;
    private String userInterests;
    private int userRadius;
    private int componentsProcessed = 0;
    private ListView feed;
    private boolean blurApplied = false;
    private boolean fragmentBlurApplied = false;
    private boolean spinnerStarted = false;
    private Bitmap blurImage;
    private AnimationDrawable loadingViewAnim=null;
    private ImageView loadingIcon = null;
    private ImageView blur;
    private LinearLayout loadingLayout = null;
    private RelativeLayout layout;
    private Fragment NewFragment;
    private Fragment SettingsFragment;
    private FragmentTransaction FT;
    private FloatingActionButton settings;
    private FloatingActionButton logout;
    private FloatingActionButton newEvent;

    static class ViewHolder {
        TextView eInterestColor;
        ImageView eImage;
        TextView eName;
        TextView eAddress;
        TextView eTime;
        TextView eID;
    }

    @Override
    protected void onDestroy() {

        markers.clear();
        markers = null;
        events.clear();
        events = null;
        feed = null;

        mGoogleApiClient = null;
        mLastLocation = null;
        user = null;
        if(mMap != null) {
            mMap.clear();
            mMap = null;
        }

        if(blurImage != null && !blurImage.isRecycled()) {
            blurImage.recycle();
            blurImage = null;
        }

        loadingViewAnim.setCallback(null);
        loadingViewAnim = null;
        stripImageView(loadingIcon);
        loadingIcon = null;
        blur = null;

        loadingLayout.setBackgroundResource(0);
        loadingLayout = null;

        layout.setBackgroundResource(0);
        layout = null;

        NewFragment = null;
        SettingsFragment = null;
        FragmentTransaction FT = null;

        System.gc();
        super.onDestroy();
    }

    public static void stripImageView(ImageView view) {
        if ( view.getDrawable() instanceof BitmapDrawable) {
            ((BitmapDrawable)view.getDrawable()).getBitmap().recycle();
        }
        view.getDrawable().setCallback(null);
        view.setImageDrawable(null);
        view.getResources().flushLayoutCache();
        view.destroyDrawingCache();
        view.setBackgroundResource(0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.gc();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            user = extras.getString("USER_ID");
        }
        else
            user = "haimomesi@gmail.com";

        TextView logoLabel = (TextView)findViewById(R.id.logoLabel);
        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "lobster.otf");
        logoLabel.setTypeface(myTypeface);
        feed = (ListView)findViewById(R.id.feed);
        markers = new HashMap<>();
        events = new ArrayList<>();

        loadingLayout = (LinearLayout)findViewById(R.id.spinnerContainer);
        loadingLayout.setVisibility(View.GONE);
        loadingIcon = (ImageView) findViewById(R.id.spinner);
        loadingIcon.setVisibility(View.GONE);
        loadingIcon.setBackgroundResource(R.drawable.loading_animation);
        loadingViewAnim = (AnimationDrawable) loadingIcon.getBackground();
        layout = (RelativeLayout)findViewById(R.id.layout);
        blur = (ImageView)findViewById(R.id.blur);

        SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recreate();
            }
        });

        newEvent = (FloatingActionButton)findViewById(R.id.add_event);
        newEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SettingsFragment != null && SettingsFragment.isAdded() )
                {
                    getFragmentManager().popBackStack();
                }
                if (NewFragment == null || !NewFragment.isAdded()) {
                    ((FloatingActionsMenu) findViewById(R.id.multiple_actions_down)).toggle();

                    setBlurBool();
                    if (!fragmentBlurApplied) {
                        applyBlur();
                        fragmentBlurApplied = true;
                    }
                    blur.setClickable(true);
                    blur.setVisibility(View.VISIBLE);

                    NewFragment = new NewEventFragment();

                    FT = getFragmentManager().beginTransaction();
                    FT.add(R.id.new_event, NewFragment);
                    FT.addToBackStack("NewFragment");
                    FT.commit();
                }
            }
        });

        logout = (FloatingActionButton)findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FloatingActionsMenu) findViewById(R.id.multiple_actions_down)).toggle();
                startActivity(new Intent(getFeedClass(), LoginActivity.class));
                finish();
            }
        });

        settings = (FloatingActionButton)findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NewFragment != null && NewFragment.isAdded()) {
                    getFragmentManager().popBackStack();
                }
                if (SettingsFragment == null || !SettingsFragment.isAdded()) {
                    ((FloatingActionsMenu) findViewById(R.id.multiple_actions_down)).toggle();

                    setBlurBool();
                    if (!fragmentBlurApplied) {
                        applyBlur();
                        fragmentBlurApplied = true;
                    }
                    blur.setClickable(true);
                    blur.setVisibility(View.VISIBLE);

                    FT = getFragmentManager().beginTransaction();
                    SettingsFragment = new SettingsFragment();

                    FT.add(R.id.new_event, SettingsFragment);
                    FT.addToBackStack("SettingsFragment");
                    FT.commit();

                }
            }
        });

        buildGoogleApiClient();
    }

    @Override
    public void onBackPressed() {
        int count = getFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
            //additional code
        } else {
            getFragmentManager().popBackStack();
            stopSpinner();
        }
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        mMap = map;
        mMap.setIndoorEnabled(true);
        if (mLastLocation != null) {
            LatLng mLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            this.mMap.setMyLocationEnabled(true);
            this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocation, 13));
        }
        loadingIcon.post(new Starter());
    }

    synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        Log.e("Connected?", String.valueOf(mGoogleApiClient.isConnected()));
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates locationSettingsStates = result.getLocationSettingsStates();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:

                        if (!haveNetworkConnection()) {
                            WifiManager wifiManager = (WifiManager) getFeedClass()
                                    .getSystemService(Context.WIFI_SERVICE);

                            if(!wifiManager.isWifiEnabled())
                                wifiManager.setWifiEnabled(true);
                        }

                        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                        MapFragment mapFragment = (MapFragment) getFragmentManager()
                                .findFragmentById(R.id.map);
                        mapFragment.getMapAsync(getFeedClass());

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.

                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().

                            status.startResolutionForResult(
                                    getFeedClass(),
                                    37);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }

                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        toast("QUITTING");
                        finish();
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //final LocationSettingsStates states = LocationSettingsStates.fromIntent(intent);
        switch (requestCode) {
            case 37:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made

                        if (!haveNetworkConnection()) {
                            WifiManager wifiManager = (WifiManager) getFeedClass()
                                    .getSystemService(Context.WIFI_SERVICE);
                            if(!wifiManager.isWifiEnabled())
                                wifiManager.setWifiEnabled(true);
                        }

                        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                        MapFragment mapFragment = (MapFragment) getFragmentManager()
                                .findFragmentById(R.id.map);
                        mapFragment.getMapAsync(getFeedClass());


                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        toast("QUITTING");
                        finish();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public void setEvent(JSONArray jsonArray)
    {
        for(int i=0; i<jsonArray.length();i++){
            JSONObject json = null;
            try {
                json = jsonArray.getJSONObject(i);
                String result = json.getString("result");
                ActionProcessButton post = (ActionProcessButton)NewFragment.getView().findViewById(R.id.post);
                FrameLayout new_event_layout = (FrameLayout)NewFragment.getView().findViewById(R.id.new_event_layout);
                if( result.equals("success") )
                {
                    ImageView image = (ImageView)NewFragment.getView().findViewById(R.id.image);
                    if(image.getDrawable() != null) {
                        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        Hashtable<String, String> params = new Hashtable<>();
                        params.put("ID", json.getString("ID"));
                        params.put("image", Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT));
                        image.destroyDrawingCache();
                        bitmap.recycle();
                        spinnerStarted = true;
                        new DBHandler("dummyUser", "set_image", params, "setImage", this).execute();
                    }
                    else
                    {
                        post.setProgress(100);
                        post.setEnabled(true);
                        if(getFragmentManager().getBackStackEntryCount() > 0)
                            getFragmentManager().popBackStack();

                        recreate();
                    }
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("DB error: Event was not added");
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            return;
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    post.setProgress(0);
                    post.setEnabled(true);
                    new_event_layout.setEnabled(true);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public void setUserSettings(JSONArray jsonArray)
    {
        for(int i=0; i<jsonArray.length();i++){
            JSONObject json = null;
            try {
                json = jsonArray.getJSONObject(i);
                String result = json.getString("result");
                ActionProcessButton buttonSaveSettings = (ActionProcessButton) SettingsFragment.getView().findViewById(R.id.buttonSaveSettings);
                RelativeLayout settings = (RelativeLayout)SettingsFragment.getView().findViewById(R.id.settings);
                if( result.equals("success") )
                {
                    buttonSaveSettings.setProgress(100);
                    buttonSaveSettings.setEnabled(true);
                    if(getFragmentManager().getBackStackEntryCount() > 0)
                        getFragmentManager().popBackStack();

                    recreate();
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("DB error: Settings not saved");
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            return;
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    buttonSaveSettings.setProgress(0);
                    buttonSaveSettings.setEnabled(true);
                    settings.setEnabled(true);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public void setImage(JSONArray jsonArray)
    {
        for(int i=0; i<jsonArray.length();i++){
            JSONObject json = null;
            try {
                ActionProcessButton post = (ActionProcessButton)NewFragment.getView().findViewById(R.id.post);
                FrameLayout new_event_layout = (FrameLayout)NewFragment.getView().findViewById(R.id.new_event_layout);
                json = jsonArray.getJSONObject(i);
                String result = json.getString("result");
                if( result.equals("success") )
                {
                    post.setProgress(100);
                    post.setEnabled(true);
                    if(getFragmentManager().getBackStackEntryCount() > 0)
                        getFragmentManager().popBackStack();

                    recreate();
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Server error: Image was not added");
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            return;
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    post.setProgress(0);
                    post.setEnabled(true);
                    new_event_layout.setEnabled(true);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void getUserSettings(JSONArray jsonArray)
    {
        for(int i=0; i<jsonArray.length();i++){
            JSONObject json = null;
            try {
                json = jsonArray.getJSONObject(i);
                userRadius = json.getInt("radius");
                userInterests = json.getString("interests");
                new DBHandler(this.user,"get_user_feed",null,"getUserFeed",this).execute();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        decrementComponentsProcessed();
    }

    public void getUserFeed(JSONArray jsonArray)
    {
        float[] results = new float[1];
        for(int i=0; i<jsonArray.length();i++){
            JSONObject json = null;
            try {
                json = jsonArray.getJSONObject(i);
                double lat = json.getDouble("lat");
                double lng = json.getDouble("lng");
                Location.distanceBetween(mLastLocation.getLatitude(), mLastLocation.getLongitude(),
                       lat , lng , results);
                if((double)results[0]/1000<userRadius) {
                    int assetName=0;
                    switch (json.getString("interests"))
                    {
                        case "Music":
                            assetName=R.drawable.music;
                            break;
                        case "Sport":
                            assetName=R.drawable.sport;
                            break;
                        case "Alcohol":
                            assetName=R.drawable.alcohol;
                            break;
                        case "Animals":
                            assetName=R.drawable.animals;
                            break;
                        case "Art":
                            assetName=R.drawable.art;
                            break;
                        case "Business":
                            assetName=R.drawable.business;
                            break;
                        case "Cinema":
                            assetName=R.drawable.cinema;
                            break;
                        case "Food":
                            assetName=R.drawable.food;
                            break;
                        case "Night Life":
                            assetName=R.drawable.night_life;
                            break;
                        case "Theater":
                            assetName=R.drawable.theater;
                            break;
                        default:
                            break;
                    }
                    markers.put(String.valueOf(json.getInt("ID")), this.mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lng))
                            .title(json.getString("name"))
                            .snippet(json.getString("address"))
                            .icon(BitmapDescriptorFactory.fromResource(assetName))));
                    events.add(new ArrayList<>(Arrays.asList(String.valueOf(json.getInt("ID")), json.getString("name"), json.getString("address"), json.getString("interests"), json.getString("date"), "true")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        final FeedArrayAdapter adapter = new FeedArrayAdapter(this, events);
        getFeedClass().feed.setAdapter(adapter);

        decrementComponentsProcessed();
    }

    public void toast(String text){
        //Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        //toast.show();
        final SuperToast superToast = new SuperToast(this);
        superToast.setDuration(SuperToast.Duration.VERY_SHORT);
        superToast.setAnimations(SuperToast.Animations.FADE);
        superToast.setBackground(SuperToast.Background.WHITE);
        superToast.setTextSize(SuperToast.TextSize.LARGE);
        superToast.setGravity(Gravity.CENTER, 0, 0);
        superToast.setTextColor(0xFF1E90FF);
        superToast.setTypefaceStyle(Typeface.BOLD);
        superToast.setText(text);
        superToast.show();
    }

    public void applyBlur()
    {
        runOnUiThread(new  Runnable() {
            public  void  run() {
                layout.setDrawingCacheEnabled(true);
                layout.buildDrawingCache();
                blurImage = BlurImage(layout.getDrawingCache());
                layout.setDrawingCacheEnabled(false);
                blur.setImageBitmap(blurImage);
                blurApplied = true;
            }
        });
    }

    public void startSpinner()
    {
        componentsProcessed++;
        if(!spinnerStarted) {
            spinnerStarted = true;

            applyBlur();

            feed.setVisibility(View.INVISIBLE);
            //blur.setClickable(true);
            blur.setVisibility(View.VISIBLE);

            loadingLayout.setVisibility(View.VISIBLE);
            loadingIcon.setVisibility(View.VISIBLE);
            loadingViewAnim.start();
        }
    }

    void checkSpinner()
    {
        if(componentsProcessed==0) {
            stopSpinner();
            feed.setVisibility(View.VISIBLE);
        }
    }

    void stopSpinner()
    {
        runOnUiThread(new Runnable() {
            public void run() {
                blur.setVisibility(View.GONE);
                //blur.setClickable(false);
                blur.setImageDrawable(null);
                if (blurImage != null && !blurImage.isRecycled()) {
                    blurImage.recycle();
                    blurImage = null;
                }
                System.gc();
                spinnerStarted = false;
                loadingLayout.setVisibility(View.GONE);
                loadingIcon.setVisibility(View.GONE);
                loadingViewAnim.stop();
            }
        });
    }

    void decrementComponentsProcessed()
    {
        componentsProcessed--;
        checkSpinner();
    }

    Bitmap BlurImage(Bitmap input)
    {
        try
        {
            RenderScript rsScript = RenderScript.create(getApplicationContext());
            Allocation alloc = Allocation.createFromBitmap(rsScript, input);

            ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rsScript,   Element.U8_4(rsScript));
            blur.setRadius(21);
            blur.setInput(alloc);

            Bitmap result = Bitmap.createBitmap(input.getWidth(), input.getHeight(), Bitmap.Config.ARGB_8888);
            Allocation outAlloc = Allocation.createFromBitmap(rsScript, result);

            blur.forEach(outAlloc);
            outAlloc.copyTo(result);

            rsScript.destroy();
            return result;
        }
        catch (Exception e) {
            // TODO: handle exception
            return input;
        }
    }

    private class Starter implements Runnable {
        public void run() {
            new DBHandler(user,"get_user_settings",null,"getUserSettings",getFeedClass()).execute();
        }
    }

    public void setSpinner()
    {
        spinnerStarted = true;
    }

    public String getUserInterests()
    {
        return userInterests;
    }

    public String getUser()
    {
        return user;
    }

    public int getUserRadius()
    {
        return userRadius;
    }

    public LatLng getLatLng()
    {
        return new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
    }

    public FeedActivity getFeedClass()
    {
        return FeedActivity.this;
    }

    public void setBlurBool()
    {
        blurApplied = false;
    }


    public class FeedArrayAdapter extends ArrayAdapter<ArrayList> {

        private final String IMG_URL = "http://nearme.host22.com/images/events/";
        private ArrayList<ArrayList<String>> values;
        private String color="";
        private Typeface myTypeface;
        private SimpleDateFormat sdf;
        private Date date;

        public FeedArrayAdapter(Context context,ArrayList values) {
            super(context,R.layout.feed_item, values);
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.feed_item,parent,false);

                holder = new ViewHolder();
                holder.eInterestColor = (TextView) convertView.findViewById(R.id.eInterestColor);
                holder.eImage = (ImageView) convertView.findViewById(R.id.eImage);
                holder.eName = (TextView) convertView.findViewById(R.id.eName);
                holder.eAddress = (TextView) convertView.findViewById(R.id.eAddress);
                holder.eTime = (TextView) convertView.findViewById(R.id.eTime);
                holder.eID = (TextView) convertView.findViewById(R.id.eID);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            myTypeface = Typeface.createFromAsset(getAssets(), "lobster.otf");
            holder.eName.setTypeface(myTypeface);
            holder.eAddress.setTypeface(myTypeface);
            holder.eTime.setTypeface(myTypeface);
            holder.eID.setVisibility(View.GONE);

            final ArrayList<String> params = (ArrayList) values.get(position);

            switch (params.get(3)) {
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
            holder.eInterestColor.setBackgroundColor(Color.parseColor(color));
            //convertView.setBackgroundColor(Color.parseColor(color));
            holder.eName.setText(params.get(1));
            holder.eAddress.setText(params.get(2));
            sdf = new SimpleDateFormat("yyyy-M-dd hh:mm:ss");
            date = null;
            try {
                date = sdf.parse(params.get(4));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            holder.eTime.setText(DateUtils.getRelativeTimeSpanString(date != null ? date.getTime() : 0, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));
            holder.eID.setText(params.get(0));
            if(params.get(5).equals("true")) {
                Picasso.with(getContext()).load(this.IMG_URL + params.get(0) + ".jpg").error(R.drawable.img_unavailable).resize(80, 80).into(holder.eImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.e("image", "success");
                    }

                    @Override
                    public void onError() {
                        holder.eImage.setImageResource(R.drawable.img_unavailable);
                        params.set(5, "false");
                    }
                });
            }
            else
                holder.eImage.setImageResource(R.drawable.img_unavailable);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView id = (TextView) v.findViewById(R.id.eID);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markers.get(id.getText().toString()).getPosition(), 18));
                }
            });

            return convertView;
        }
    }
}