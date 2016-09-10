package com.github.polurival.shoppinglistclient;

import android.net.Uri;

/**
 * Created by Polurival
 * on 05.09.2016.
 */
public interface ProviderSettings {

    String AUTHORITY = "com.github.polurival.shoppinglistprovider";
    String ITEM_PATH = "items";

    Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + ITEM_PATH);

    String COLUMN_ID = "_id";
    String COLUMN_NAME = "name";
    String COLUMN_SHOP = "shop";
    String COLUMN_COUNT = "count";
}
