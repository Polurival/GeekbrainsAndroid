package com.github.polurival.shoppinglistclient;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import static com.github.polurival.shoppinglistclient.ProviderSettings.*;

/**
 * Created by Polurival
 * on 04.09.2016.
 */
public class AddEditFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // defines callback method implemented by MainActivity
    public interface AddEditFragmentListener {
        // called when item is saved
        void onAddEditCompleted(Uri itemUri);
    }

    private static final int ITEM_LOADER = 0;

    private AddEditFragmentListener listener;
    private Uri itemUri;
    private boolean addingNewItem = true;

    private TextInputLayout nameTextInputLayout;
    private TextInputLayout countTextInputLayout;
    private TextInputLayout shopTextInputLayout;
    private FloatingActionButton saveItemFab;

    private CoordinatorLayout coordinatorLayout;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (AddEditFragmentListener) context;
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

        View view = inflater.inflate(R.layout.fragment_add_edit, container, false);

        nameTextInputLayout = (TextInputLayout) view.findViewById(R.id.nameTextInputLayout);
        nameTextInputLayout.getEditText().addTextChangedListener(textChangedListener);

        countTextInputLayout = (TextInputLayout) view.findViewById(R.id.countTextInputLayout);
        countTextInputLayout.getEditText().addTextChangedListener(textChangedListener);

        shopTextInputLayout = (TextInputLayout) view.findViewById(R.id.shopTextInputLayout);

        saveItemFab = (FloatingActionButton) view.findViewById(R.id.saveItemFab);
        saveItemFab.setOnClickListener(saveItemBtnClicked);
        updateSaveButtonFAB();

        coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinatorLayout);

        Bundle args = getArguments();
        if (args != null) {
            addingNewItem = false;
            itemUri = args.getParcelable(MainActivity.ITEM_URI);
        }
        if (itemUri != null) {
            getLoaderManager().initLoader(ITEM_LOADER, null, this);
        }

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ITEM_LOADER:
                return new CursorLoader(getActivity(), itemUri, null, null, null, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            int nameIndex = data.getColumnIndex(COLUMN_NAME);
            int shopIndex = data.getColumnIndex(COLUMN_SHOP);
            int countIndex = data.getColumnIndex(COLUMN_COUNT);

            nameTextInputLayout.getEditText().setText(data.getString(nameIndex));
            shopTextInputLayout.getEditText().setText(data.getString(shopIndex));
            countTextInputLayout.getEditText().setText(data.getString(countIndex));

            updateSaveButtonFAB();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private final TextWatcher textChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            updateSaveButtonFAB();
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    private void updateSaveButtonFAB() {
        String inputName = nameTextInputLayout.getEditText().getText().toString();
        String inputCount = countTextInputLayout.getEditText().getText().toString();

        if (inputName.trim().length() != 0 || inputCount.trim().length() != 0) {
            saveItemFab.show();
        } else {
            saveItemFab.hide();
        }
    }

    private final View.OnClickListener saveItemBtnClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(getView().getWindowToken(), 0);
            saveItem();
        }
    };

    private void saveItem() {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, nameTextInputLayout.getEditText().getText().toString());
        cv.put(COLUMN_SHOP, shopTextInputLayout.getEditText().getText().toString());
        cv.put(COLUMN_COUNT, countTextInputLayout.getEditText().getText().toString());

        if (addingNewItem) {
            Uri newItemUri = getActivity().getContentResolver().insert(CONTENT_URI, cv);

            if (newItemUri != null) {
                Snackbar.make(coordinatorLayout, R.string.item_added, Snackbar.LENGTH_LONG).show();
                listener.onAddEditCompleted(newItemUri);
            } else {
                Snackbar.make(coordinatorLayout, R.string.item_not_added, Snackbar.LENGTH_LONG).show();
            }
        } else {
            int updatedRows = getActivity().getContentResolver().update(itemUri, cv, null, null);

            if (updatedRows > 0) {
                listener.onAddEditCompleted(itemUri);
                Snackbar.make(coordinatorLayout, R.string.item_updated, Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(coordinatorLayout, R.string.item_not_updated, Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
