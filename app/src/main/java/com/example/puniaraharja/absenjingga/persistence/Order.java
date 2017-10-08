package com.example.puniaraharja.absenjingga.persistence;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Startup on 1/31/17.
 */

public class Order  implements Parcelable{

    public User user;
    public String id_order;
    public String orderDate;
    public int price;
    public List<Item> item;
    public int totalPrice;
    public String status;
    public String address;


    public Order()
    {


        item = new ArrayList<Item>();
        user = new User();
        id_order="";
        orderDate="";
        price=0;
        totalPrice=0;
        status="";
        address="";
    }

    public String getRacapPrice()
    {
        int price=0;
        for(int i=0;i<item.size();i++)
        {

            price=price+item.get(i).getItemPrice();
        }
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatter.format(price);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(user,flags);
        dest.writeString(id_order);
        dest.writeString(orderDate);
        dest.writeInt(price);
        dest.writeList(item);
        dest.writeInt(totalPrice);
        dest.writeString(status);
        dest.writeString(address);


    }

    public Order(Parcel parcel) {

        this.user = (User) parcel.readParcelable(User.class.getClassLoader());
        this.id_order=parcel.readString();
        this.orderDate=parcel.readString();
        this.price=parcel.readInt();
        this.item = parcel.readArrayList(Item.class.getClassLoader());
        this.totalPrice=parcel.readInt();
        this.status=parcel.readString();
        this.address=parcel.readString();

    }

    // Method to recreate a Question from a Parcel
    public static Creator<Order> CREATOR = new Creator<Order>() {

        @Override
        public Order createFromParcel(Parcel source) {
            return  new Order(source);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }

    };

   public String getOrderJson()
   {
       Gson gson = new Gson();
       return gson.toJson(item);
   }

    public String getDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Date date= null;
        try {
            date = simpleDateFormat.parse(orderDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd/MM/yyyy");
        return simpleDateFormat2.format(date);

    }




}
