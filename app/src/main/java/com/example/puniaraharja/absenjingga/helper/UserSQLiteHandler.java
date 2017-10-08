/**
 * Author: Ravi Tamada
 * URL: www.androidhive.info
 * twitter: http://twitter.com/ravitamada
 * */
package com.example.puniaraharja.absenjingga.helper;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.puniaraharja.absenjingga.LoginActivity;
import com.example.puniaraharja.absenjingga.persistence.User;


public class UserSQLiteHandler extends SQLiteOpenHelper {

	private static final String TAG = UserSQLiteHandler.class.getSimpleName();

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 2;

	// Database Name
	private static final String DATABASE_NAME = "AbsenJingga";

	// Login table name
	private static final String TABLE_USER = "user";

	// Login Table Columns names
	private static final String KEY_TIKET = "tiket";
	private static final String KEY_NAME = "name";
	private static final String KEY_POSITION = "position";
	private static final String KEY_PHOTO = "photo";
	private static final String KEY_DEP = "departemen";
	private static final String KEY_UNIT = "unit";
    private UserSQLiteHandler db;
    private SessionManager session;

	public UserSQLiteHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
				+ KEY_TIKET + " TEXT UNIQUE," + KEY_NAME + " TEXT,"
				+ KEY_POSITION + " TEXT," + KEY_PHOTO + " TEXT,"+KEY_UNIT + " TEXT,"+  KEY_DEP + " TEXT" + ")";
		db.execSQL(CREATE_LOGIN_TABLE);

		Log.d(TAG, "Database tables created");
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
		// Create tables again
		onCreate(db);
	}

	/**
	 * Storing user details in database
	 * */
	public void addUser(User user) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_TIKET,user.tiket);
		values.put(KEY_NAME, user.name); // Name
		values.put(KEY_POSITION, user.position); // Email
		values.put(KEY_PHOTO, user.photo);
		values.put(KEY_DEP,user.departemen);
		values.put(KEY_UNIT,user.unitKerja);

		// Inserting Row
		long id = db.insert(TABLE_USER, null, values);
		db.close(); // Closing database connection

		Log.d(TAG, "New user inserted into sqlite: " + id);
	}

	public void updateUser(User user) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_TIKET,user.tiket);
		values.put(KEY_NAME, user.name); // Name
		values.put(KEY_POSITION, user.position); // Email
		values.put(KEY_PHOTO, user.photo);
		values.put(KEY_DEP,user.departemen);// Phone
		values.put(KEY_UNIT,user.unitKerja);
		// Inserting Row
		long id = db.update(TABLE_USER, values, KEY_TIKET +"="+user.tiket,null);
		db.close(); // Closing database connection

		Log.d(TAG, "New user inserted into sqlite: " + id);
	}

	/**
	 * Getting user data from database
	 * */
	public User getUserDetails() {
		User user = new User();
		String selectQuery = "SELECT  * FROM " + TABLE_USER;

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		// Move to first row
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			user.tiket =cursor.getString(cursor.getColumnIndex(KEY_TIKET));
			user.name=cursor.getString(cursor.getColumnIndex(KEY_NAME));
			user.position =cursor.getString(cursor.getColumnIndex(KEY_POSITION));
			user.photo=cursor.getString(cursor.getColumnIndex(KEY_PHOTO));
			user.departemen=cursor.getString(cursor.getColumnIndex(KEY_DEP));
			user.unitKerja=cursor.getString(cursor.getColumnIndex(KEY_UNIT));
		}
		cursor.close();
		db.close();
		// return user
		Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

		return user;
	}

	/**
	 * Re crate database Delete all tables and create them again
	 * */
	public void deleteUsers() {
		SQLiteDatabase db = this.getWritableDatabase();
		// Delete All Rows
		db.delete(TABLE_USER, null, null);
		db.close();

		Log.d(TAG, "Deleted all user info from sqlite");
	}

}
