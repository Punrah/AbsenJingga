package com.example.puniaraharja.absenjingga.persistence;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Startup on 2/10/17.
 */

public class Item implements Parcelable {

    public String idItem;
    public String name;
    public String metric_retail;
    public String metric_grosir;
    public String description;
    public int price_retail;
    public int price_grosir;
    public String photo;
    public int qty;
    public String notes;
    public String retGro;
    public String contains;
    public Item()
    {

        idItem="";
        name="";
        metric_retail ="";
        metric_grosir ="";
        photo="";
        price_retail=0;
        price_grosir=0;
        description="";
        qty=0;
        notes="";
        retGro = "retail";
        contains="";
    }

    public String getPrice() {
        String price = "";
        if (retGro.contentEquals("retail"))
        {

            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        price= formatter.format(price_retail) + "/" + metric_retail;
    }
    else if(retGro.contentEquals("grosir"))
        {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            price= formatter.format(price_grosir) + "/" + metric_grosir;
        }
        return price;
    }



    public String getQty()
    {
        String quan = "";
        if (retGro.contentEquals("retail"))
        {

            quan=String.valueOf(qty)+" "+metric_retail;
        }
        else if(retGro.contentEquals("grosir"))
        {
            quan= String.valueOf(qty)+" "+metric_grosir;
        }
        return quan;
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

        String price = "";
        if (retGro.contentEquals("retail"))
        {

            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            price= formatter.format(qty*price_retail);
        }
        else if(retGro.contentEquals("grosir"))
        {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            price= formatter.format(qty*price_grosir);
        }
        return price;

    }

    public int getItemPrice() {
        int price=0;
        if(retGro.contentEquals("retail")) {
            price= qty * price_retail;
        }
        else if(retGro.contentEquals("grosir"))
        {
            price= qty * price_grosir;
        }
        return price;
    }

    public String getPriceStringRetail()
    {
        return String.valueOf(price_retail);
    }
    public String getPriceStringGrosir()
    {
        return String.valueOf(price_grosir);
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        public Item createFromParcel(Parcel source) {
            Item item = new Item();
            item.idItem = source.readString();
            item.name = source.readString();
            item.metric_retail = source.readString();
            item.metric_grosir = source.readString();
            item.description=source.readString();
            item.photo=source.readString();
            item.price_retail=source.readInt();
            item.price_grosir=source.readInt();
            item.qty=source.readInt();
            item.notes=source.readString();
            item.retGro=source.readString();
            item.contains=source.readString();

            return item;
        }
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    public int describeContents() {
        return 0;
    }


    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(idItem);
        parcel.writeString(name);
        parcel.writeString(metric_retail);
        parcel.writeString(metric_grosir);
        parcel.writeString(description);
        parcel.writeString(photo);
        parcel.writeInt(price_retail);
        parcel.writeInt(price_grosir);
        parcel.writeInt(qty);
        parcel.writeString(notes);
        parcel.writeString(retGro);
        parcel.writeString(contains);
    }

    public int getItemQty() {
        return qty;
    }

    public void setContains()
    {
        contains=idItem+retGro;
    }
}
