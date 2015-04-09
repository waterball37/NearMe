package com.roni.haim.nearme;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;


public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private String user;
    private TextView userFullName;
    private ImageView userPic;
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
        this.userFullName = (TextView) this.findViewById(R.id.userFullName);
        this.userPic = (ImageView)findViewById(R.id.userPic);
        //this.mLatitudeText = (TextView) this.findViewById(R.id.mLatitudeText);
        //this.mLongitudeText = (TextView) this.findViewById(R.id.mLongitudeText);
        //this.map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
        //this.map.setMyLocationEnabled(true);
        //buildGoogleApiClient();


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
        //TODO ADD IMAGE
        */

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

    public void getUserFeed(JSONArray jsonArray)
    {
        for(int i=0; i<jsonArray.length();i++){
            JSONObject json = null;
            try {
                json = jsonArray.getJSONObject(i);
                //this.map.addMarker(new MarkerOptions()
                //.position(new LatLng(json.getDouble("lat"), json.getDouble("lng"))));
                System.out.println(
                    "ID: " + json.getString("ID") +
                    "Date: " + json.getString("date") +
                    " name: " + json.getString("name") +
                    " Interests: " + json.getString("interests") +
                    " Address: " + json.getString("address") +
                    " Lat : "+json.getDouble("lat") +
                    " Lng : "+json.getDouble("lng") +
                    " Address: " + json.getString("type"));
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

    /*
    protected synchronized void buildGoogleApiClient() {
        this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    */

    public void SetUserDetails(JSONArray jsonArray)
    {
        for(int i=0; i<jsonArray.length();i++){
            JSONObject json = null;
            try {
                json = jsonArray.getJSONObject(i);
                new IMGHandler(this.userPic,"http://nearme.host22.com/images/users/"+this.user+".jpg").execute(70);
                this.userFullName.setText(json.getString("name"));

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

        this.userFullName.setText(s);
    }

    /*
    public void setOnMap(JSONArray jsonArray) {
        String s  = "";
        for(int i=0; i<jsonArray.length();i++) {
            JSONObject json = null;
            try {
                json = jsonArray.getJSONObject(i);

                s = s +
                        "Lat : "+json.getDouble("lat")+"\n"+
                        "Lng : "+json.getDouble("lng")+"\n\n";
                //this.map.addMarker(new MarkerOptions()
                        .position(new LatLng(json.getDouble("lat"), json.getDouble("lng"))));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        this.userFullName.setText(s);
    }
    */

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
        this.userFullName.setText(s);
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
