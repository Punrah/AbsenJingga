package com.example.puniaraharja.absenjingga.persistence;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Startup on 2/10/17.
 */

public class Absen implements Parcelable {

    public String status;
    public String waktu;
    public String lokasi;;
    public Absen()
    {

        status="";
        waktu="";
        lokasi ="";
    }


    public static final Creator<Absen> CREATOR = new Creator<Absen>() {
        public Absen createFromParcel(Parcel source) {
            Absen item = new Absen();
            item.status = source.readString();
            item.lokasi = source.readString();
            item.waktu = source.readString();

            return item;
        }
        public Absen[] newArray(int size) {
            return new Absen[size];
        }
    };

    public int describeContents() {
        return 0;
    }


    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(status);
        parcel.writeString(lokasi);
        parcel.writeString(waktu);
    }

}
