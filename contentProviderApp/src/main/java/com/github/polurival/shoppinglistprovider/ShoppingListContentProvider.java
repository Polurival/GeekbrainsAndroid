package com.github.polurival.shoppinglistprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import static com.github.polurival.shoppinglistprovider.DBHelper.*;

public class ShoppingListContentProvider extends ContentProvider {

    private DBHelper dbHelper;

    public ShoppingListContentProvider() {
    }

    private static final String AUTHORITY = "com.github.polurival.shoppinglistprovider";
    private static final String ITEM_PATH = "items";

    private static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + ITEM_PATH);

    private static final UriMatcher uriMatcher;
    private static final int ONE_ITEM = 1;
    private static final int ITEMS = 2;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, ITEM_PATH + "/#", ONE_ITEM);
        uriMatcher.addURI(AUTHORITY, ITEM_PATH, ITEMS);
    }

    private static final String base_vnd = AUTHORITY + "." + ITEM_PATH;
    private static final String ONE_ITEM_CONTENT_TYPE = "vnd.android.cursor.item/vnd." + base_vnd;
    private static final String ITEMS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + base_vnd;

    @Override
    public boolean onCreate() {
        dbHelper = new DBHelper(getContext());
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case ONE_ITEM:
                return ONE_ITEM_CONTENT_TYPE;
            case ITEMS:
                return ITEMS_CONTENT_TYPE;
        }
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        if (uriMatcher.match(uri) != ITEMS) {
            throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        Uri newItemUri = null;
        long rowID = dbHelper.getWritableDatabase().insert(TABLE_NAME, null, values);
        if (rowID > 0) {
            newItemUri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(newItemUri, null);
        } else {
            throw new SQLException("Insert failed: " + uri);
        }

        return newItemUri;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        String[] args = getArgs(uri, selection, sortOrder);
        Cursor cursor = dbHelper.getReadableDatabase()
                .query(TABLE_NAME, projection, args[0], selectionArgs, null, null, args[1]);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        String[] args = getArgs(uri, selection, null);
        int numberOfRowsUpdated = dbHelper.getWritableDatabase()
                .update(TABLE_NAME, values, args[0], selectionArgs);
        if (numberOfRowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numberOfRowsUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        String[] args = getArgs(uri, selection, null);
        int numberOfRowsDeleted =
                dbHelper.getWritableDatabase().delete(TABLE_NAME, args[0], selectionArgs);
        if (numberOfRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numberOfRowsDeleted;
    }

    private String[] getArgs(Uri uri, String selection, String sortOrder) {
        switch (uriMatcher.match(uri)) {
            case ONE_ITEM:
                String id = uri.getLastPathSegment();
                String idAddition = COLUMN_ID + " = " + id;
                if (TextUtils.isEmpty(selection)) {
                    selection = idAddition;
                } else {
                    selection += " AND " + idAddition;
                }
                break;
            case ITEMS:
                if (sortOrder != null) {
                    if (TextUtils.isEmpty(sortOrder)) {
                        sortOrder = COLUMN_NAME + " ASC";
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        return new String[]{selection, sortOrder};
    }
}
