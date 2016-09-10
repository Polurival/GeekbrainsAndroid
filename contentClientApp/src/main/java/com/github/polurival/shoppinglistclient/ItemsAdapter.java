package com.github.polurival.shoppinglistclient;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static com.github.polurival.shoppinglistclient.ProviderSettings.*;

/**
 * Created by Polurival
 * on 05.09.2016.
 */
public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {

    public interface ItemClickListener {
        void onClick(Uri itemUri);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private long rowId;
        private final TextView nameTextView;
        private final TextView countTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.columnName);
            countTextView = (TextView) itemView.findViewById(R.id.columnCount);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.onClick(ContentUris.withAppendedId(CONTENT_URI, rowId));
                }
            });
        }

        public void setRowId(long rowId) {
            this.rowId = rowId;
        }
    }

    private Cursor cursor = null;
    private final ItemClickListener clickListener;

    public ItemsAdapter(ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        cursor.moveToPosition(position);
        holder.setRowId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
        holder.nameTextView.setText(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
        holder.countTextView.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(COLUMN_COUNT))));
    }

    @Override
    public int getItemCount() {
        return (cursor != null) ? cursor.getCount() : 0;
    }

    public void swapCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }
}
