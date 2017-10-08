package com.example.puniaraharja.absenjingga.persistence;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings.Secure;

import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Created by Startup on 1/27/17.
 */

public class User implements Parcelable {
    public  String name;
    public  String position;
    public  String departemen;
    public String photo;
    public String unitKerja;

    public  String tiket;

    public User()
    {
        name="";
        position ="";
        photo ="";
        tiket ="";
        departemen="";
        tiket="";
        unitKerja="";

    }

    public Bitmap getPhoto()
    {
        InputStream is = new ByteArrayInputStream(photo.getBytes(Charset.forName("UTF-8")));
        return BitmapFactory.decodeStream(is);
    }

    public static String getDeviceId(Context context)
    {
        return Secure.getString(context.getContentResolver(),
                Secure.ANDROID_ID);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(name);
        dest.writeString(position);
        dest.writeString(photo);
        dest.writeString(tiket);
        dest.writeString(departemen);
        dest.writeString(tiket);
        dest.writeString(unitKerja);

    }
    // Method to recreate a Question from a Parcel
    public static Creator<User> CREATOR = new Creator<User>() {

        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }

    };

    public User (Parcel parcel) {

        this.name=parcel.readString();
        this.position = parcel.readString();
        this.tiket =parcel.readString();
        this.photo=parcel.readString();
        this.departemen=parcel.readString();
        this.tiket=parcel.readString();
        this.unitKerja=parcel.readString();
    }




}
