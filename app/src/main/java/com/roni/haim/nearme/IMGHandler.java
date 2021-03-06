package com.roni.haim.nearme;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;

/**
 * Created by Haim Omesi & Roni Gonikman on 4/6/15.
 */
class IMGHandler extends AsyncTask<Integer, Void, Bitmap> {

    private final WeakReference<ImageView> imageViewReference;
    private final String url;
    private final WeakReference<Object> senderClass;

    public IMGHandler(ImageView imageView,String url,Object senderClass) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        this.imageViewReference = new WeakReference<>(imageView);
        this.senderClass = new WeakReference<>(senderClass);
        this.url = url;
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    Bitmap decodeSampledBitmapFromResource(int reqWidth, int reqHeight) {

        try {

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            //URL url = new URL(this.url);
            if(BitmapFactory.decodeStream((InputStream)new URL(this.url).getContent(), null, options)==null)
                return null;
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream((InputStream)new URL(this.url).getContent(), null, options);

            //Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(this.url).getContent());
            //return bitmap;

        } catch (IOException e) {
            Log.e("IMGHandler", "Image UnAvailable");
            return null;
            //e.printStackTrace();
        }
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(Integer... params) {
        return decodeSampledBitmapFromResource(params[0],params[0]);
    }

    /*
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
    */

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if(bitmap != null)
        {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
        /*
        try {
            Class noparams[] = {};
            Method method = null;
            method = this.senderClass.getClass().getDeclaredMethod ("decrementComponentsProcessed", noparams );
            method.invoke (this.senderClass);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        */
    }
}