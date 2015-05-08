package com.roni.haim.nearme;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;

public class FeedActivity extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener  {
    private final String IMG_URL = "http://nearme.host22.com/images/users/";
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String user;
    private int userRadius;
    private GoogleMap mMap;
    private TextView userFullName;
    private ImageView userPic;
    private ListView feed;
    private ArrayAdapter<String> listAdapter ;
    private HashMap<String,Marker> markers;
    private ArrayList<ArrayList<String>> events;

    private boolean blurApplied = false;
    private Bitmap blurImage;

    private boolean spinnerStarted = false;
    private int componentsProcessed = 0;

    private AnimationDrawable loadingViewAnim=null;
    private ImageView loadingIcon = null;
    private LinearLayout loadingLayout = null;

    private RelativeLayout layout;
    private ImageView blur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        this.user = "haimomesi@gmail.com";

        //this.userFullName = (TextView)findViewById(R.id.userFullName);
        //this.userPic = (ImageView)findViewById(R.id.userPic);
        this.feed = (ListView)findViewById(R.id.feed);
        this.markers = new HashMap<String,Marker>();
        this.events = new ArrayList<ArrayList<String>>();
        //new IMGHandler(this.userPic, this.IMG_URL+this.user+".jpg").execute(70);
        //new DBHandler(this.user,"get_user",null,"getUser",this).execute();

        loadingLayout = (LinearLayout)findViewById(R.id.spinnerContainer);
        loadingLayout.setVisibility(View.GONE);

        loadingIcon = (ImageView) findViewById(R.id.spinner);
        loadingIcon.setVisibility(View.GONE);

        loadingIcon.setBackgroundResource(R.drawable.loading_animation);
        loadingViewAnim = (AnimationDrawable) loadingIcon.getBackground();

        layout = (RelativeLayout)findViewById(R.id.layout);
        blur = (ImageView)findViewById(R.id.blur);

        buildGoogleApiClient();
    }

    public void getUser(JSONArray jsonArray)
    {
        if(jsonArray==null)
        {
            System.out.println("it was null");
            return;
        }
        for(int i=0; i<jsonArray.length();i++){
            JSONObject json = null;
            try {
                json = jsonArray.getJSONObject(i);
                this.userFullName.setText(json.getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        decrementComponentsProcessed();
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        this.mMap = map;
        if(mLastLocation!=null) {
            LatLng mLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            this.mMap.setMyLocationEnabled(true);
            this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocation, 13));
        }
        loadingIcon.post(new Starter());
        //
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
                    events.add(new ArrayList<String>(Arrays.asList(String.valueOf(json.getInt("ID")),json.getString("name"),json.getString("address"),json.getString("interests"))));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        FeedArrayAdapter adapter = new FeedArrayAdapter(this, events);
        this.feed.setAdapter(adapter);
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

    private void toast(String text){
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
        private String IMG_URL = "http://nearme.host22.com/images/events/";
        private Context context;
        private ArrayList<ArrayList<String>> values;
        private View rowView;
        private ImageView eImage;
        private TextView eInterestColor;
        private TextView eName;
        private TextView eAddress;
        private TextView eID;
        private ArrayList<String> params;
        private String color="";

        public FeedArrayAdapter(Context context,ArrayList values) {
            super(context,R.layout.feed_item, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.feed_item, parent, false);
            eImage = (ImageView) rowView.findViewById(R.id.eImage);
            eInterestColor = (TextView) rowView.findViewById(R.id.eInterestColor);
            eName = (TextView) rowView.findViewById(R.id.eName);
            eAddress = (TextView) rowView.findViewById(R.id.eAddress);
            eID = (TextView) rowView.findViewById(R.id.eID);
            params = (ArrayList)values.get(position);
            new IMGHandler(eImage, this.IMG_URL+params.get(0)+".jpg", getFeedClass()).execute(80);
            eID.setText(params.get(0));
            eName.setText(params.get(1));
            eAddress.setText(params.get(2));

            switch (params.get(3))
            {
                case "Music":
                    color="#F22613";
                    break;
                case "Sport":
                    color="#26C281";
                    break;
                case "Alcohol":
                    color="#22313F";
                    break;
                case "Animals":
                    color="#663399";
                    break;
                case "Art":
                    color="#F62459";
                    break;
                case "Business":
                    color="#6C7A89";
                    break;
                case "Cinema":
                    color="#F89406";
                    break;
                case "Food":
                    color="#F9BF3B";
                    break;
                case "Night Life":
                    color="#1F3A93";
                    break;
                case "Theater":
                    color="#4183D7";
                    break;
                default:
                    break;
            }

            eInterestColor.setBackgroundColor(Color.parseColor(color));
            //eName.setTextColor(Color.WHITE);
            //eAddress.setTextColor(Color.WHITE);
            eID.setVisibility(View.GONE);
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView id = (TextView)v.findViewById(R.id.eID);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markers.get(id.getText().toString()).getPosition(), 19));
                }
            });

            return rowView;
        }
    }

    FeedActivity getFeedClass()
    {
        return FeedActivity.this;
    }

    public void startSpinner()
    {
        componentsProcessed++;
        if(!spinnerStarted) {
            if(!blurApplied) {
                layout.setDrawingCacheEnabled(true);
                layout.buildDrawingCache();
                blurImage = BlurImage(layout.getDrawingCache());
                layout.setDrawingCacheEnabled(false);
                blur.setImageBitmap(blurImage);
                blurApplied = true;
            }
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
        loadingLayout.setVisibility(View.GONE);
        loadingIcon.setVisibility(View.GONE);
        loadingViewAnim.stop();
        blur.setVisibility(View.GONE);
        blur.setClickable(false);

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
            //start Asyn Task here
            new DBHandler(user,"get_user_settings",null,"getUserSettings",getFeedClass()).execute();
        }
    }
}