package com.example.puniaraharja.absenjingga.persistence;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Startup on 2/10/17.
 */

public class ItemOrder implements Parcelable {

    public String idItem;
    public String name;
    public String metric;
    public String description;
    public int price;
    public String photo;
    public int qty;
    public String notes;
    public String ret_gro;
    public ItemOrder()
    {

        idItem="";
        name="";
        metric ="";
        photo="";
        price=0;
        description="";
        qty=0;
        notes="";
        ret_gro="";
    }

    public String getPrice()
    {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatter.format(price)+"/"+metric;
    }



    public String getQty()
    {
        return String.valueOf(qty)+" "+metric;
    }

    public void plusOne()
    {
        qty++;
    }
    public void minOne()
    {
        if(qty>1) {
            qty--;
        }
    }

    public String getItemPriceString() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatter.format(qty*price);
    }



    public int getItemPrice() {
        return qty*price;
    }

    public String getPriceString()
    {
        return String.valueOf(price);
    }

    public static final Creator<ItemOrder> CREATOR = new Creator<ItemOrder>() {
        public ItemOrder createFromParcel(Parcel source) {
            ItemOrder item = new ItemOrder();
            item.idItem = source.readString();
            item.name = source.readString();
            item.metric = source.readString();
            item.description=source.readString();
            item.photo=source.readString();
            item.price=source.readInt();
            item.qty=source.readInt();
            item.notes=source.readString();
            item.ret_gro=source.readString();

            return item;
        }
        public ItemOrder[] newArray(int size) {
            return new ItemOrder[size];
        }
    };

    public int describeContents() {
        return 0;
    }


    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(idItem);
        parcel.writeString(name);
        parcel.writeString(metric);
        parcel.writeString(description);
        parcel.writeString(photo);
        parcel.writeInt(price);
        parcel.writeInt(qty);
        parcel.writeString(notes);
        parcel.writeString(ret_gro);
    }

    public int getItemQty() {
        return qty;
    }
}
