package com.github.polurival.geocoderapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Polurival
 * on 10.09.2016.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "items.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_NAME = "markers";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_HASHCODE = "hashcode";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_ADDRESS = "address";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_ITEMS_TABLE =
                "CREATE TABLE " + TABLE_NAME + "(" +
                        COLUMN_ID + " integer primary key autoincrement, " +
                        COLUMN_HASHCODE + " integer, " +
                        COLUMN_LATITUDE + " real, " +
                        COLUMN_LONGITUDE + " real, " +
                        COLUMN_ADDRESS + " text);";
        db.execSQL(CREATE_ITEMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void insertMarker(Marker marker) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_HASHCODE, marker.getLatLng().hashCode());
        cv.put(COLUMN_LATITUDE, marker.getLatLng().latitude);
        cv.put(COLUMN_LONGITUDE, marker.getLatLng().longitude);
        cv.put(COLUMN_ADDRESS, marker.getAddress());

        this.getWritableDatabase().insert(TABLE_NAME, null, cv);
    }

    public Cursor selectMarkers() {
        return this.getReadableDatabase().query(TABLE_NAME, null, null, null, null, null, null);
    }

    public Cursor selectMarker(int hashcode) {
        return this.getReadableDatabase().query(
                TABLE_NAME, null,
                COLUMN_HASHCODE + " = ?",
                new String[]{String.valueOf(hashcode)},
                null, null, null);
    }
}
