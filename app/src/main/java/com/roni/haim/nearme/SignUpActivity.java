package com.roni.haim.nearme;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;


public class SignUpActivity extends ActionBarActivity {

    private EditText userID;
    private EditText userPass;
    private EditText userPassConfirm;
    private EditText userFullName;
    private ImageView viewImage;
    private Button b;
    //private boolean ans = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        userFullName = (EditText)findViewById(R.id.editTextFullName);
        userID = (EditText)findViewById(R.id.editTextUserName);
        userPass = (EditText)findViewById(R.id.editTextPassword);
        userPassConfirm = (EditText)findViewById(R.id.editTextConfirmPassword);
        viewImage = (ImageView)findViewById(R.id.viewImage);
        b=(Button)findViewById(R.id.btnSelectPhoto);
        //viewImage=(ImageView)findViewById(R.id.viewImage);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds options to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    private void selectImage() {

        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo"))
                {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, 1);
                }
                else if (options[item].equals("Choose from Gallery"))
                {
                    Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);

                }
                else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                File f = new File(Environment.getExternalStorageDirectory().toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("no_user.png")) {
                        f = temp;
                        break;
                    }
                }
                try {
                    Bitmap bitmap;
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

                    bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(),
                            bitmapOptions);

                    viewImage.setImageBitmap(bitmap);

                    String path = android.os.Environment
                            .getExternalStorageDirectory()
                            + File.separator
                            + "Phoenix" + File.separator + "default";
                    f.delete();
                    OutputStream outFile = null;
                    File file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
                    try {
                        outFile = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
                        outFile.flush();
                        outFile.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == 2) {

                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                Bitmap thumbnail = null;
                    //BitmapFactory.Options options = new BitmapFactory.Options();
                    //thumbnail = BitmapFactory.decodeStream(new java.net.URL(picturePath).openConnection().getInputStream(), null, options);
                    thumbnail = BitmapFactory.decodeFile(picturePath);
                //Log.w("path of image from gallery......******************.........", picturePath + "");
                viewImage.setImageBitmap(thumbnail);
                //b.setVisibility(View.GONE);
            }
        }

    }

    public void registration(View view)
    {
        userID.setError(null);
        userPass.setError(null);
        userPassConfirm.setError(null);
        String user = userID.getText().toString();
        String pass = userPass.getText().toString();
        new DBHandler(user, "get_user", null, "getUser", this).execute();

       /* Bitmap bitmap = ((BitmapDrawable)viewImage.getDrawable()).getBitmap();
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageInByte=stream.toByteArray();
        Hashtable<String,String> params = new Hashtable<String,String>();
        params.put("image",imageInByte.toString());*/




        //new DBHandler(user, "set_image", params, "setUser", this).execute();
        //HttpFileUpload upload = new HttpFileUpload();
        //upload.uploadFile(user);
    }

    public void getUser(JSONArray jsonArray) {

        if (jsonArray == null) {

                String user = userID.getText().toString();
                String pass = userPass.getText().toString();
                String userName = userFullName.getText().toString();
                String passConfirm = userPassConfirm.getText().toString();
                if(pass.equals(passConfirm)) {
                    Hashtable<String,String> params = new Hashtable<String,String>();
                    params.put("name",userName);
                    params.put("ID",user);
                    params.put("pass",BCrypt.hashpw(pass, BCrypt.gensalt()));
                    new DBHandler(user,"set_user",params,"setUser",this).execute();
                }
        } else {
            JSONObject json = null;
            try {
                json = jsonArray.getJSONObject(0);
                String id = json.getString("ID");
                String pass = json.getString("pass");
                System.out.println(
                        "id: " + json.getString("ID") +
                                "pass: " + json.getString("pass"));


            } catch (JSONException e) {
                e.printStackTrace();
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("USER EXISTS");
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    return;
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
    public void setUser(JSONArray jsonArray) {
        for(int i=0; i<jsonArray.length();i++) {
            JSONObject json = null;
            try {
                json = jsonArray.getJSONObject(i);
                String result = json.getString("result");
                if (result.equals("success")) {
                    String user = userID.getText().toString();
                    Bitmap bitmap = ((BitmapDrawable) viewImage.getDrawable()).getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    //byte[] imageInByte=stream.toByteArray();
                    Hashtable<String, String> params = new Hashtable<String, String>();
                    params.put("image", Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT));
                    new DBHandler(user, "set_image", params, "setImage", this).execute();
                } else
                    System.out.println("error");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void setImage(JSONArray jsonArray) {
        for(int i=0; i<jsonArray.length();i++) {
            JSONObject json = null;
            try {
                json = jsonArray.getJSONObject(i);
                String result = json.getString("result");
                if (result.equals("success")) {
                    System.out.println("success");
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                } else
                    System.out.println("error");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


   /* public void check(String id,String pass)
    {
        mEmailView.setError(null);
        mPasswordView.setError(null);
        if (TextUtils.isEmpty(pass) || !isPasswordValid(pass))
            mPasswordView.setError(getString(R.string.error_invalid_password));
        if(TextUtils.isEmpty(id) || isEmailValid(id))
            mEmailView.setError(getString(R.string.error_invalid_email));
    }*/

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 5;
    }
}
