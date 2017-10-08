package com.example.puniaraharja.absenjingga;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.puniaraharja.absenjingga.persistence.Absen;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AbsenAdapter extends RecyclerView.Adapter<AbsenAdapter.MyViewHolder> {

    private List<Absen> itemList;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView lokasi;
        public TextView waktu;
        public ImageView status;

        public MyViewHolder(View view) {
            super(view);
            lokasi = (TextView) view.findViewById(R.id.lokasi);
            waktu = (TextView) view.findViewById(R.id.waktu);
            status = (ImageView) view.findViewById(R.id.status);
        }
    }


    public AbsenAdapter(Context context, List<Absen> moviesList) {
        this.itemList = moviesList;
        this.context=context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_absen, parent, false);


        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final Absen item = itemList.get(position);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat formatter2 = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        Date waktuMasuk;
        try {
            waktuMasuk=formatter.parse(item.waktu);
            holder.waktu.setText(formatter2.format(waktuMasuk));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.lokasi.setText(item.lokasi);

        if(item.status.contentEquals("in")) {
            holder.status.setImageResource(R.drawable.right);
        }
        else if(item.status.contentEquals("out"))
        {
            holder.status.setImageResource(R.drawable.left);
        }

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}