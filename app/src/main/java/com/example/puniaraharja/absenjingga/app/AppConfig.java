package com.example.puniaraharja.absenjingga.app;

public class AppConfig {


    public static final String URL_DRIVER_IMAGE ="http://system.avillahospitality.com/" ;
    public static final String URL_IMAGE = "http://system.avillahospitality.com/";

    public  static String getUrlLogin(String username, String password)
    {
       String username2= username.replace(" ","%20");
        String password2=password.replace(" ","%20");
        return "http://system.avillahospitality.com/index.php?pagetype=service&user=login&username="+username2+"&pass="+password2;
    }

    public static String getUrlProfile(String tiket)
    {
        tiket.replace(" ","%20");
        return "http://system.avillahospitality.com/index.php?language=id&domain=&pagetype=service&page=Web-Service-Absen&action=getProfilPegawai&tiket="+tiket;
    }

    public static String getUrlStatus(String tiket)
    {
        tiket.replace(" ","%20");
        return "http://system.avillahospitality.com/index.php?language=id&domain=&pagetype=service&page=Web-Service-Absen&action=getLastAbsen&tiket="+tiket;
    }


    public static String getUrlAbsen(String tiket,String devid,String lat,String lng) {
        return "http://system.avillahospitality.com/index.php?language=id&domain=&pagetype=service&page=Web-Service-Absen&action=doAbsen&tiket="+tiket+"&deviceId="+devid+"&lat="+lat+"&lng="+lng;

    }

    public static String getUrlStatusQr(String tiket,String qr) {
        return "http://system.avillahospitality.com/index.php?language=id&domain=&pagetype=service&page=Web-Service-Absen&action=getProfilPegawaiFromQRCode&qrcode="+qr+"&tiket="+tiket;
    }

    public static String getUrlStatusQrNoTiket(String qr) {
        return "http://system.avillahospitality.com/index.php?language=id&domain=&pagetype=service&page=Web-Service-Absen&action=getProfilPegawaiFromQRCode&qrcode="+qr;
    }

    public static String getUrlAbsenQR(String tiket, String qr, String lat, String lng) {
        return "http://system.avillahospitality.com/index.php?language=id&domain=&pagetype=service&page=Web-Service-Absen&action=doAbsenQRCode&tiket="+tiket+"&qrcode="+qr+"&lat="+lat+"&lng="+lng;
    }

    public static String getUrlAbsenQRNoTiket( String qr, String lat, String lng) {
        return "http://system.avillahospitality.com/index.php?language=id&domain=&pagetype=service&page=Web-Service-Absen&action=doAbsenQRCode&qrcode="+qr+"&lat="+lat+"&lng="+lng;
    }
    public static  String getHistoryUrl(String tiket)
    {
        return "http://system.avillahospitality.com/index.php?language=id&domain=&pagetype=service&page=Web-Service-Absen&action=getLastWeekAbsen&tiket="+tiket;
    }
}
