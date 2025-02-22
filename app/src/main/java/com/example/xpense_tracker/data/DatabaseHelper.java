package com.example.xpense_tracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "xpense_tracker.db";
    private static final int DATABASE_VERSION = 2; // Update versi database

    public static final String TABLE_EXPENSE = "expense"; // Sesuaikan nama tabel
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_USER = "user"; // Menambahkan kolom user

    private static final String COLUMN_DATE = ;
    private static final String TABLE_CREATE_EXPENSE =
            "CREATE TABLE " + TABLE_EXPENSE + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_AMOUNT + " TEXT, " +
                    COLUMN_CATEGORY + " TEXT, " +
                    COLUMN_DATE + " TEXT, " +
                    COLUMN_TYPE + " TEXT, " +
                    COLUMN_USER + " TEXT, " +
                    "currency TEXT);";  // Kolom baru untuk mata uang

    // Update tabel saat upgrade versi
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {  // Verifikasi apakah perlu upgrade ke versi 2
            db.execSQL("ALTER TABLE " + TABLE_EXPENSE + " ADD COLUMN currency TEXT");
        }
    }


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_EXPENSE); // Buat tabel expense dengan kolom user
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Tambahkan kolom user ke tabel expense jika versi database diubah
            db.execSQL("ALTER TABLE " + TABLE_EXPENSE + " ADD COLUMN " + COLUMN_USER + " TEXT;");
        }
    }
}
