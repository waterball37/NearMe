package com.roni.haim.nearme;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

public class NewEventFragment extends Fragment {

    private static int MAX_IMAGE_DIMENSION = 80;
    private int RESULT_LOAD_IMG = 1;
    private int RESULT_CAMERA_IMG = 2;
    private String imgDecodableString ="";
    private EditText name;
    private EditText add;
    private Spinner interests;
    private ImageView image;
    private ImageButton deleteImage;
    private View frag;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        frag = null;
        System.gc();
        // not cleaning up.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "lobster.otf");
        View frag = (View)inflater.inflate(R.layout.fragment_new_event, container, false);
        add = (EditText)frag.findViewById(R.id.address);
        TextView addLabel = (TextView)frag.findViewById(R.id.address_label);
        TextView interest = (TextView)frag.findViewById(R.id.interest);
        interests = (Spinner)frag.findViewById(R.id.interests);
        TextView nameLabel = (TextView)frag.findViewById(R.id.name_label);
        TextView imageLabel = (TextView)frag.findViewById(R.id.images);
        name = (EditText)frag.findViewById(R.id.name);
        Button post = (Button)frag.findViewById(R.id.post);
        ImageButton addImageCamera = (ImageButton)frag.findViewById(R.id.add_image_camera);
        ImageButton addImageGallery = (ImageButton)frag.findViewById(R.id.add_image_gallery);
        deleteImage = (ImageButton)frag.findViewById(R.id.image_delete);
        image = (ImageView)frag.findViewById(R.id.image);
        LatLng loc = ((FeedActivity) getActivity()).getLatLng();
        String address="";
        Geocoder geoCoder = new Geocoder(getActivity(), Locale.ENGLISH);
        List<Address> addresses = null;
        try {
            addresses = geoCoder.getFromLocation(
                    loc.latitude,
                    loc.longitude, 1);
        } catch (IOException e) {
            ((FeedActivity) getActivity()).toast("Can't determine address");
            e.printStackTrace();
        }

        if (addresses.size() > 0) {
            for (int index = 0;
                 index <= addresses.get(0).getMaxAddressLineIndex(); index++)
                address += addresses.get(0).getAddressLine(index) + " ";
        }
        else
            ((FeedActivity) getActivity()).toast("Can't determine address");

        //new DBHandler("dummyUser","get_interests",null,"setInterests",((FeedActivity)this.getActivity()).getFeedClass()).execute();
        String[] items = new String[]{"Alcohol", "Animals", "Art", "Business", "Cinema", "Food", "Music", "Night Life", "Sport", "Theater"};
        ArrayList<String> values = new ArrayList<>(Arrays.asList(items));
        MyCustomAdapter adapter = new MyCustomAdapter(getActivity().getBaseContext(), values);
        interests.setAdapter(adapter);

