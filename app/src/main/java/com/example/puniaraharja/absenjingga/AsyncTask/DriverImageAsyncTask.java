package com.example.puniaraharja.absenjingga.AsyncTask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;


import com.example.puniaraharja.absenjingga.app.AppConfig;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Startup on 4/8/17.
 */

public class DriverImageAsyncTask extends AsyncTask<ImageView, Void, Bitmap> {

    ImageView imageView = null;

    @Override
    protected Bitmap doInBackground(ImageView... imageViews) {
        this.imageView = imageViews[0];
        String foto = (String)imageView.getTag();
        return download_Image(AppConfig.URL_DRIVER_IMAGE+foto.replace(" ","%20"));
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if(!(result==null)) {
            imageView.setImageBitmap(result);
        }
    }

    private Bitmap download_Image(String url) {

        Bitmap bmp =null;
        try{
            URL ulrn = new URL(url);
            HttpURLConnection con = (HttpURLConnection)ulrn.openConnection();
            InputStream is = con.getInputStream();
            bmp = BitmapFactory.decodeStream(is);
            if (null != bmp)
                return bmp;

        }catch(Exception e){}
        return bmp;
    }
}