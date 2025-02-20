package com.example.xpense_tracker.data;

import static com.example.xpense_tracker.data.model.ExpenseContract.TransactionContent.*;

public final class QueryConstant {

    private QueryConstant() {}

    public static final String DATABASE = "ExpenseTracker.db";

    public static final String CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS users(email TEXT PRIMARY KEY, password TEXT)";
    public static final String DROP_USERS_TABLE = "DROP TABLE IF EXISTS users";

    // SQLite INT primary key means a different ID than rowID, but INTEGER will be the same as the default rowID
    public static final String CREATE_CATEGORY_TABLE = "CREATE TABLE IF NOT EXISTS category(id INTEGER PRIMARY KEY, name TEXT, type TEXT)";
    public static final String DROP_CATEGORY_TABLE = "DROP TABLE IF EXISTS category";

    public static final String CREATE_SUB_CATEGORY_TABLE = "CREATE TABLE IF NOT EXISTS sub_category(id INTEGER PRIMARY KEY, name TEXT, type TEXT, parent_id INT)";
    public static final String DROP_SUB_CATEGORY_TABLE = "DROP TABLE IF EXISTS sub_category";

    public static final String CREATE_EXPENSE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME_CATEGORY + " TEXT, " +
                    COLUMN_NAME_SUB_CATEGORY + " TEXT, " +
                    COLUMN_NAME_TYPE + " TEXT, " +
                    COLUMN_NAME_NOTE + " TEXT, " +
                    COLUMN_NAME_CREATED_AT + " TEXT, " +
                    COLUMN_NAME_AMOUNT + " TEXT, " +
                    COLUMN_NAME_HASH + " TEXT)"; // ✅ Tambahkan hash transaksi

    public static final String DROP_EXPENSE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
}
