package com.roni.haim.nearme;

import android.graphics.Point;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Random;


public class SplashActivity extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {
    private GoogleMap mMap;
    private MarkerOptions mMarkerOptions ;
    private MapFragment mMapFragment;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        buildGoogleApiClient();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash, menu);
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

    @Override
    public void onMapReady(final GoogleMap map) {
        if(mLastLocation!=null) {
            LatLng mLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            map.setMyLocationEnabled(true);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocation, 13));
        }
        //else

        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        final LatLng sw = bounds.southwest;
        final LatLng ne = bounds.northeast;
        final long duration = 5000;
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = map.getProjection();
        final SecureRandom random = new SecureRandom();

        final Interpolator interpolator = new LinearInterpolator();
        final HashMap<Integer,Marker> markers = new HashMap<Integer,Marker>();
        final int mark = 0;
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                for(int i=0 ; i<10 ; i++) {
                    boolean stop = false;
                    while (!stop) {
                        int rand = random.nextInt();
                        if (markers.get(rand) == null) {
                            double lat = random.nextDouble() * (ne.latitude - sw.latitude) + sw.latitude;
                            double lng = random.nextDouble() * (ne.longitude - sw.longitude) + sw.longitude;
                            markers.put(mark, map.addMarker(new MarkerOptions()
                                    .position(new LatLng(lat, lng)).icon(BitmapDescriptorFactory.fromResource(R.drawable.blue_marker))));
                            stop = true;
                        }
                    }
                }
                //.icon(BitmapDescriptorFactory.defaultMarker(random.nextInt(360)))
                //marker.setPosition(new LatLng(lat, lng));
                if (t < 1.0) {
                    // Post again 10ms later.
                    handler.postDelayed(this, 1);
                } else {
                    // animation ended

                }
            }
        });
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
}
