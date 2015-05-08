package com.roni.haim.nearme;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Hashtable;

/**
 * Created by Haim Omesi & Roni Gonikman on 4/6/15.
 */
@SuppressWarnings("deprecation")
class DBHandler extends AsyncTask<Integer,Long,JSONArray>
{
    private final String user;
    private final String action;
    private final Hashtable<String,String> params;
    private final String callback;
    private final Object senderClass;

    public DBHandler(String user, String action, Hashtable<String,String> params, String callback, Object senderClass) {
        this.user = user;
        this.action = action;
        this.params = params;
        this.callback = callback;
        this.senderClass = senderClass;
    }

    JSONArray Launch(String user, String action, Hashtable<String, String> params)
    {
        String url = "http://nearme.host22.com/dbConnector.php?action="+action+"&user="+user;
        if(params!=null)
        {
            for (String key : params.keySet()) {
                try {
                    url=url+"&params["+key+"]="+ URLEncoder.encode(params.get(key),"UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        // Get HttpResponse Object from url.
        // Get HttpEntity from Http Response Object
        HttpEntity httpEntity = null;

        try
        {
            DefaultHttpClient httpClient = new DefaultHttpClient();  // Default HttpClient
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            httpEntity = httpResponse.getEntity();
        } catch (ClientProtocolException e) {
            // Signals error in http protocol
            e.printStackTrace();
            //Log Errors Here
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Convert HttpEntity into JSON Array
        JSONArray jsonArray = null;

        if (httpEntity != null) {
            try {
                String entityResponse = EntityUtils.toString(httpEntity);
                Log.e("Entity Response  : ", entityResponse);
                jsonArray = new JSONArray(entityResponse);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonArray;
    }

    @Override
    protected JSONArray doInBackground(Integer... params) {
        return Launch(this.user, this.action, this.params);
    }

    @Override
    protected void onPreExecute() {
        try {
            Class noparams[] = {};
            Method method = null;
            method = this.senderClass.getClass().getDeclaredMethod ("startSpinner", noparams);
            method.invoke (this.senderClass);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(JSONArray jsonArray) {
        try {
            Method method = this.senderClass.getClass().getDeclaredMethod (this.callback, JSONArray.class);
            method.invoke (this.senderClass, jsonArray);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