        addLabel.setTypeface(myTypeface);
        add.setText(address);
        add.setTypeface(myTypeface);
        interest.setTypeface(myTypeface);
        nameLabel.setTypeface(myTypeface);
        name.setTypeface(myTypeface);
        imageLabel.setTypeface(myTypeface);

        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name.setText("");
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng loc = ((FeedActivity) getActivity()).getLatLng();
                Hashtable<String,String> params = new Hashtable<String,String>();
                params.put("name",name.getText().toString());
                params.put("interests",interests.getSelectedItem().toString());
                params.put("address", add.getText().toString());
                params.put("lat", String.valueOf(loc.latitude));
                params.put("lng",String.valueOf(loc.longitude));
                new DBHandler("dummyUser","set_event",params,"setEvent",((FeedActivity) getActivity()).getFeedClass()).execute();
            }
        });

        addImageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(cameraIntent, RESULT_CAMERA_IMG);
                }
            }
        });

        addImageGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create intent to Open Image applications like Gallery, Google Photos
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                // Start the Intent
                startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
            }
        });

        deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image.setImageDrawable(null);
                deleteImage.setVisibility(View.INVISIBLE);
                System.gc();
            }
        });

        return frag;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA , MediaStore.Images.ImageColumns.ORIENTATION};

                // Get the cursor
                Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                int orientationColumnIndex = cursor.getColumnIndex(filePathColumn[1]);
                imgDecodableString = cursor.getString(columnIndex);
                int orientation = cursor.getInt(orientationColumnIndex);
                cursor.close();
                BitmapFactory.Options dbo = new BitmapFactory.Options();
                dbo.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imgDecodableString,dbo);

                int rotatedWidth, rotatedHeight;

                if (orientation == 90 || orientation == 270) {
                    rotatedWidth = dbo.outHeight;
                    rotatedHeight = dbo.outWidth;
                } else {
                    rotatedWidth = dbo.outWidth;
                    rotatedHeight = dbo.outHeight;
                }

                Bitmap rawImage = null;

                if (rotatedWidth > MAX_IMAGE_DIMENSION || rotatedHeight > MAX_IMAGE_DIMENSION) {
                    float widthRatio = ((float) rotatedWidth) / ((float) MAX_IMAGE_DIMENSION);
                    float heightRatio = ((float) rotatedHeight) / ((float) MAX_IMAGE_DIMENSION);
                    float maxRatio = Math.max(widthRatio, heightRatio);

                    // Create the bitmap from file
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = (int) maxRatio;
                    rawImage = BitmapFactory.decodeFile(imgDecodableString, options);
                } else {
                    rawImage = BitmapFactory.decodeFile(imgDecodableString);
                }

                Bitmap desImage = null;
                imgDecodableString = null;
                if (orientation > 0) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(orientation);
                    //matrix.postScale(4, 4);
                    //desImage = Bitmap.createScaledBitmap(rawImage, 80, 80, false);
                    desImage = Bitmap.createBitmap(rawImage, 0, 0, rawImage.getWidth(),
                            rawImage.getHeight(), matrix, true);
                }
                //Bitmap desImage = Bitmap.createScaledBitmap(rawImage, 80, 80, false);
                image.setImageBitmap(desImage);
                deleteImage.setVisibility(View.VISIBLE);

                rawImage.recycle();
                System.gc();

            } else if(requestCode == RESULT_CAMERA_IMG
                    && null != data){
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                //String pathToImage = mImageCaptureUri.getPath();
                String result = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), imageBitmap, "", "");

                Uri imageFileUri = Uri.parse(result);
                File imageFile = new File(result);
                ExifInterface exif = new ExifInterface(
                        imageFile.getAbsolutePath());
                int orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);
                int rotate = 0;
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotate = 270;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotate = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotate = 90;
                        break;
                }

                String[] filePathColumn = { MediaStore.Images.ImageColumns.ORIENTATION};
                // Get the cursor
                Cursor cursor = getActivity().getContentResolver().query(imageFileUri,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();
                int orientationColumnIndex = cursor.getColumnIndex(filePathColumn[0]);
                //int orientation = cursor.getInt(orientationColumnIndex);
                cursor.close();
                image.setImageBitmap(imageBitmap);
                deleteImage.setVisibility(View.VISIBLE);
            }
            else
            {
                Toast.makeText(getActivity(), "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_LONG)
                    .show();
            e.printStackTrace();
        }
    }

    class MyCustomAdapter extends ArrayAdapter<String> {

        Context context;
        ArrayList<String> list;
        private int defaultPosition;

        public int getDefaultPosition() {
            return defaultPosition;
        }

        public MyCustomAdapter(Context context, ArrayList<String> objects) {
            super(context, 0, objects);
            this.context = context;
            list = objects;
        }

        public void setDefaultPostion(int position) {
            this.defaultPosition = position;
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustom(position, convertView, parent);
        }

        public View getCustom(int position, View convertView, ViewGroup parent) {

            View row = LayoutInflater.from(context).inflate(
                    R.layout.interests_item, parent, false);

            TextView interestName = (TextView) row.findViewById(R.id.interestName);
            TextView interestColor = (TextView) row.findViewById(R.id.interestColor);
            Typeface tf = Typeface.createFromAsset(context.getAssets(), "lobster.otf");
            interestName.setTypeface(tf);
            interestName.setText(list.get(position));
            String color = "";
            switch (list.get(position)) {
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
            interestColor.setBackgroundColor(Color.parseColor(color));

            return row;
        }

        public View getCustomView(int position, View convertView,
                                  ViewGroup parent) {

            View row = LayoutInflater.from(context).inflate(
                    R.layout.interests_item, parent, false);

            TextView interestName = (TextView) row.findViewById(R.id.interestName);
            TextView interestColor = (TextView) row.findViewById(R.id.interestColor);
            Typeface tf = Typeface.createFromAsset(context.getAssets(), "lobster.otf");
            interestName.setTypeface(tf);
            interestName.setText(list.get(position));
            String color = "";
            switch (list.get(position)) {
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
            interestColor.setBackgroundColor(Color.parseColor(color));

            return row;
        }
    }
}
