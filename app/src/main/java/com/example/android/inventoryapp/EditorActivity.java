package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ItemContract.ItemEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    private static final int ITEM_LOADER = 0;

    private EditText mNameEditText;

    private EditText mPriceEditText;

    private EditText mQuantityEditText;

    private boolean mItemHasChanged = false;

    private Uri currentItemUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(LOG_TAG, "onCreate");

        setContentView(R.layout.activity_editor);

        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_item_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_item_quantity);

        LinearLayout mDeleteCancelLayout = (LinearLayout) findViewById(R.id.delete_cancel_buttons);

        Button mIncrementButton = (Button) findViewById(R.id.increment_button);
        Button mDecrementButton = (Button) findViewById(R.id.decrement_button);

        Button mDeleteButton = (Button) findViewById(R.id.delete_item_button);
        Button mOrderMoreButton = (Button) findViewById(R.id.order_item_button);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);

        currentItemUri = getIntent().getData();

        if (currentItemUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_item));
            mDeleteCancelLayout.setVisibility(View.GONE);
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_item));
            getLoaderManager().initLoader(ITEM_LOADER, null, this);
        }


        mIncrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String quantityStr = mQuantityEditText.getText().toString().trim();

                if (!TextUtils.isEmpty(quantityStr)) {
                    int quantity = Integer.parseInt(quantityStr) + 1;
                    mQuantityEditText.setText(Integer.toString(quantity));

                    mItemHasChanged = true;
                }
            }
        });

        mDecrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String quantityStr = mQuantityEditText.getText().toString().trim();

                if (!TextUtils.isEmpty(quantityStr)) {
                    int quantity = Integer.parseInt(quantityStr);

                    if (quantity > 0) {
                        quantity--;
                    }
                    mQuantityEditText.setText(Integer.toString(quantity));

                    mItemHasChanged = true;
                }
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showDeleteConfirmationDialog();
            }
        });

        mOrderMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String nameString = mNameEditText.getText().toString().trim();
                String priceString = mPriceEditText.getText().toString().trim();
                String quantityString = mQuantityEditText.getText().toString().trim();

                if (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) ||
                        TextUtils.isEmpty(quantityString)) {

                    return;
                }

                String subject = "Item Name: " + nameString + " Quantity: " + quantityString;
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu_editor options from res/menu_editor/menu_editor.xml

        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_save:
                saveItem();
                return true;
            case android.R.id.home:
                // if user clicks home arrow, then check if item has changed.
                // if not, go back to catalog.
                // otherwise show a dialog to confirm
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteItem() {
        if (currentItemUri != null) {
            int rowsDeleted = getContentResolver().delete(currentItemUri, null, null);

            String msg;
            if (rowsDeleted > 0) {
                msg = getString(R.string.editor_delete_item_successful);
            } else {
                msg = getString(R.string.editor_delete_item_failed);
            }

            Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void showDeleteConfirmationDialog() {
        // Show an AlertDialog before deleting item
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteItem();
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Dismiss dialog and keep editing item
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(
                        ContextCompat.getColor(
                                getApplicationContext(), R.color.dialogBoxButtonColor));
                alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(
                        ContextCompat.getColor(
                                getApplicationContext(), R.color.dialogBoxButtonColor));
            }
        });
        alertDialog.show();
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(
                        ContextCompat.getColor(
                                getApplicationContext(), R.color.dialogBoxButtonColor));
                alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(
                        ContextCompat.getColor(
                                getApplicationContext(), R.color.dialogBoxButtonColor));
            }
        });
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    private void saveItem() {

        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();

        if (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(quantityString)) {

            Toast toast = Toast.makeText(getApplicationContext(),
                    getText(R.string.editor_error_msg), Toast.LENGTH_LONG);

            toast.show();

            return;
        }

        ContentValues values = new ContentValues();

        values.put(ItemEntry.COLUMN_ITEM_NAME, nameString);
        values.put(ItemEntry.COLUMN_ITEM_PRICE, priceString);
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantityString);

        String msg;

        if (currentItemUri == null) {
            Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);

            if (newUri == null) {
                msg = getString(R.string.editor_save_item_fail);
            } else {
                msg = getString(R.string.editor_save_item_success);
            }
        } else {
            int updatedRows = getContentResolver().update(currentItemUri, values,
                    null, null);

            if (updatedRows == 0) {
                msg = getString(R.string.editor_save_item_fail);
            } else {
                msg = getString(R.string.editor_save_item_success);
            }
        }

        Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
        toast.show();

        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_QUANTITY
        };

        return new CursorLoader(this, currentItemUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {

            int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);

            String name = cursor.getString(nameColumnIndex);
            Float price = cursor.getFloat(priceColumnIndex);
            Integer quantity = cursor.getInt(quantityColumnIndex);

            mNameEditText.setText(name);
            mPriceEditText.setText(Float.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
    }
}
