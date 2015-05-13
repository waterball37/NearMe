package com.roni.haim.nearme;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import android.app.DialogFragment;
import 	android.app.AlertDialog.Builder;
import android.content.DialogInterface;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {

    // UI references.
    private String user;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    //private boolean ans = false;
    private String email;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
    }

    public void sign_in(View view) {

        password = mPasswordView.getText().toString();
        email = mEmailView.getText().toString();
        this.user = email;
        System.out.println("EMAIL " + email);
        System.out.println("PASSWORD " + password);
        new DBHandler(this.user, "get_user", null, "getUser", this).execute();

    }


    public void getUser(JSONArray jsonArray) {

        if(jsonArray == null)
        {
            //ans = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("USER DOES NOT EXIST");
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    return;
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            mEmailView.setText("");
            mPasswordView.setText("");
        }
        else{
            JSONObject json = null;
            //for(int i=0; i<jsonArray.length();i++) {
                try {
                    json = jsonArray.getJSONObject(0);
                    String id = json.getString("ID");
                    String pass = json.getString("pass");
                    ifExists(id,pass);
                System.out.println(
                        "id: " + json.getString("ID") +
                                "pass: " + json.getString("pass"));


                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }



        //}

    }

    public void ifExists(String idDB, String passDB) {
        String passFromDB = passDB;
        //String passFromUser = BCrypt.hashpw(password, BCrypt.gensalt());
        String passFromUser = password;
        //System.out.println("from db"+passFromDB);
        //System.out.println("from user"+passFromUser);
        if (BCrypt.checkpw(passFromUser, passFromDB)) {
            //ans = true;
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("WRONG PASSWORD");
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    return;
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            mEmailView.setText("");
            mPasswordView.setText("");

        }
    }

    public void check(String id,String pass)
    {
        mEmailView.setError(null);
        mPasswordView.setError(null);
        if (TextUtils.isEmpty(pass) || !isPasswordValid(pass))
            mPasswordView.setError(getString(R.string.error_invalid_password));
        if(TextUtils.isEmpty(id) || isEmailValid(id))
            mEmailView.setError(getString(R.string.error_invalid_email));
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 5;
    }


    }





