package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ItemDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "store.db";

    // CREATE TABLE inventory (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL,
    //                          quantity INTEGER NOT NULL, price REAL NOT NULL);
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ItemContract.ItemEntry.TABLE_NAME + " (" +
                    ItemContract.ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    ItemContract.ItemEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL," +
                    ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL," +
                    ItemContract.ItemEntry.COLUMN_ITEM_PRICE + " REAL NOT NULL)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ItemContract.ItemEntry.TABLE_NAME;

    public ItemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Database is at version 1.
    }

}
