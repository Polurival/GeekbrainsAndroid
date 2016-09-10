package com.github.polurival.shoppinglistclient;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity
        implements ItemsFragment.ItemsFragmentListener,
        DetailFragment.DetailFragmentListener,
        AddEditFragment.AddEditFragmentListener {

    public static final String ITEM_URI = "item_uri";

    private ItemsFragment itemsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null && findViewById(R.id.fragmentContainer) != null) {
            itemsFragment = new ItemsFragment();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragmentContainer, itemsFragment);
            transaction.commit();
        }
    }

    @Override
    public void onItemSelected(Uri itemUri) {
        if (findViewById(R.id.fragmentContainer) != null) {
            displayItem(R.id.fragmentContainer, itemUri);
        }
    }

    @Override
    public void onAddItem() {
        if (findViewById(R.id.fragmentContainer) != null) {
            displayAddEditFragment(R.id.fragmentContainer, null);
        }
    }

    @Override
    public void onItemDeleted() {
        getSupportFragmentManager().popBackStack();
        itemsFragment.updateItemList();
    }

    @Override
    public void onEditItem(Uri itemUri) {
        if (findViewById(R.id.fragmentContainer) != null) {
            displayAddEditFragment(R.id.fragmentContainer, itemUri);
        }
    }

    @Override
    public void onAddEditCompleted(Uri itemUri) {
        getSupportFragmentManager().popBackStack();
        itemsFragment.updateItemList();
        getSupportFragmentManager().popBackStack();
    }

    private void displayItem(int viewID, Uri itemUri) {
        DetailFragment detailFragment = new DetailFragment();

        Bundle args = new Bundle();
        args.putParcelable(ITEM_URI, itemUri);
        detailFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(viewID, detailFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void displayAddEditFragment(int viewId, Uri itemUri) {
        AddEditFragment addEditFragment = new AddEditFragment();

        if (itemUri != null) {
            Bundle args = new Bundle();
            args.putParcelable(ITEM_URI, itemUri);
            addEditFragment.setArguments(args);
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(viewId, addEditFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
