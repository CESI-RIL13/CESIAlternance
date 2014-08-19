package com.kolapsis.utils;

/**
 * Created by Benjamin on 19/08/2014.
 */
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import fr.cesi.alternance.R;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

public class DownloadImageTask extends AsyncTask<Void, Void, Bitmap> {

    private ImageView imageView = null;
    private boolean isRunning = false;

    public DownloadImageTask(ImageView im) {
        imageView = im;
    }

    @Override
    protected Bitmap doInBackground(Void... none) {
        isRunning = true;
        return downloadImage((String) imageView.getTag());
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        imageView.setImageBitmap(result);
        isRunning = false;
        imageView = null;
    }

    private Bitmap downloadImage(String url) {
        Bitmap bm = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
            bis = null;
            is = null;
        } catch (Exception e) {
            bm = BitmapFactory.decodeResource(imageView.getResources(), R.drawable.ic_launcher);
        }
        return bm;
    }

    public boolean isRunning() {
        return isRunning;
    }

}