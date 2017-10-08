package com.example.puniaraharja.absenjingga;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.puniaraharja.absenjingga.AsyncTask.DriverImageAsyncTask;
import com.example.puniaraharja.absenjingga.AsyncTask.MyAsyncTask;
import com.example.puniaraharja.absenjingga.app.AppConfig;
import com.example.puniaraharja.absenjingga.helper.SessionManager;
import com.example.puniaraharja.absenjingga.helper.UserSQLiteHandler;
import com.example.puniaraharja.absenjingga.persistence.User;
import com.example.puniaraharja.absenjingga.persistence.UserGlobal;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class IndentitasActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = IndentitasActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CODE_LOCATION = 1;

    final int MSG_START_TIMER = 0;
    final int MSG_STOP_TIMER = 1;
    final int MSG_UPDATE_TIMER = 2;
    int state=1;
    int cameraState =1;
    final int REFRESH_RATE = 100;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    int time=0;

    TextView namaTextView,nipTextView,statusTextView,lokasiTextView,jamMasukTextView,waktu_kerjaTextView;
    TextView camera;
    ImageView imageView;
    Button buttonAbsen;
    StopWatch timer = new StopWatch();

    ImageView fotoImageView;
    String foto;
    String nama;
    String nip;
    String status;
    String lokasi;
    String jamMasuk,qr,photo;
    String now;
    Bitmap bitmapResize;

    protected GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    double lat,lng;

    private UserSQLiteHandler db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indentitas);
        fotoImageView = (ImageView) findViewById(R.id.img);
        namaTextView=(TextView) findViewById(R.id.nama);
        nipTextView=(TextView) findViewById(R.id.nip);
        statusTextView=(TextView) findViewById(R.id.status);
        lokasiTextView=(TextView) findViewById(R.id.lokasi_kerja);
        jamMasukTextView=(TextView) findViewById(R.id.waktu_masuk);
        waktu_kerjaTextView=(TextView) findViewById(R.id.waktu_kerja);

        // ambil foto
        //camera = (TextView) findViewById(R.id.camera);

        imageView =(ImageView) findViewById(R.id.image);
        buttonAbsen =(Button) findViewById(R.id.absen);

         foto=getIntent().getStringExtra("foto");
         nama=getIntent().getStringExtra("nama");
         nip=getIntent().getStringExtra("nip");
         status=getIntent().getStringExtra("status");
         lokasi=getIntent().getStringExtra("lokasi");
         jamMasuk=getIntent().getStringExtra("waktu_masuk");
         now=getIntent().getStringExtra("now");
         qr=getIntent().getStringExtra("qr");

        db = new UserSQLiteHandler(getApplicationContext());
        // session manager
        session = new SessionManager(getApplicationContext());

        buildGoogleApiClient();
        if(status.contentEquals("in")) {

            if(!foto.isEmpty()) {
                fotoImageView.setTag(foto);
                new DriverImageAsyncTask().execute(fotoImageView);
            }
            namaTextView.setText(nama);
            nipTextView.setText(nip);
            statusTextView.setText("Bekerja");
            lokasiTextView.setText(lokasi);
            waktu_kerjaTextView.setText(now);

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat formatter2 = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
            try {
                Date waktuMasuk=formatter.parse(jamMasuk);
                jamMasukTextView.setText(formatter2.format(waktuMasuk));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            state=2;
            buttonState();
            mHandler.sendEmptyMessage(MSG_START_TIMER);

        }
        else
        {
            if(!foto.isEmpty()) {
                fotoImageView.setTag(foto);
                new DriverImageAsyncTask().execute(fotoImageView);
            }
            namaTextView.setText(nama);
            nipTextView.setText(nip);
            statusTextView.setText("Off");
            jamMasukTextView.setText("-");
            waktu_kerjaTextView.setText("00:00:00");
            lokasiTextView.setText("-");

            state=1;
            buttonState();
            mHandler.sendEmptyMessage(MSG_STOP_TIMER);
        }

        // ambil foto
//        camera.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//               dispatchTakePictureIntent();
//            }
//        });

        // without ambil foto
        cameraState=2;
        buttonState();

        buttonAbsen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setConditionLocation();
            }
        });

    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(IndentitasActivity.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }
    public void setConditionLocation()
    {
        if (checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION,getApplicationContext(),IndentitasActivity.this)) {
            setPermissionLocation();
        }
        else
        {
            requestPermission(android.Manifest.permission.ACCESS_FINE_LOCATION,PERMISSION_REQUEST_CODE_LOCATION,getApplicationContext(),IndentitasActivity.this);
        }
    }
    public  void requestPermission(String strPermission, int perCode, Context _c, Activity _a){
        switch (perCode) {
            case  PERMISSION_REQUEST_CODE_LOCATION:
                if (ActivityCompat.shouldShowRequestPermissionRationale(_a,strPermission)){
                    Toast.makeText(getApplicationContext(),"GPS permission allows us to access location data. Please allow in App Settings for additional functionality.",Toast.LENGTH_LONG).show();
                } else {

                    ActivityCompat.requestPermissions(_a,new String[]{strPermission},perCode);
                }
                break;


        }


    }

    public  boolean checkPermission(String strPermission,Context _c,Activity _a){
        int result = ContextCompat.checkSelfPermission(_c, strPermission);
        if (result == PackageManager.PERMISSION_GRANTED){

            return true;

        } else {

            return false;

        }
    }


    public  void setPermissionLocation()
    {
        getMyLocation();
    }

    private void getMyLocation()
    {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {
            lat = mLastLocation.getLatitude();
            lng = mLastLocation.getLongitude();
            new doAbsen(UserGlobal.getUser(getApplicationContext()),qr,lat,lng,bitmapResize).execute();

            //Toast.makeText(getActivity(), String.valueOf(lat)+" "+String.valueOf(lng)+" "+getDeviceId(getActivity().getApplicationContext()), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    private class doAbsen extends MyAsyncTask {

        User user;
        double lat,lng;
        String qr;
        Bitmap bitmap;

        public doAbsen(User user,String qr, Double lat, Double lng, Bitmap photo)
        {
            this.user =user;
            this.qr=qr;
            this.lat=lat;
            this.lng=lng;
            this.bitmap=photo;
        }




        @Override
        public Context getContext () {
            return IndentitasActivity.this;
        }



        @Override
        public void setSuccessPostExecute() {
            new getStatus(UserGlobal.getUser(getApplicationContext()),qr).execute();
            Toast.makeText(IndentitasActivity.this, "sukses", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void setFailPostExecute() {

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        public void postData() {
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
//            byte[] byte_arr = stream.toByteArray();
//            String encodedString = Base64.encodeToString(byte_arr, 0);

            String url = AppConfig.getUrlAbsenQR(user.tiket,qr,String.valueOf(lat),String.valueOf(lng));
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);
            try {
                // Execute HTTP Post Request
                List nameValuePairs = new ArrayList();
                //nameValuePairs.add(new BasicNameValuePair("photo", encodedString));
                nameValuePairs.add(new BasicNameValuePair("photo", ""));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                String jsonStr = EntityUtils.toString(entity, "UTF-8");

                if (jsonStr != null) {
                    try {
                        JSONObject obj = new JSONObject(jsonStr);
                        status = obj.getString("status");

                        if (status.contentEquals("success")) {
                            isSucces = true;
                        }
                        else if(status.contentEquals("error"))
                         {
                            msgTitle="Error";
                            msg=obj.getString("message");
                            alertType=DIALOG_TITLE;
                        }
                        else if(status.contentEquals("loginfailed"))
                        {
                            logoutUser();

                        }
                    } catch (final JSONException e) {
                        badServerAlert();
                    }
                } else {
                    badServerAlert();
                }
            } catch (IOException e) {
                badInternetAlert();
            }
        }


    }



    public void buttonState()
    {
        if(cameraState==1)
        {

            if(state==1)
            {
                buttonAbsen.setBackgroundResource(R.color.gray);
                buttonAbsen.setText("Absen Masuk");
            }
            else if(state==2)
            {
                buttonAbsen.setBackgroundResource(R.color.gray);
                buttonAbsen.setText("Absen Pulang");
            }
        }
        else if(cameraState==2)
        {
            if(state==1)
            {
                buttonAbsen.setBackgroundResource(R.color.green);
                buttonAbsen.setText("Absen Masuk");
            }
            else if(state==2)
            {
                buttonAbsen.setBackgroundResource(R.color.red);
                buttonAbsen.setText("Absen Pulang");
            }
        }
    }



    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
            bitmapResize = resize(imageBitmap, 1000, 1000);
           // Toast.makeText(this, imageBitmap.getByteCount(), Toast.LENGTH_SHORT).show();
            cameraState=2;
            buttonState();

        }
    }
    private static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight <= 0 || maxWidth <= 0) {
            return image;
        }
        float ratioBitmap = ((float) image.getWidth()) / ((float) image.getHeight());
        int finalWidth = maxWidth;
        int finalHeight = maxHeight;
        if (((float) maxWidth) / ((float) maxHeight) > 1.0f) {
            finalWidth = (int) (((float) maxHeight) * ratioBitmap);
        } else {
            finalHeight = (int) (((float) maxWidth) / ratioBitmap);
        }
        return Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
    }

    Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_START_TIMER:
                    timer.start(); //start timer
                    mHandler.sendEmptyMessage(MSG_UPDATE_TIMER);
                    break;

                case MSG_UPDATE_TIMER:
                    waktu_kerjaTextView.setText(twoDatesBetweenTime(jamMasuk,now));
                    mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIMER,REFRESH_RATE); //text view is updated every second,
                    break;                                  //though the timer is still running
                case MSG_STOP_TIMER:
                    mHandler.removeMessages(MSG_UPDATE_TIMER); // no more updates.
                    timer.stop();//stop timer
                    waktu_kerjaTextView.setText("00:00:00");
                    break;

                default:
                    break;
            }
        }
    };

    public String twoDatesBetweenTime(String oldtime, String newTime)
    {
        SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        int day = 0;
        int hh = 0;
        int mm = 0;
        int ss =0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date oldDate = null;
        Date cDate=null;
        try {
            oldDate = formatter2.parse(oldtime);
            cDate = formatter2.parse(newTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        time=time+100;
        Long timeDiff = (cDate.getTime() - oldDate.getTime())+time;
        day = (int) TimeUnit.MILLISECONDS.toDays(timeDiff);
        hh = (int) (TimeUnit.MILLISECONDS.toHours(timeDiff) - TimeUnit.DAYS.toHours(day));
        mm = (int) (TimeUnit.MILLISECONDS.toMinutes(timeDiff) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeDiff)));
        ss = (int) (TimeUnit.MILLISECONDS.toSeconds(timeDiff)- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeDiff)));
        DecimalFormat formatter = new DecimalFormat("00");
        String hhf = formatter.format(hh);
        String mmf = formatter.format(mm);
        String ssf = formatter.format(ss);
        return hhf+":"+mmf+":"+ssf;
    }

    private class getStatus extends MyAsyncTask {

        User user;
        String qr;
        String inOut;
        String jamMasuk;
        String lokasi;
        String foto;
        String nama;
        String nip;
        String now;

        public getStatus(User user, String qr)
        {

            this.user =user;
            this.qr=qr;
        }




        @Override
        public Context getContext () {
            return IndentitasActivity.this;
        }



        @Override
        public void setSuccessPostExecute() {

            Intent i = new Intent(IndentitasActivity.this,IndentitasActivity.class);
            i.putExtra("nama",nama);
            i.putExtra("nip",nip);
            i.putExtra("status",inOut);
            i.putExtra("lokasi",lokasi);
            i.putExtra("waktu_masuk",jamMasuk);
            i.putExtra("now",now);
            i.putExtra("qr",qr);
            i.putExtra("foto",foto);
            startActivity(i);
            finish();
        }

        @Override
        public void setFailPostExecute() {
        }

        public void postData() {
            String url = AppConfig.getUrlStatusQr(user.tiket,qr);
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);
            try {
                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                String jsonStr = EntityUtils.toString(entity, "UTF-8");

                if (jsonStr != null) {
                    try {
                        JSONObject obj = new JSONObject(jsonStr);
                        status = obj.getString("status");

                        if (status.contentEquals("success")) {
                            isSucces = true;
                            JSONArray jsonArray = obj.getJSONArray("result");
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            inOut=jsonObject.getString("absen");
                            jamMasuk=jsonObject.getString("waktu");
                            lokasi=jsonObject.getString("lokasi_nama");
                            now=jsonObject.getString("waktu_sekarang");
                            nama = jsonObject.getString("nama");
                            nip=jsonObject.getString("nip");
                            foto = jsonObject.getString("photo");
                            //foto ="files/fotopegawai/Kadek Widana.jpg";
                        }
                        else if(status.contentEquals("error"))
                        {
                            msgTitle="Error";
                            msg=obj.getString("message");
                            alertType=DIALOG_TITLE;
                        }
                        else if(status.contentEquals("loginfailed"))
                        {
                            logoutUser();

                        }
                    } catch (final JSONException e) {
                        badServerAlert();
                    }
                } else {
                    badServerAlert();
                }
            } catch (IOException e) {
                badInternetAlert();
            }
        }


    }

    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(IndentitasActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


}
