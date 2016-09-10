package com.github.polurival.shoppinglistclient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static com.github.polurival.shoppinglistclient.ProviderSettings.*;

/**
 * Created by Polurival
 * on 04.09.2016.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // callback methods implemented by MainActivity
    public interface DetailFragmentListener {

        // called when an item is deleted
        void onItemDeleted();

        // pass Uri of item to edit to the DetailFragmentListener
        void onEditItem(Uri itemUri);
    }

    private static final int ITEM_LOADER = 0;

    private static DetailFragmentListener listener;
    private static Uri itemUri;

    private TextView nameTextView;
    private TextView shopTextView;
    private TextView countTextView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (DetailFragmentListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        if (args != null) {
            itemUri = args.getParcelable(MainActivity.ITEM_URI);
        }

        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        nameTextView = (TextView) view.findViewById(R.id.nameTextView);
        shopTextView = (TextView) view.findViewById(R.id.shopTextView);
        countTextView = (TextView) view.findViewById(R.id.countTextView);

        getLoaderManager().initLoader(ITEM_LOADER, null, this);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_details_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                listener.onEditItem(itemUri);
                return true;
            case R.id.action_delete:
                deleteItem();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader;

        switch (id) {
            case ITEM_LOADER:
                cursorLoader = new CursorLoader(getActivity(), itemUri, null, null, null, null);
                break;
            default:
                cursorLoader = null;
                break;
        }

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            int nameIndex = data.getColumnIndex(COLUMN_NAME);
            int shopIndex = data.getColumnIndex(COLUMN_SHOP);
            int countIndex = data.getColumnIndex(COLUMN_COUNT);

            nameTextView.setText(data.getString(nameIndex));
            countTextView.setText(data.getString(shopIndex));
            shopTextView.setText(data.getString(countIndex));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void deleteItem() {
        new DeleteDialog().show(getFragmentManager(), "confirm delete");
    }

    public static class DeleteDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.confirm_title);
            builder.setMessage(R.string.confirm_message);

            builder.setPositiveButton(R.string.btn_delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    getActivity().getContentResolver().delete(itemUri, null, null);
                    listener.onItemDeleted();
                }
            });

            builder.setNegativeButton(R.string.btn_cancel, null);

            return builder.create();
        }
    }
}
