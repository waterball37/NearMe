package com.roni.haim.nearme;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

public class FeedActivity extends Activity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener  {

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String user;
    private int userRadius;
    private GoogleMap mMap;
    private ListView feed;
    //private MyListView feed;
    private HashMap<String,Marker> markers;
    private ArrayList<ArrayList<String>> events;

    private boolean blurApplied = false;
    private boolean fragmentBlurApplied = false;
    private Bitmap blurImage;

    private boolean spinnerStarted = false;
    private int componentsProcessed = 0;

    private AnimationDrawable loadingViewAnim=null;
    private ImageView loadingIcon = null;
    private LinearLayout loadingLayout = null;

    private RelativeLayout layout;
    private ImageView blur;

    private ImageButton newEvent;
    private Fragment NewFragment;
    private FragmentTransaction FT;

    private SwipeRefreshLayout mSwipeRefreshLayout;

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
        mMap.clear();
        markers.clear();
        events.clear();
        System.gc();
        super.onDestroy();
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

        feed = (ListView)findViewById(R.id.feed);
        //feed = (MyListView)findViewById(R.id.feed);
        markers = new HashMap<String,Marker>();
        events = new ArrayList<ArrayList<String>>();

        loadingLayout = (LinearLayout)findViewById(R.id.spinnerContainer);
        loadingLayout.setVisibility(View.GONE);

        loadingIcon = (ImageView) findViewById(R.id.spinner);
        loadingIcon.setVisibility(View.GONE);

        loadingIcon.setBackgroundResource(R.drawable.loading_animation);
        loadingViewAnim = (AnimationDrawable) loadingIcon.getBackground();

        layout = (RelativeLayout)findViewById(R.id.layout);
        blur = (ImageView)findViewById(R.id.blur);

        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.refresh_layout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                finish();
                startActivity(getIntent());
            }
        });

        newEvent = (ImageButton)findViewById(R.id.add_event);
        newEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(NewFragment == null || !NewFragment.isAdded()) {
                    setBlurBool(false);
                    if(!fragmentBlurApplied) {
                        applyBlur();
                        fragmentBlurApplied = true;
                    }
                    blur.setClickable(true);
                    blur.setVisibility(View.VISIBLE);

                    FT = getFragmentManager().beginTransaction();
                    //FT.setCustomAnimations(R.animator.enter_anim, R.animator.exit_anim);

                    NewFragment = new NewEventFragment();

                    FT.add(R.id.new_event, NewFragment);
                    FT.addToBackStack("NewFragment");
                    FT.commit();
                }
            }
        });

        buildGoogleApiClient();
    }


    public void setEvent(JSONArray jsonArray)
    {
        for(int i=0; i<jsonArray.length();i++){
            JSONObject json = null;
            try {
                json = jsonArray.getJSONObject(i);
                String result = json.getString("result");
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
                        new DBHandler("dummyUser", "set_image", params, "setImage", this).execute();
                    }
                }
                else
                    System.out.println("error");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

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

    public void setImage(JSONArray jsonArray)
    {
        for(int i=0; i<jsonArray.length();i++){
            JSONObject json = null;
            try {
                json = jsonArray.getJSONObject(i);
                String result = json.getString("result");
                if( result.equals("success") )
                {
                    toast("Event added succesfully");
                    stopSpinner();
                    finish();
                    startActivity(getIntent());
                }
                else
                    System.out.println("error");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        mMap = map;
        mMap.setIndoorEnabled(true);
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            if(haveNetworkConnection()) {
                if (mLastLocation != null) {
                    LatLng mLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    this.mMap.setMyLocationEnabled(true);
                    this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocation, 13));
                }
                loadingIcon.post(new Starter());
            }
            else
                toast("No internet connection");
        }
        else
        {
            toast("Location services is off");
        }
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

    public LatLng getLatLng()
    {
        return new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
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
                            assetName=R.drawable.red_marker;
                            break;
                        case "Sport":
                            assetName=R.drawable.green_marker;
                            break;
                        case "Alcohol":
                            assetName=R.drawable.dark_marker;
                            break;
                        case "Animals":
                            assetName=R.drawable.purple_marker;
                            break;
                        case "Art":
                            assetName=R.drawable.pink_marker;
                            break;
                        case "Business":
                            assetName=R.drawable.grey_marker;
                            break;
                        case "Cinema":
                            assetName=R.drawable.orange_marker;
                            break;
                        case "Food":
                            assetName=R.drawable.yellow_marker;
                            break;
                        case "Night Life":
                            assetName=R.drawable.dark_blue_marker;
                            break;
                        case "Theater":
                            assetName=R.drawable.blue_marker;
                            break;
                        default:
                            break;
                    }
                    markers.put(String.valueOf(json.getInt("ID")), this.mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lng))
                            .title(json.getString("name"))
                            .snippet(json.getString("address"))
                            .icon(BitmapDescriptorFactory.fromResource(assetName))));
                    events.add(new ArrayList<String>(Arrays.asList(String.valueOf(json.getInt("ID")),json.getString("name"),json.getString("address"),json.getString("interests"),json.getString("date"),"true")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        final FeedArrayAdapter adapter = new FeedArrayAdapter(this, events);
        getFeedClass().feed.setAdapter(adapter);

        decrementComponentsProcessed();
    }

    public void getUserSettings(JSONArray jsonArray)
    {
        for(int i=0; i<jsonArray.length();i++){
            JSONObject json = null;
            try {
                json = jsonArray.getJSONObject(i);
                userRadius = json.getInt("radius");
                new DBHandler(this.user,"get_user_feed",null,"getUserFeed",this).execute();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        decrementComponentsProcessed();
    }

    public void toast(String text){
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
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
        //new Thread(new GetContent()).start();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

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
                    color = "#F22613";
                    break;
                case "Sport":
                    color = "#26C281";
                    break;
                case "Alcohol":
                    color = "#22313F";
                    break;
                case "Animals":
                    color = "#663399";
                    break;
                case "Art":
                    color = "#F62459";
                    break;
                case "Business":
                    color = "#6C7A89";
                    break;
                case "Cinema":
                    color = "#F89406";
                    break;
                case "Food":
                    color = "#F9BF3B";
                    break;
                case "Night Life":
                    color = "#1F3A93";
                    break;
                case "Theater":
                    color = "#4183D7";
                    break;
                default:
                    break;
            }
            holder.eInterestColor.setBackgroundColor(Color.parseColor(color));
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

    FeedActivity getFeedClass()
    {
        return FeedActivity.this;
    }

    public void setBlurBool(Boolean value)
    {
        blurApplied = value;
    }

    public void applyBlur()
    {
        layout.setDrawingCacheEnabled(true);
        layout.buildDrawingCache();
        blurImage = BlurImage(layout.getDrawingCache());
        layout.setDrawingCacheEnabled(false);
        blur.setImageBitmap(blurImage);
        blurApplied = true;
    }

    public void startSpinner()
    {
        componentsProcessed++;
        if(!spinnerStarted) {
            spinnerStarted = true;

            applyBlur();

            feed.setVisibility(View.INVISIBLE);
            blur.setClickable(true);
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
        blur.setVisibility(View.GONE);
        blur.setClickable(false);
        blur.setImageDrawable(null);
        if(blurImage != null && !blurImage.isRecycled()) {
            blurImage.recycle();
            blurImage = null;
        }
        System.gc();
        spinnerStarted = false;
        loadingLayout.setVisibility(View.GONE);
        loadingIcon.setVisibility(View.GONE);
        loadingViewAnim.stop();
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
}