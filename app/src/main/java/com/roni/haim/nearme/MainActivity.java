package com.roni.haim.nearme;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;


public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private String user;
    private ListView feed;
    private ArrayList<ArrayList<String>> events;
    //private TextView mLatitudeText;
    //private TextView mLongitudeText;

    //private GoogleMap map;
    //private GoogleApiClient mGoogleApiClient;
    //private Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.user = "haimomesi@hotmail.com";
        this.feed = (ListView)findViewById(R.id.feed);
        this.events = new ArrayList<ArrayList<String>>();
        new DBHandler(this.user,"get_user_feed",null,"getUserFeed",this).execute();

        /*
        SET USER STUB
        Hashtable<String,String> params = new Hashtable<String,String>();
        params.put("name","Haim Omesi");
        params.put("pass",BCrypt.hashpw("Waterball37", BCrypt.gensalt()));
        new DBHandler(this.user,"set_user",params,"setUser",this).execute();
        */

        /*
        SET USER SETTINGS STUB
        Hashtable<String,String> params = new Hashtable<String,String>();
        params.put("radius","37");
        params.put("interests","Food,Music,Art");
        new DBHandler(this.user,"set_user_settings",params,"setUserSettings",this).execute();
         */


        /*
        MATCH PASSWORD STUB
        String passFromDB = "@YREHGFKU^$&%$EG";
        String passFromUser = BCrypt.hashpw(passFromForm, BCrypt.gensalt());
        if (BCrypt.checkpw(PassFromUser, hashed))
            System.out.println("It matches");
        else
            System.out.println("It does not match");
        */

        /*
        GET USER SETTINGS STUB
        new DBHandler(this.user,"get_user_settings",null,"getUserSettings",this).execute();
        */

        /*
        GET USER FEED STUB
        new DBHandler(this.user,"get_user_feed",null,"getUserFeed",this).execute();
         */

        /*
        GET EVENT BY ID STUB
        Hashtable<String,String> params = new Hashtable<String,String>();
        params.put("ID","10");
        new DBHandler(this.user,"get_event",params,"getEvent",this).execute();
        */

        /*
        CREATE EVENT STUB
        Hashtable<String,String> params = new Hashtable<String,String>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        Date date = new Date();
        params.put("date", dateFormat.format(date));
        params.put("name", "Jimmy Hendrix Tribute");
        params.put("interests", "Music");
        params.put("address", "7 Ha-Biluyim, Gedera, Israel");
        params.put("lat", "31.815186");
        params.put("lng", "34.772329");
        params.put("type","bar");
        new DBHandler(this.user,"set_event",params,"setEvent",this).execute();

        */

        /*
        Bitmap bitmap = ((BitmapDrawable)userPic.getDrawable()).getBitmap();
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        //byte[] imageInByte=stream.toByteArray();
        Hashtable<String,String> params = new Hashtable<String,String>();
        params.put("image",Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT));
        new DBHandler(user,"set_image",params,"setUser",this).execute();
        */

    }

    public void getUserFeed(JSONArray jsonArray)
    {
        float[] results = new float[1];
        for(int i=0; i<jsonArray.length();i++){
            JSONObject json = null;
            try {
                json = jsonArray.getJSONObject(i);
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
                    events.add(new ArrayList<String>(Arrays.asList(String.valueOf(json.getInt("ID")),json.getString("name"),json.getString("address"),json.getString("interests"),json.getString("date"),"true")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        final FeedArrayAdapter adapter = new FeedArrayAdapter(this, events);
        getFeedClass().feed.setAdapter(adapter);
    }

    MainActivity getFeedClass()
    {
        return MainActivity.this;
    }

    static class ViewHolder {
        TextView eInterestColor;
        ImageView eImage;
        TextView eName;
        TextView eAddress;
        TextView eTime;
        TextView eID;
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
                //holder.eInterestColor = (TextView) convertView.findViewById(R.id.eInterestColor);
                holder.eImage = (ImageView) convertView.findViewById(R.id.eImage);
                holder.eName = (TextView) convertView.findViewById(R.id.eName);
                holder.eAddress = (TextView) convertView.findViewById(R.id.eAddress);
                holder.eTime = (TextView) convertView.findViewById(R.id.eTime);
                holder.eID = (TextView) convertView.findViewById(R.id.eID);

                convertView.setTag(holder);
                Log.e("new", "on position " + position);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
                Log.e("old", "on position "+ position);
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

            sdf = new SimpleDateFormat("yyyy-M-dd hh:mm:ss", Locale.US);
            date = null;
            try {
                date = sdf.parse(params.get(4));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            holder.eTime.setText(DateUtils.getRelativeTimeSpanString(date != null ? date.getTime() : 0, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));
            holder.eID.setText(params.get(0));
            if(params.get(5).equals("true")) {
                Picasso.with(getContext()).load(this.IMG_URL + params.get(0) + ".jpg").resize(80, 80).into(holder.eImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.e("image","success");
                    }

                    @Override
                    public void onError() {
                        Log.e("image","failure");
                        holder.eImage.setImageResource(R.drawable.img_unavailable);
                        params.set(5,"false");
                    }
                });
            }

            return convertView;
        }
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
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
                    System.out.println("success");
                }
                else
                    System.out.println("error");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void getEvent(JSONArray jsonArray)
    {
        for(int i=0; i<jsonArray.length();i++){
            JSONObject json = null;
            try {
                json = jsonArray.getJSONObject(i);
                System.out.println(json.toString());
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
                System.out.println("ID: " + json.getString("ID") + " Radius: " + json.getInt("radius") + " Interests: ");
                String[] interests = json.getString("interests").split(",");
                for (int idx=0; idx<interests.length; idx++) {
                    System.out.println(interests[idx]);
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
                if( result.equals("success") )
                {
                    System.out.println("success");
                }
                else
                    System.out.println("error");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void setUser(JSONArray jsonArray) {
        for(int i=0; i<jsonArray.length();i++){
            JSONObject json = null;
            try {
                json = jsonArray.getJSONObject(i);
                String result = json.getString("result");
                if( result.equals("success") )
                {
                    System.out.println("success");
                }
                else
                    System.out.println("error");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    public void SetUserDetails(JSONArray jsonArray)
    {
        for(int i=0; i<jsonArray.length();i++){
            JSONObject json = null;
            try {
                json = jsonArray.getJSONObject(i);
               // new IMGHandler(this.userPic,"http://nearme.host22.com/images/users/"+this.user+".jpg",this).execute(70);
                //this.userFullName.setText(json.getString("name"));

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                    e.printStackTrace();
            }
        }
    }

    public void setTextToTextView(JSONArray jsonArray)
    {
        String s  = "";
        for(int i=0; i<jsonArray.length();i++){

            JSONObject json = null;
            try {
                json = jsonArray.getJSONObject(i);
                s = s +
                        "User : "+json.getString("ID")+"\n"+
                        "Name : "+json.getString("name")+"\n"+
                        "Picture : "+json.getString("pic")+"\n"+
                        "Password : "+json.getString("pass")+"\n\n";
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //this.userFullName.setText(s);
    }

    public void showInterests(JSONArray jsonArray) {
        String s  = "Interests :\n";
        for(int i=0; i<jsonArray.length();i++) {
            JSONObject json = null;
            try {
                json = jsonArray.getJSONObject(i);

                s = s + json.getString("interest")+"\n";
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //this.userFullName.setText(s);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        /*
        this.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
        }
        */
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
