package com.example.puniaraharja.absenjingga;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.puniaraharja.absenjingga.AsyncTask.MyAsyncTask;
import com.example.puniaraharja.absenjingga.app.AppConfig;
import com.example.puniaraharja.absenjingga.helper.SessionManager;
import com.example.puniaraharja.absenjingga.helper.UserSQLiteHandler;
import com.example.puniaraharja.absenjingga.persistence.User;
import com.example.puniaraharja.absenjingga.persistence.UserGlobal;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.zxing.Result;

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

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class Scan2Activity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private static final int PERMISSION_REQUEST_CODE_CAMERA = 2;

    private ZXingScannerView mScannerView;
    private static RelativeLayout bottomLayout;
    private RecyclerView recyclerView;
    private static LinearLayoutManager mLayoutManager;

    Button scan;
    LinearLayout qrCameraLayout,cam;

    boolean isCam;
    private UserSQLiteHandler db;
    private SessionManager session;

    private AdView mAdView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_scan);
        db = new UserSQLiteHandler(getApplicationContext());
        // session manager
        session = new SessionManager(getApplicationContext());

        mScannerView = new ZXingScannerView(this);
        qrCameraLayout = (LinearLayout) findViewById(R.id.all);
        scan = (Button) findViewById(R.id.scan);
        cam = (LinearLayout) findViewById(R.id.qr);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QrScanner();
            }
        });

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }




    public void QrScanner(){

        qrCameraLayout.removeAllViews();
        qrCameraLayout.addView(mScannerView);

        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
            // Start camera
        setConditionCamera();
        isCam=true;

    }


    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }


    @Override
    public void handleResult(Result rawResult) {

        new getStatus(rawResult.getText()).execute();
        // Do something with the result here
//        qrCameraLayout.removeAllViews();
//        qrCameraLayout.addView(cam);
//        qrCameraLayout.addView(scan);
//
//        Log.e("handler", rawResult.getText()); // Prints scan results
//        Log.e("handler", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode)
//
//        // show the scanner result into dialog box.
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle("Scan Result");
//        builder.setMessage(rawResult.getText());
//        AlertDialog alert1 = builder.create();
//        alert1.show();

        // If you would like to resume scanning, call this method below:
        // mScannerView.resumeCameraPreview(this);
    }

    private class getStatus extends MyAsyncTask {

        User user;
        String qr;
        String inOut;
        String jamMasuk;
        String lokasi;
        String nama;
        String nip;
        String now;
        String foto;

        public getStatus( String qr)
        {

            this.qr=qr;
        }




        @Override
        public Context getContext () {
            return Scan2Activity.this;
        }



        @Override
        public void setSuccessPostExecute() {
            qrCameraLayout.removeAllViews();
            qrCameraLayout.addView(cam);
            qrCameraLayout.addView(scan);

            Intent i = new Intent(Scan2Activity.this,IndentitasActivity.class);
            i.putExtra("nama",nama);
            i.putExtra("nip",nip);
            i.putExtra("status",inOut);
            i.putExtra("lokasi",lokasi);
            i.putExtra("waktu_masuk",jamMasuk);
            i.putExtra("now",now);
            i.putExtra("qr",qr);
            i.putExtra("foto",foto);
            startActivity(i);
        }

        @Override
        public void setFailPostExecute() {
        qrCameraLayout.removeAllViews();
        qrCameraLayout.addView(cam);
        qrCameraLayout.addView(scan);
        }

        public void postData() {
            String url = AppConfig.getUrlStatusQrNoTiket(qr);
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
                            foto=jsonObject.getString("photo");
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

    public  void requestPermission(String strPermission, int perCode, Context _c, Activity _a){
        switch (perCode) {
            case  PERMISSION_REQUEST_CODE_CAMERA:
                if (ActivityCompat.shouldShowRequestPermissionRationale(_a,strPermission)){
                    Toast.makeText(Scan2Activity.this,"Camera permission allows us to access camera. Please allow in App Settings for additional functionality.",Toast.LENGTH_LONG).show();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case PERMISSION_REQUEST_CODE_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setPermissionCamera();
                } else {

                    Toast.makeText(Scan2Activity.this,"Permission Denied, You cannot access camera.",Toast.LENGTH_LONG).show();

                }
                break;


        }
    }

    public void setConditionCamera()
    {
        if (checkPermission(Manifest.permission.CAMERA,getApplicationContext(),Scan2Activity.this)) {
            setPermissionCamera();
        }
        else
        {
            requestPermission(Manifest.permission.CAMERA,PERMISSION_REQUEST_CODE_CAMERA,getApplicationContext(),Scan2Activity.this);
        }
    }

    public  void setPermissionCamera()
    {
        getCamera();
    }

    private void getCamera()
    {
        mScannerView.startCamera();
    }

    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(Scan2Activity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }






}
