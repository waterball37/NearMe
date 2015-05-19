package com.roni.haim.nearme;

import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by Haim Omesi & Roni Gonikman on 4/6/15.
 */
@SuppressWarnings("deprecation")
class DBHandler extends AsyncTask<Integer,Long,JSONArray>
{
    private String user;
    private String action;
    private Hashtable<String,String> params;
    private String callback;
    private WeakReference<Object> senderClass;

    public DBHandler(String user, String action, Hashtable<String,String> params, String callback, Object senderClass) {
        this.user = user;
        this.action = action;
        this.params = params;
        this.callback = callback;
        this.senderClass = new WeakReference<>(senderClass);
    }

    private JSONArray Launch(String user, String action, Hashtable<String, String> params)
    {
        String url = "http://nearme.host22.com/dbConnector.php";
        List<NameValuePair> nameValuePair = new ArrayList<>();
        nameValuePair.add(new BasicNameValuePair("action", action));
        nameValuePair.add(new BasicNameValuePair("user", user));
        if(params!=null) {
            for (String key : params.keySet()) {
                nameValuePair.add(new BasicNameValuePair("params[" + key + "]", params.get(key)));
            }
        }
        // Get HttpResponse Object from url.
        // Get HttpEntity from Http Response Object
        HttpEntity httpEntity = null;

        try
        {
            DefaultHttpClient httpClient = new DefaultHttpClient();  // Default HttpClient
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair,"UTF-8"));
            //HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpPost);
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
            } catch (JSONException | IOException e) {
                e.printStackTrace();
                return null;
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
            if(senderClass != null)
            {
                Object sender = senderClass.get();
                if(sender != null)
                {
                    Class noparams[] = {};
                    Method method = null;
                    method = sender.getClass().getDeclaredMethod ("startSpinner", noparams);
                    method.invoke (sender);
                }
            }

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
            if(senderClass != null)
            {
                Object sender = senderClass.get();
                senderClass=null;
                if(sender != null)
                {
                    params = null;
                    Method method = sender.getClass().getDeclaredMethod (this.callback, JSONArray.class);
                    method.invoke (sender, jsonArray);
                }
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
