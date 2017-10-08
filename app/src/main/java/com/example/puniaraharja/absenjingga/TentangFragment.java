package com.example.puniaraharja.absenjingga;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class TentangFragment extends Fragment {

TextView avila, jingga, version;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myInflater=inflater.inflate(R.layout.fragment_tentang, container, false);
        avila =(TextView) myInflater.findViewById(R.id.avila);
        jingga=(TextView) myInflater.findViewById(R.id.jingga);
        version=(TextView) myInflater.findViewById(R.id.textView2);

        avila.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.avillahospitality.com"));
                startActivity(browserIntent);
            }
        });

        jingga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.djinggamedia.com"));
                startActivity(browserIntent);
            }
        });

        String ve="Absensi Mobile d-HRSystem V"+BuildConfig.VERSION_NAME;

        version.setText(ve);
        return myInflater;
    }


}
