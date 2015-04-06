package com.roni.haim.nearme;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
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


public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private TextView responseTextView;
    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private String user;
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.user = "haimomesi@gmail.com";
        this.responseTextView = (TextView) this.findViewById(R.id.responseTextView);
        //this.mLatitudeText = (TextView) this.findViewById(R.id.mLatitudeText);
        //this.mLongitudeText = (TextView) this.findViewById(R.id.mLongitudeText);
        //this.map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
        //this.map.setMyLocationEnabled(true);
        //buildGoogleApiClient();
       //new ShowOnMapTask().execute(new ApiConnector(this.user));
        new GetInterestsTask().execute(new ApiConnector(this.user));
        //new GetFeedTask().execute(new ApiConnector(this.user));
    }

    protected synchronized void buildGoogleApiClient() {
        this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
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

        this.responseTextView.setText(s);
    }

    public void setOnMap(JSONArray jsonArray) {
        String s  = "";
        for(int i=0; i<jsonArray.length();i++) {
            JSONObject json = null;
            try {
                json = jsonArray.getJSONObject(i);

                s = s +
                        "Lat : "+json.getDouble("lat")+"\n"+
                        "Lng : "+json.getDouble("lng")+"\n\n";
                this.map.addMarker(new MarkerOptions()
                        .position(new LatLng(json.getDouble("lat"), json.getDouble("lng"))));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        this.responseTextView.setText(s);
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
        this.responseTextView.setText(s);
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

    private class ShowOnMapTask extends AsyncTask<ApiConnector,Long,JSONArray>
    {
        @Override
        protected JSONArray doInBackground(ApiConnector... params) {
            return params[0].ShowOnMap();
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            setOnMap(jsonArray);
        }
    }

    private class GetFeedTask extends AsyncTask<ApiConnector,Long,JSONArray>
    {
        @Override
        protected JSONArray doInBackground(ApiConnector... params) {

            // it is executed on Background thread
            return params[0].GetFeed();
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            setTextToTextView(jsonArray);
        }
    }

    private class GetInterestsTask extends AsyncTask<ApiConnector,Long,JSONArray>
    {
        @Override
        protected JSONArray doInBackground(ApiConnector... params) {
            return params[0].GetInterests();
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            showInterests(jsonArray);
        }
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
