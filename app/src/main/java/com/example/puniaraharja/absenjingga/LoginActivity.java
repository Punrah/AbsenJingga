
package com.example.puniaraharja.absenjingga;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.puniaraharja.absenjingga.AsyncTask.MyAsyncTask;
import com.example.puniaraharja.absenjingga.app.AppConfig;
import com.example.puniaraharja.absenjingga.helper.SessionManager;
import com.example.puniaraharja.absenjingga.helper.UserSQLiteHandler;
import com.example.puniaraharja.absenjingga.persistence.User;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private Button btnLogin;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;

    private UserSQLiteHandler db;

    private Button btnQR;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);


        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.button_login);
        btnQR =(Button) findViewById(R.id.button_qr);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

// SQLite database handler
        db = new UserSQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }


        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String email = inputEmail.getText().toString();
                String password = inputPassword.getText().toString();

                signIn(email,password);


            }

        });
        btnQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this,Scan2Activity.class);
                startActivity(i);
            }
        });

    }


    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        new checkLogin(email,password).execute();
    }

    private boolean validateForm() {


        boolean valid = true;

        String email = inputEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            inputEmail.setError("Required.");
            valid = false;
        } else {
            inputEmail.setError(null);
        }

        String password = inputPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            inputPassword.setError("Required.");
            valid = false;
        } else {
            inputPassword.setError(null);
        }

        return valid;
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private class checkLogin extends MyAsyncTask {

        String password;
        String username;
        User user = new User();

        public checkLogin(String username,String password)
        {
            this.username =username;
            this.password=password;
        }




        @Override
        public Context getContext () {
            return LoginActivity.this;
        }



        @Override
        public void setSuccessPostExecute() {
            // user successfully logged in
            // Create login session


            new getProfile(user).execute();
        }

        @Override
        public void setFailPostExecute() {

        }

        public void postData() {
            String url = AppConfig.getUrlLogin(username,password);
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
                        user.tiket = obj.getString("tiket");

                        if (!user.tiket.contentEquals("null")) {
                            isSucces = true;
                        }
                        else {
                            msg="Login tidak berhasil";
                            alertType=DIALOG;
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


    private class getProfile extends MyAsyncTask {

        User user;
        String urlPhoto;

        public getProfile(User user)
        {
            this.user =user;
        }




        @Override
        public Context getContext () {
            return LoginActivity.this;
        }



        @Override
        public void setSuccessPostExecute() {

            session.setLogin(true);
            db.addUser(user);

            Intent intent = new Intent(LoginActivity.this,
                    MainActivity.class);
            startActivity(intent);
            finish();
        }

        @Override
        public void setFailPostExecute() {

        }

        public void postData() {
            String url = AppConfig.getUrlProfile(user.tiket);
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
                            user.name=jsonObject.getString("nama");
                            user.photo=jsonObject.getString("photo");
                            user.position=jsonObject.getString("jabatan");
                            user.departemen=jsonObject.getString("departemen");
                            user.unitKerja = jsonObject.getString("unitkerja");
                        }
                        else {
                            badServerAlert();
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


    private class getImage extends MyAsyncTask {

        User user;
        String url;

        public getImage(User user, String url)
        {
            this.user =user;
            this.url = url;
        }




        @Override
        public Context getContext () {
            return LoginActivity.this;
        }



        @Override
        public void setSuccessPostExecute() {

            session.setLogin(true);
            db.addUser(user);

            Intent intent = new Intent(LoginActivity.this,
                    MainActivity.class);
            startActivity(intent);
            finish();
        }

        @Override
        public void setFailPostExecute() {

        }

        public void postData() {

            user.photo = download_Image(url);
        }

        private String download_Image(String url) {

            String bmp =null;
            try{

                URL ulrn = new URL(url);
                HttpURLConnection con = (HttpURLConnection)ulrn.openConnection();
                InputStream is = con.getInputStream();
                bmp = convertStreamToString(is);
                if (null != bmp)
                    isSucces=true;
                    return bmp;

            }catch(Exception e){
                msg="Foto tidak ada";
                alertType=DIALOG;

            }
            return bmp;
        }


    }
    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }


}
