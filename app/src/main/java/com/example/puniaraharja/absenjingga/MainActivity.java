package com.example.puniaraharja.absenjingga;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.puniaraharja.absenjingga.AsyncTask.DriverImageAsyncTask;
import com.example.puniaraharja.absenjingga.helper.SessionManager;
import com.example.puniaraharja.absenjingga.helper.UserSQLiteHandler;
import com.example.puniaraharja.absenjingga.persistence.UserGlobal;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int PERMISSION_REQUEST_CODE_LOCATION = 1;

    private ZXingScannerView mScannerView;
    protected GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    double lat,lng;

    TextView name,jabatan,departemen,unit;
    Toolbar toolbar;
    TextView judul;
ImageView foto;

    private UserSQLiteHandler db;
    private SessionManager session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new UserSQLiteHandler(getApplicationContext());
        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
           logoutUser();
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        judul = (TextView) toolbar.findViewById(R.id.judul);

        db = new UserSQLiteHandler(getApplicationContext());
        // session manager
        session = new SessionManager(getApplicationContext());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header=navigationView.getHeaderView(0);
/*View view=navigationView.inflateHeaderView(R.layout.nav_header_main);*/
        foto = (ImageView) header.findViewById(R.id.img);
        name = (TextView)header.findViewById(R.id.name);
        jabatan = (TextView)header.findViewById(R.id.jabatan);
        departemen =(TextView)header.findViewById(R.id.departemen);
        unit =(TextView)header.findViewById(R.id.unit);
        name.setText(UserGlobal.getUser(getApplicationContext()).name);
        jabatan.setText(UserGlobal.getUser(getApplicationContext()).position);
        departemen.setText(UserGlobal.getUser(getApplicationContext()).departemen);
unit.setText(UserGlobal.getUser(getApplicationContext()).unitKerja);
        foto.setTag(UserGlobal.getUser(getApplicationContext()).photo);
        new DriverImageAsyncTask().execute(foto);
        buildGoogleApiClient();
        initiateFragment();
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    private void initiateFragment()
    {
        if (findViewById(R.id.fragment_container) != null) {
            Bundle bundle = new Bundle();
            bundle.putString("lat", String.valueOf(lat));
            bundle.putString("lng", String.valueOf(lng));

            // Create a new Fragment to be placed in the activity layout
            AbsenFragment homeFragment = new AbsenFragment();
            homeFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, homeFragment).commit();
            judul.setText("Absen");

        }
    }

    private void replaceScanFragment()
    {   ScanFragment newFragment = new ScanFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        judul.setText("Scan QR Code");
    }


    private void replaceToAbsenFragment()
    {   Bundle bundle = new Bundle();
        bundle.putString("lat", String.valueOf(lat));
        bundle.putString("lng", String.valueOf(lng));

        // Create a new Fragment to be placed in the activity layout
        AbsenFragment homeFragment = new AbsenFragment();
        homeFragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, homeFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        judul.setText("Absen");
    }
    private void replaceToTentangFragment()
    {   TentangFragment newFragment = new TentangFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        judul.setText("Info");
    }

    private void replaceToHistoryFragment()
    {   HistoryFragment newFragment = new HistoryFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        judul.setText("Absen Terakhir");
    }




    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finish();
        }
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.absen) {
            replaceToAbsenFragment();
        } else if (id == R.id.scan) {
            replaceScanFragment();
        }
        else if(id==R.id.last)
        {
            replaceToHistoryFragment();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.tentang) {
           replaceToTentangFragment();
        }
        else if(id==R.id.logout)
        {
            logoutUser();
        }

        return super.onOptionsItemSelected(item);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case PERMISSION_REQUEST_CODE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setPermissionLocation();
                } else {

                    Toast.makeText(getApplicationContext(),"Permission Denied, You cannot access location data.",Toast.LENGTH_LONG).show();

                }
                break;


        }
    }

    public void setConditionLocation()
    {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,getApplicationContext(),MainActivity.this)) {
            setPermissionLocation();
        }
        else
        {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,PERMISSION_REQUEST_CODE_LOCATION,getApplicationContext(),MainActivity.this);
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
            //Toast.makeText(this, String.valueOf(lat)+" "+String.valueOf(lng)+" "+getDeviceId(this), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        setConditionLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
     //   mGoogleApiClient.connect();
    }

    public static String getDeviceId(Context context)
    {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
