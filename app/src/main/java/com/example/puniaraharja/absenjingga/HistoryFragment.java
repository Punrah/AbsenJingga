package com.example.puniaraharja.absenjingga;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.puniaraharja.absenjingga.AsyncTask.MyAsyncTask;
import com.example.puniaraharja.absenjingga.app.AppConfig;
import com.example.puniaraharja.absenjingga.helper.SessionManager;
import com.example.puniaraharja.absenjingga.helper.UserSQLiteHandler;
import com.example.puniaraharja.absenjingga.persistence.Absen;
import com.example.puniaraharja.absenjingga.persistence.UserGlobal;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class HistoryFragment extends Fragment {

    private List<Absen> listItem;
    private static RelativeLayout bottomLayout;
    private RecyclerView recyclerView;
    private AbsenAdapter mAdapter;
    private static LinearLayoutManager mLayoutManager;

    private UserSQLiteHandler db;
    private SessionManager session;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myInflater= inflater.inflate(R.layout.fragment_history, container, false);
        db = new UserSQLiteHandler(getActivity().getApplicationContext());
        // session manager
        session = new SessionManager(getActivity().getApplicationContext());

        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView = (RecyclerView) myInflater.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);


        return myInflater;
    }

    @Override
    public void onResume() {
        super.onResume();
        new fetchOrder().execute();
    }

    private void populate()
    {
        mAdapter = new AbsenAdapter(getActivity(),listItem);
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }


    private class fetchOrder extends MyAsyncTask {


public fetchOrder()
{
    listItem =new ArrayList<>();
}

        @Override
        public Context getContext() {
            return getActivity();
        }

        @Override
        public void setSuccessPostExecute() {

            populate();

        }

        @Override
        public void setFailPostExecute() {

        }

        public void postData() {
            String url = AppConfig.getHistoryUrl(UserGlobal.getUser(getActivity().getApplicationContext()).tiket);
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
                            for(int i=0;i<jsonArray.length();i++)
                            {
                                JSONObject jsonItem = jsonArray.getJSONObject(i);
                                Absen item = new Absen();
                                item.lokasi=jsonItem.getString("lokasi_nama");
                                item.status = jsonItem.getString("absen");
                                item.waktu = jsonItem.getString("waktu");
                                listItem.add(item);
                            }
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
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }


}
