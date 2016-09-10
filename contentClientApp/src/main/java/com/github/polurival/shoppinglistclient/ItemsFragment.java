package com.github.polurival.shoppinglistclient;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static com.github.polurival.shoppinglistclient.ProviderSettings.*;

/**
 * Created by Polurival
 * on 04.09.2016.
 */
public class ItemsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // callback method implemented by MainActivity
    public interface ItemsFragmentListener {

        // called when item selected
        void onItemSelected(Uri itemUri);

        // called when add button is pressed
        void onAddItem();
    }

    private static final int ITEMS_LOADER = 0;

    private ItemsFragmentListener listener;

    private ItemsAdapter itemsAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_items, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getBaseContext()));

        itemsAdapter = new ItemsAdapter(new ItemsAdapter.ItemClickListener() {
            @Override
            public void onClick(Uri itemUri) {
                listener.onItemSelected(itemUri);
            }
        });
        recyclerView.setAdapter(itemsAdapter);
        recyclerView.addItemDecoration(new ItemDivider(getContext()));

        recyclerView.setHasFixedSize(true);

        FloatingActionButton addItemBtn = (FloatingActionButton) view.findViewById(R.id.addItemBtn);
        addItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onAddItem();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(ITEMS_LOADER, null, this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (ItemsFragmentListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ITEMS_LOADER:
                return new CursorLoader(getActivity(),
                        CONTENT_URI, null, null, null, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        itemsAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        itemsAdapter.swapCursor(null);
    }

    public void updateItemList() {
        itemsAdapter.notifyDataSetChanged();
    }
}
