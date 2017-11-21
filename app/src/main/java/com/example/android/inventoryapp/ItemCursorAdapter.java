package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.data.ItemContract;

public class ItemCursorAdapter extends CursorAdapter {

    private static final String LOG_TAG = ItemCursorAdapter.class.getSimpleName();

    // Needs to be declared here in order for it to be modified with the button
    private TextView quantityTextView;

    // Needed to update
    private int currentItemId;

    /**
     * Constructs a new {@link ItemCursorAdapter}
     *
     * @param   context     The context
     * @param   c           The cursor used to get data.
     */
    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     * Make a blank list item view. Set no data to the views yet.
     *
     * @param   context     app context
     * @param   cursor      The cursor from which to get the data. The cursor is already moved to
     *                      the current position.
     * @param   parent      The parent to which the new view is attached to
     * @return              newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * Bind item data (in current row pointed to by cursor) to given list item layout.
     *
     * @param   view        Existing view, returned by newView() method
     * @param   context     app context
     * @param   cursor      used to get the data. It already points to the correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        Button saleButton = (Button) view.findViewById(R.id.sale);

        String itemName = cursor.getString(cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_NAME));
        Float itemPrice = cursor.getFloat(cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_PRICE));
        int itemQuantity = cursor.getInt(cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY));

        quantityTextView = (TextView) view.findViewById(R.id.quantity);
        currentItemId = cursor.getInt(cursor.getColumnIndex(ItemContract.ItemEntry._ID));

        nameTextView.setText(itemName);
        priceTextView.setText(ItemContract.ItemEntry.formatPrice(itemPrice));

        quantityTextView.setText(Integer.toString(itemQuantity));

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int itemQuantity = Integer.parseInt(quantityTextView.getText().toString());

                if (itemQuantity > 0) {
                    itemQuantity--;
                }

                ContentValues values = new ContentValues();

                values.put(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY, itemQuantity);

                Uri currentItemUri = ContentUris.withAppendedId(ItemContract.ItemEntry.CONTENT_URI,
                        currentItemId);

                context.getContentResolver().update(currentItemUri, values, null, null);
            }

        });
    }
}
