package com.example.puniaraharja.absenjingga;

import android.*;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.example.puniaraharja.absenjingga.AsyncTask.MyAsyncTask;
import com.example.puniaraharja.absenjingga.app.AppConfig;
import com.example.puniaraharja.absenjingga.helper.SessionManager;
import com.example.puniaraharja.absenjingga.helper.UserSQLiteHandler;
import com.example.puniaraharja.absenjingga.persistence.User;
import com.example.puniaraharja.absenjingga.persistence.UserGlobal;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class AbsenFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    TextView statusAbsen,jamMasukTextView,waktuKerjaTextView,buttonAbsen,lokasiTextView;
    private static final int PERMISSION_REQUEST_CODE_LOCATION = 1;
 int state=1;

    final int MSG_START_TIMER = 0;
    final int MSG_STOP_TIMER = 1;
    final int MSG_UPDATE_TIMER = 2;

    StopWatch timer = new StopWatch();
    final int REFRESH_RATE = 100;
    Date waktuMasuk;

    protected GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    double lat,lng;

    private UserSQLiteHandler db;
    private SessionManager session;
    private AdView mAdView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myInflater = inflater.inflate(R.layout.fragment_absen, container, false);

        db = new UserSQLiteHandler(getActivity().getApplicationContext());
        // session manager
        session = new SessionManager(getActivity().getApplicationContext());

        mAdView = (AdView) myInflater.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        statusAbsen = (TextView) myInflater.findViewById(R.id.status);
        jamMasukTextView = (TextView) myInflater.findViewById(R.id.jam_masuk);
        waktuKerjaTextView = (TextView) myInflater.findViewById(R.id.waktu_kerja);
        lokasiTextView =(TextView) myInflater.findViewById(R.id.lokasi);

        buttonAbsen = (TextView) myInflater.findViewById(R.id.button_absen);



        buttonAbsen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(),SwipeActivity.class);
                startActivityForResult(i,1);
            }
        });
        buildGoogleApiClient();
        return myInflater;
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onResume() {
        super.onResume();
        new getStatus(UserGlobal.getUser(getActivity().getApplicationContext())).execute();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case PERMISSION_REQUEST_CODE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setPermissionLocation();
                } else {

                    Toast.makeText(getActivity().getApplicationContext(),"Permission Denied, You cannot access location data.",Toast.LENGTH_LONG).show();

                }
                break;


        }
    }

    public void setConditionLocation()
    {
        if (checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION,getActivity().getApplicationContext(),getActivity())) {
            setPermissionLocation();
        }
        else
        {
            requestPermission(android.Manifest.permission.ACCESS_FINE_LOCATION,PERMISSION_REQUEST_CODE_LOCATION,getActivity().getApplicationContext(),getActivity());
        }
    }
    public  void requestPermission(String strPermission, int perCode, Context _c, Activity _a){
        switch (perCode) {
            case  PERMISSION_REQUEST_CODE_LOCATION:
                if (ActivityCompat.shouldShowRequestPermissionRationale(_a,strPermission)){
                    Toast.makeText(getActivity().getApplicationContext(),"GPS permission allows us to access location data. Please allow in App Settings for additional functionality.",Toast.LENGTH_LONG).show();
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
            new absen(UserGlobal.getUser(getActivity().getApplicationContext()),lat,lng).execute();

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

    private class getStatus extends MyAsyncTask {

        User user;
        String inOut;
        String jamMasuk;
        String lokasi;

        public getStatus(User user)
        {
            this.user =user;
        }




        @Override
        public Context getContext () {
            return getActivity();
        }



        @Override
        public void setSuccessPostExecute() {

            if(inOut.contentEquals("in")) {
                statusAbsen.setText("Bekerja");
                lokasiTextView.setText(lokasi);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat formatter2 = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
                try {
                    waktuMasuk=formatter.parse(jamMasuk);
                    jamMasukTextView.setText(formatter2.format(waktuMasuk));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                buttonAbsen.setBackgroundResource(R.drawable.red);
                buttonAbsen.setText("Absen Pulang");
                state=2;
                mHandler.sendEmptyMessage(MSG_START_TIMER);

            }
            else
            {
                statusAbsen.setText("Off");
                jamMasukTextView.setText("-");
                waktuKerjaTextView.setText("00:00:00");
                lokasiTextView.setText("-");
                buttonAbsen.setBackgroundResource(R.drawable.green);
                buttonAbsen.setText("Absen Masuk");
                state=1;
                mHandler.sendEmptyMessage(MSG_STOP_TIMER);
            }
        }

        @Override
        public void setFailPostExecute() {

        }

        public void postData() {
            String url = AppConfig.getUrlStatus(user.tiket);
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



    private class absen extends MyAsyncTask {

        User user;
        double lat,lng;

        public absen(User user,Double lat, Double lng)
        {
            this.user =user;
            this.lat=lat;
            this.lng=lng;
        }




        @Override
        public Context getContext () {
            return getActivity();
        }



        @Override
        public void setSuccessPostExecute() {
            new getStatus(UserGlobal.getUser(getActivity().getApplicationContext())).execute();
        }

        @Override
        public void setFailPostExecute() {

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        public void postData() {
            //setConditionLocation();
            String url = AppConfig.getUrlAbsen(user.tiket,getDeviceId(getActivity().getApplicationContext()),String.valueOf(lat),String.valueOf(lng));
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

    public static String getDeviceId(Context context)
    {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                setConditionLocation();
            }
        }
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
                    waktuKerjaTextView.setText(twoDatesBetweenTime(waktuMasuk));
                    mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIMER,REFRESH_RATE); //text view is updated every second,
                    break;                                  //though the timer is still running
                case MSG_STOP_TIMER:
                    mHandler.removeMessages(MSG_UPDATE_TIMER); // no more updates.
                    timer.stop();//stop timer
                    waktuKerjaTextView.setText("00:00:00");
                    break;

                default:
                    break;
            }
        }
    };

    public String twoDatesBetweenTime(Date oldtime)
    {
        // TODO Auto-generated method stub
        int day = 0;
        int hh = 0;
        int mm = 0;
        int ss =0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date oldDate = oldtime;
        Date cDate = new Date();
        Long timeDiff = cDate.getTime() - oldDate.getTime();
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


    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }


}
