package com.github.polurival.shoppinglistprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Polurival
 * on 04.09.2016.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "items.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_NAME = "items";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SHOP = "shop";
    public static final String COLUMN_COUNT = "count";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        updateDatabase(db, 0, DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateDatabase(db, oldVersion, newVersion);
    }

    private void updateDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            final String CREATE_ITEMS_TABLE =
                    "CREATE TABLE " + TABLE_NAME + "(" +
                            COLUMN_ID + " integer primary key autoincrement, " +
                            COLUMN_NAME + " text, " +
                            COLUMN_SHOP + " text, " +
                            COLUMN_COUNT + " integer);";
            db.execSQL(CREATE_ITEMS_TABLE);
        }
    }
}
