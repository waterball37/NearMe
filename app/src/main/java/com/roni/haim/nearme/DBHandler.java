package com.roni.haim.nearme;

import android.os.AsyncTask;
import org.json.JSONArray;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;

/**
 * Created by Haim Omesi & Roni Gonikman on 4/6/15.
 */
public class DBHandler extends AsyncTask<ApiConnector,Long,JSONArray>
{
    private String user;
    private String action;
    private Hashtable<String,String> params;
    private String callback;
    private Object senderClass;

    public DBHandler(String user, String action, Hashtable<String,String> params, String callback, Object senderClass) {
        this.user = user;
        this.action = action;
        this.params = params;
        this.callback = callback;
        this.senderClass = senderClass;
    }

    @Override
    protected JSONArray doInBackground(ApiConnector... params) {
        return params[0].Launch(this.user, this.action,this.params);
    }

    @Override
    protected void onPostExecute(JSONArray jsonArray) {
        try {
            Class[] pTypes = new Class[1];
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
