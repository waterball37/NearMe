package com.roni.haim.nearme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.DialogInterface;

import com.dd.processbutton.iml.ActionProcessButton;

/**
 * A login screen that offers login via email/password.
 */
    public class LoginActivity extends Activity {

    private LinearLayout login_layout;
    private String user;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private String email;
    private String password;
    private ImageView emailIcon;
    private ImageView passIcon;
    private ActionProcessButton mSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.gc();
        setContentView(R.layout.activity_login);
        login_layout = (LinearLayout) findViewById(R.id.login_layout);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        TextView mLogoLabel = (TextView) findViewById(R.id.logoLabel);
        emailIcon = (ImageView) findViewById(R.id.emailIcon);
        passIcon = (ImageView) findViewById(R.id.passIcon);
        TextView mSignUp = (TextView) findViewById(R.id.signUp);

        Typeface mTypeface = Typeface.createFromAsset(getAssets(), "lobster.otf");
        mLogoLabel.setTypeface(mTypeface);
        mPasswordView.setTypeface(mTypeface);
        mEmailView.setTypeface(mTypeface);
        mSignUp.setTypeface(mTypeface);

        mSignIn = (ActionProcessButton) findViewById(R.id.email_sign_in_button);
        mSignIn.setTypeface(mTypeface);
        //mSignIn.setMode(ActionProcessButton.Mode.PROGRESS);
        mSignIn.setMode(ActionProcessButton.Mode.ENDLESS);
        // set progress > 0 to start progress indicator animation
        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //progressGenerator.start(mSignIn);
                //mSignIn.setProgress(0);
                mSignIn.setProgress(1);
                mSignIn.setEnabled(false);
                mPasswordView.setEnabled(false);
                mPasswordView.setEnabled(false);
                mEmailView.setEnabled(false);

                password = mPasswordView.getText().toString();
                email = mEmailView.getText().toString();
                user = email;
                System.out.println("EMAIL " + email);
                System.out.println("PASSWORD " + password);
                //mSignIn.setProgress(25);
                new DBHandler(user, "get_user", null, "getUser", getLoginClass()).execute();
            }
        });
    }

    public LoginActivity getLoginClass()
    {
        return LoginActivity.this;
    }

    @Override
    protected void onDestroy() {
        stripImageView(emailIcon);
        stripImageView(passIcon);
        login_layout.setBackgroundResource(0);
        login_layout = null;
        System.gc();
        super.onDestroy();
    }

    public static void stripImageView(ImageView view) {
        if ( view.getDrawable() instanceof BitmapDrawable) {
            ((BitmapDrawable)view.getDrawable()).getBitmap().recycle();
        }
        view.getDrawable().setCallback(null);
        view.setImageDrawable(null);
        view.getResources().flushLayoutCache();
        view.destroyDrawingCache();
        view.setBackgroundResource(0);
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
        //mSignIn.setProgress(50);
        if(jsonArray == null)
        {
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
            mEmailView.setEnabled(true);
            mPasswordView.setEnabled(true);
            mSignIn.setProgress(0);
            mSignIn.setEnabled(true);
        }
        else{
            JSONObject json = null;
            //for(int i=0; i<jsonArray.length();i++) {
                try {
                    json = jsonArray.getJSONObject(0);
                    String id = json.getString("ID");
                    String pass = json.getString("pass");
                    //mSignIn.setProgress(75);
                    ifExists(id,pass);
                System.out.println(
                        "id: " + json.getString("ID") +
                                "pass: " + json.getString("pass"));


                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }
    }

    public void ifExists(String idDB, String passDB) {
        String passFromDB = passDB;
        //String passFromUser = BCrypt.hashpw(password, BCrypt.gensalt());
        String passFromUser = password;
        //System.out.println("from db"+passFromDB);
        //System.out.println("from user"+passFromUser);
        if (BCrypt.checkpw(passFromUser, passFromDB)) {

            mSignIn.setProgress(100);
            Intent intent = new Intent(this, FeedActivity.class);
            intent.putExtra("USER_ID", mEmailView.getText().toString());
            startActivity(intent);
            finish();
        }
        else {
            mSignIn.setProgress(0);
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
            mEmailView.setEnabled(true);
            mPasswordView.setEnabled(true);
            mSignIn.setEnabled(true);
        }
    }

   public void registration_button(View view)
    {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
        //finish();
    }
}





